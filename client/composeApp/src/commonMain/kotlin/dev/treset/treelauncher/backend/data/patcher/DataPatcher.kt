package dev.treset.treelauncher.backend.data.patcher

import androidx.compose.runtime.MutableState
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.data.manifest.ParentManifest
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.Version
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.string.PatternString
import dev.treset.treelauncher.localization.Strings
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException

class DataPatcher {

    object PatchStep {
        val CREATE_BACKUP = FormatStringProvider { Strings.launcher.patch.status.createBackup() }
        val UPGRADE_SETTINGS = FormatStringProvider { Strings.launcher.patch.status.upgradeSettings() }
        val GAME_DATA_COMPONENTS = FormatStringProvider { Strings.launcher.patch.status.gameDataComponents() }
        val GAME_DATA_SAVES = FormatStringProvider { Strings.launcher.patch.status.gameDataSaves() }
        val GAME_DATA_MODS = FormatStringProvider { Strings.launcher.patch.status.gameDataMods() }
        val REMOVE_BACKUP_EXCLUDED_FILES = FormatStringProvider { Strings.launcher.patch.status.removeBackupIncludedFiles() }
        val UPGRADE_COMPONENTS = FormatStringProvider { Strings.launcher.patch.status.upgradeComponents() }
        val UPGRADE_MAIN_MANIFEST = FormatStringProvider { Strings.launcher.patch.status.upgradeMainManifest() }
        val UPGRADE_INSTANCES = FormatStringProvider { Strings.launcher.patch.status.upgradeInstances() }
        val UPGRADE_SAVES = FormatStringProvider { Strings.launcher.patch.status.upgradeSaves() }
        val UPGRADE_RESOURCEPACKS = FormatStringProvider { Strings.launcher.patch.status.upgradeResourcepacks() }
        val UPGRADE_OPTIONS = FormatStringProvider { Strings.launcher.patch.status.upgradeOptions() }
        val UPGRADE_MODS = FormatStringProvider { Strings.launcher.patch.status.upgradeMods() }
        val UPGRADE_VERSIONS = FormatStringProvider { Strings.launcher.patch.status.upgradeVersions() }
        val UPGRADE_JAVA = FormatStringProvider { Strings.launcher.patch.status.upgradeJavas() }
        val COMPONENT_DIRECTORIES = FormatStringProvider { Strings.launcher.patch.status.componentDirectories() }
        val INCLUDED_FILES = FormatStringProvider { Strings.launcher.patch.status.includedFiles() }
        val INCLUDED_FILES_INSTANCE = FormatStringProvider { Strings.launcher.patch.status.includedFilesInstances() }
        val INCLUDED_FILES_SAVES = FormatStringProvider { Strings.launcher.patch.status.includedFilesSaves() }
        val INCLUDED_FILES_RESOURCEPACKS = FormatStringProvider { Strings.launcher.patch.status.includedFilesResourcepacks() }
        val INCLUDED_FILES_OPTIONS = FormatStringProvider { Strings.launcher.patch.status.includedFilesOptions() }
        val INCLUDED_FILES_MODS = FormatStringProvider { Strings.launcher.patch.status.includedFilesMods() }
        val REMOVE_RESOURCEPACKS_ARGUMENT = FormatStringProvider { Strings.launcher.patch.status.removeResourcepacksArgument() }
        val TEXTUREPACKS_INCLUDED_FILES = FormatStringProvider { Strings.launcher.patch.status.texturepacksIncludedFiles() }
        val REMOVE_LOGIN = FormatStringProvider { Strings.launcher.patch.status.removeLogin() }
        val RESTRUCTURE_MODS = FormatStringProvider { Strings.launcher.patch.status.restructureMods() }
    }

    private class UpgradeFunction(
        val function: (StatusProvider) -> Unit,
        val applies: () -> Boolean
    ) {
        constructor(function: (StatusProvider) -> Unit, vararg version: Version) : this(function, {
            version.any { appConfig().dataVersion >= it && Version.fromString(AppSettings.dataVersion.value) < it }
        })

        fun execute(statusProvider: StatusProvider) {
            if(applies()) {
                function(statusProvider)
            }
        }
    }

    private val upgradeMap: Array<UpgradeFunction> = arrayOf(
        UpgradeFunction(this::moveGameDataComponents, Version(1,0,0)),
        UpgradeFunction(this::removeBackupExcludedFiles, Version(1,0,0)),
        UpgradeFunction(this::upgradeComponents, Version(2,0,0)),
        UpgradeFunction(this::upgradeComponentDirectories, Version(2,0,0)),
        UpgradeFunction(this::upgradeIncludedFiles, Version(2,0,0)),
        UpgradeFunction(this::removeResourcepacksDirGameArguments, Version(2,0,0)),
        UpgradeFunction(this::upgradeTexturePacksIncludedFiles, Version(2,0,0)),
        UpgradeFunction(this::removeLoginFile, Version(2,0,0)),
        UpgradeFunction(this::restructureMods, Version(2, 1, 0)),
        UpgradeFunction(this::upgradeSettings, Version(1,0,0), Version(2,0,0), Version(2, 1, 0))
    )

    fun upgradeNeeded(): Boolean {
        return upgradeMap.any { it.applies() }
    }

    @Throws(IOException::class)
    fun performUpgrade(backup: Boolean, onStatus: (Status) -> Unit) {
        if(!upgradeNeeded()) {
            return
        }

        val statusProvider = StatusProvider(null, 0, onStatus)

        LOGGER.info { "Performing data upgrade: v${AppSettings.dataVersion.value} -> v${appConfig().dataVersion} " }
        if(backup) {
            backupFiles(statusProvider)
        }
        for(upgrade in upgradeMap) {
            upgrade.execute(statusProvider)
        }
        LOGGER.info { "Data upgrade complete" }
    }

    @Throws(IOException::class)
    fun backupFiles(statusProvider: StatusProvider) {
        LOGGER.info { "Creating backup..." }
        val backupProvider = statusProvider.subStep(PatchStep.CREATE_BACKUP, 1)
        backupProvider.next("")
        val dir = LauncherFile.ofData()
        val backupDir = LauncherFile.ofData(".backup")
        if(backupDir.exists()) {
            backupDir.remove()
        }
        dir.copyTo(backupDir)
        backupProvider.finish("")
        LOGGER.info { "Created backup" }
    }

    @Throws(IOException::class)
    fun restructureMods(statusProvider: StatusProvider) {
        LOGGER.info { "Restructuring mods" }
        val modsStatusProvider = statusProvider.subStep(PatchStep.RESTRUCTURE_MODS, 1)

        val files = Pre2_1LauncherFiles()
        files.reload()

        val total = files.modsComponents.sumOf { it.mods.size }
        modsStatusProvider.total = total
        files.modsComponents.forEach { c ->
            c.mods.forEach { m ->
                modsStatusProvider.next("${c.name}: ${m.name}")
                m.toLauncherMod(c.modsDirectory)
            }
        }

        modsStatusProvider.finish()
        LOGGER.info { "Finished restructuring mods" }
    }

    @Throws(IOException::class)
    fun moveGameDataComponents(statusProvider: StatusProvider) {
        LOGGER.info { "Moving game data components..."}
        val gameDataComponentsProvider = statusProvider.subStep(PatchStep.GAME_DATA_COMPONENTS, 4)
        gameDataComponentsProvider.next("")

        val files = Pre1_0LauncherFiles()
        files.reloadAll()
        val gameDataDir = LauncherFile.ofData(files.launcherDetails.gamedataDir)

        LOGGER.info { "Moving saves components..." }
        gameDataComponentsProvider.next("saves")
        val savesDir = LauncherFile.ofData(files.launcherDetails.savesDir)
        LauncherFile.of(files.savesManifest.directory, files.gameDetailsManifest.components[1]).moveTo(
            LauncherFile.of(savesDir, appConfig().manifestFileName)
        )

        val savesProvider = gameDataComponentsProvider.subStep(PatchStep.GAME_DATA_SAVES, files.savesComponents.size)
        for(file in gameDataDir.listFiles()) {
            if(file.isDirectory && file.name.startsWith(files.savesManifest.prefix)) {
                savesProvider.next(file.name)
                file.atomicMoveTo(LauncherFile.of(savesDir, file.name))
            }
        }
        savesProvider.finish("")
        LOGGER.info { "Moved saves components" }

        LOGGER.info { "Moving mods components..." }
        gameDataComponentsProvider.next("mods")
        val modsDir = LauncherFile.ofData(files.launcherDetails.modsDir)
        LauncherFile.of(files.modsManifest.directory, files.gameDetailsManifest.components[0]).moveTo(
            LauncherFile.of(modsDir, appConfig().manifestFileName)
        )

        val modsProvider = gameDataComponentsProvider.subStep(PatchStep.GAME_DATA_MODS, files.modsComponents.size)
        for(file in gameDataDir.listFiles()) {
            if(file.isDirectory && file.name.startsWith(files.modsManifest.prefix)) {
                modsProvider.next(file.name)
                file.atomicMoveTo(LauncherFile.of(modsDir, file.name))
            }
        }
        modsProvider.finish("")
        LOGGER.info { "Moved mods components" }

        LOGGER.info { "Removing old manifest..." }
        gameDataComponentsProvider.next("")
        LauncherFile.ofData(files.launcherDetails.gamedataDir, appConfig().manifestFileName).remove()
        LOGGER.info { "Removed old manifest" }

        gameDataComponentsProvider.finish("")
        LOGGER.info { "Moved game data components" }
    }

    @Throws(IOException::class)
    fun removeBackupExcludedFiles(statusProvider: StatusProvider) {
        LOGGER.info { "Removing backup excluded files from instances..." }
        val backupExcludedFilesProvider = statusProvider.subStep(PatchStep.REMOVE_BACKUP_EXCLUDED_FILES, 2)
        backupExcludedFilesProvider.next()

        val files = Pre2_0LauncherFiles()
        files.reloadAll()

        backupExcludedFilesProvider.total = files.instanceComponents.size + 1
        files.instanceComponents.forEach {
            backupExcludedFilesProvider.next(it.first.name)
            it.second.ignoredFiles = it.second.ignoredFiles.filter { file ->
                !PatternString(file, true).matches("backups/")
            }
            LauncherFile.of(it.first.directory, it.first.details).write(it.second)
        }
        backupExcludedFilesProvider.finish()
        LOGGER.info { "Removed backup excluded files from instances" }
    }

    @Throws(IOException::class)
    fun upgradeComponents(statusProvider: StatusProvider) {
        LOGGER.info { "Upgrading components..." }
        val componentStatusProvider = statusProvider.subStep(PatchStep.UPGRADE_COMPONENTS, 9)
        componentStatusProvider.next("")

        val files = Pre2_0LauncherFiles()
        files.reloadAll()

        upgradeMainManifest(files, statusProvider)

        componentStatusProvider.next("instances")
        upgradeParentManifest(files.instanceManifest)
        val instancesStatusProvider = componentStatusProvider.subStep(PatchStep.UPGRADE_INSTANCES, files.instanceComponents.size)
        for(instance in files.instanceComponents) {
            instancesStatusProvider.next(instance.first.name)
            upgradeComponent(instance.first) { instance.toInstanceComponent() }
        }
        instancesStatusProvider.finish("")

        componentStatusProvider.next("saves")
        upgradeParentManifest(files.savesManifest)
        val savesStatusProvider = componentStatusProvider.subStep(PatchStep.UPGRADE_SAVES, files.savesComponents.size)
        for(saves in files.savesComponents) {
            savesStatusProvider.next(saves.name)
            upgradeComponent(saves) { saves.toSavesComponent() }
        }
        savesStatusProvider.finish("")

        componentStatusProvider.next("resourcepacks")
        upgradeParentManifest(files.resourcepackManifest)
        val resourcepacksStatusProvider = componentStatusProvider.subStep(PatchStep.UPGRADE_RESOURCEPACKS, files.resourcepackComponents.size)
        for(resourcepacks in files.resourcepackComponents) {
            resourcepacksStatusProvider.next(resourcepacks.name)
            upgradeComponent(resourcepacks) { resourcepacks.toResourcepackComponent() }
        }
        resourcepacksStatusProvider.finish("")

        componentStatusProvider.next("options")
        upgradeParentManifest(files.optionsManifest)
        val optionsStatusProvider = componentStatusProvider.subStep(PatchStep.UPGRADE_OPTIONS, files.optionsComponents.size)
        for(options in files.optionsComponents) {
            optionsStatusProvider.next(options.name)
            upgradeComponent(options) { options.toOptionsComponent() }
        }
        optionsStatusProvider.finish("")

        componentStatusProvider.next("mods")
        upgradeParentManifest(files.modsManifest)
        val modsStatusProvider = componentStatusProvider.subStep(PatchStep.UPGRADE_MODS, files.modsComponents.size)
        for(mods in files.modsComponents) {
            modsStatusProvider.next(mods.first.name)
            upgradeComponent(mods.first) { mods.toPre3_1ModsComponent() }
        }

        componentStatusProvider.next("versions")
        upgradeParentManifest(files.versionManifest)
        val versionsStatusProvider = componentStatusProvider.subStep(PatchStep.UPGRADE_VERSIONS, files.versionComponents.size)
        for(version in files.versionComponents) {
            versionsStatusProvider.next(version.first.name)
            upgradeComponent(version.first) { version.toVersionComponent() }
        }
        versionsStatusProvider.finish("")

        componentStatusProvider.next("java")
        upgradeParentManifest(files.javaManifest)
        val javaStatusProvider = componentStatusProvider.subStep(PatchStep.UPGRADE_JAVA, files.javaComponents.size)
        for(java in files.javaComponents) {
            javaStatusProvider.next(java.name)
            upgradeComponent(java) { java.toJavaComponent() }
        }
        javaStatusProvider.finish("")

        componentStatusProvider.finish("")
        LOGGER.info { "Upgraded components" }
    }

    @Throws(IOException::class)
    fun upgradeMainManifest(files: Pre2_0LauncherFiles, statusProvider: StatusProvider) {
        LOGGER.info { "Upgrading launcher details..." }
        val mainManifestProvider = statusProvider.subStep(PatchStep.UPGRADE_MAIN_MANIFEST, 1)
        mainManifestProvider.next()
        val oldFile = LauncherFile.of(files.mainManifest.directory, appConfig().manifestFileName)
        val backupFile = LauncherFile.of(files.mainManifest.directory, appConfig().manifestFileName + ".old")
        oldFile.moveTo(backupFile)

        val mainManifest = files.launcherDetails.toMainManifest()
        mainManifest.write()

        backupFile.remove()
        LauncherFile.of(files.mainManifest.directory, files.mainManifest.details).remove()

        mainManifestProvider.finish()
        LOGGER.info { "Upgraded launcher details" }
    }

    @Throws(IOException::class)
    fun upgradeParentManifest(manifest: Pre2_0ParentManifest) {
        LOGGER.info { "Upgrading parent manifest: ${manifest.type}..." }
        val oldFile = LauncherFile.of(manifest.directory, appConfig().manifestFileName)
        val backupFile = LauncherFile.of(manifest.directory, appConfig().manifestFileName + ".old")
        oldFile.moveTo(backupFile)

        val newManifest = ParentManifest(
            manifest.type,
            manifest.prefix,
            manifest.components,
            LauncherFile.of(manifest.directory, appConfig().manifestFileName)
        )
        newManifest.write()

        backupFile.remove()
        LOGGER.info { "Upgraded parent manifest: ${manifest.type}" }
    }

    @Throws(IOException::class)
    fun upgradeComponent(component: Pre2_0ComponentManifest, toComponent: () -> Component) {
        LOGGER.info { "Upgrading component: ${component.type}: ${component.id}" }
        val oldFile = LauncherFile.of(component.directory, appConfig().manifestFileName)
        val backupFile = LauncherFile.of(component.directory, appConfig().manifestFileName + ".old")
        oldFile.moveTo(backupFile)

        val newComponent = toComponent()
        newComponent.write()

        backupFile.remove()
        if(!component._details.isNullOrBlank()) {
            LauncherFile.of(component.directory, component.details).remove()
        }
        LOGGER.info { "Upgraded component: ${component.type}: ${component.id}" }
    }

    @Throws(IOException::class)
    fun upgradeComponentDirectories(statusProvider: StatusProvider) {
        LOGGER.info { "Upgrading component directories..." }
        val componentDirectoriesProvider = statusProvider.subStep(PatchStep.COMPONENT_DIRECTORIES, 10)
        componentDirectoriesProvider.next()

        val files = Pre2_1LauncherFiles()
        files.reload()

        componentDirectoriesProvider.next("instances")
        upgradeComponentsDirectory(files.mainManifest.instancesDir, "instances")
        componentDirectoriesProvider.next("saves_components")
        upgradeComponentsDirectory(files.mainManifest.savesDir, "saves_components")
        componentDirectoriesProvider.next("resourcepacks_components")
        upgradeComponentsDirectory(files.mainManifest.resourcepacksDir, "resourcepacks_components")
        componentDirectoriesProvider.next("options_components")
        upgradeComponentsDirectory(files.mainManifest.optionsDir, "options_components")
        componentDirectoriesProvider.next("mods_components")
        upgradeComponentsDirectory(files.mainManifest.modsDir, "mods_components")
        componentDirectoriesProvider.next("version_components")
        upgradeComponentsDirectory(files.mainManifest.versionDir, "version_components")
        componentDirectoriesProvider.next("java_components")
        upgradeComponentsDirectory(files.mainManifest.javasDir, "java_components")

        componentDirectoriesProvider.next()
        files.mainManifest.write()

        componentDirectoriesProvider.finish()
        LOGGER.info { "Upgraded component directories" }
    }

    @Throws(IOException::class)
    fun upgradeComponentsDirectory(property: MutableState<String>, targetName: String) {
        val srcDir = LauncherFile.ofData(property.value)
        val targetDir = LauncherFile.ofData(targetName)
        if(srcDir.exists()) {
            srcDir.atomicMoveTo(targetDir)
            property.value = targetName
        }
    }

    @Throws(IOException::class)
    fun upgradeIncludedFiles(statusProvider: StatusProvider) {
        LOGGER.info { "Upgrading included files..." }
        val includedFilesProvider = statusProvider.subStep(PatchStep.INCLUDED_FILES, 6)
        includedFilesProvider.next("")

        val files = Pre2_1LauncherFiles()
        files.reload()

        includedFilesProvider.next("instances")
        val instanceProvider = includedFilesProvider.subStep(PatchStep.INCLUDED_FILES_INSTANCE, files.instanceComponents.size)
        for(instance in files.instanceComponents) {
            instanceProvider.next(instance.name.value)
            upgradeIncludedFiles(instance)
        }
        instanceProvider.finish("")

        includedFilesProvider.next("saves")
        val savesProvider = includedFilesProvider.subStep(PatchStep.INCLUDED_FILES_SAVES, files.savesComponents.size)
        for(saves in files.savesComponents) {
            savesProvider.next(saves.name.value)
            moveRootFilesToDirectory(saves, "saves")
            upgradeIncludedFiles(saves)
        }
        savesProvider.finish("")

        includedFilesProvider.next("resourcepacks")
        val resourcepacksProvider = includedFilesProvider.subStep(PatchStep.INCLUDED_FILES_RESOURCEPACKS, files.resourcepackComponents.size)
        for(resourcepacks in files.resourcepackComponents) {
            resourcepacksProvider.next(resourcepacks.name.value)
            moveRootFilesToDirectory(resourcepacks, "resourcepacks")
            upgradeIncludedFiles(resourcepacks)
        }
        resourcepacksProvider.finish("")

        includedFilesProvider.next("options")
        val optionsProvider = includedFilesProvider.subStep(PatchStep.INCLUDED_FILES_OPTIONS, files.optionsComponents.size)
        for(options in files.optionsComponents) {
            optionsProvider.next(options.name.value)
            upgradeIncludedFiles(options)
        }
        optionsProvider.finish("")

        includedFilesProvider.next("mods")
        val modsProvider = includedFilesProvider.subStep(PatchStep.INCLUDED_FILES_MODS, files.modsComponents.size)
        for(mods in files.modsComponents) {
            modsProvider.next(mods.name.value)
            moveRootFilesToDirectory(mods, "mods")
            upgradeIncludedFiles(mods)
        }
        modsProvider.finish("")

        includedFilesProvider.finish("")
        LOGGER.info { "Upgraded included files" }
    }

    @Throws(IOException::class)
    fun moveRootFilesToDirectory(component: Component, dirName: String) {
        LOGGER.info { "Moving root files to directory for ${component.type}: ${component.id}..." }

        val dir = LauncherFile.of(component.directory)
        val target = dir.child(dirName)
        val files = dir.listFiles()
        for(file in files) {
            if(file.name != dirName
                && file.name != appConfig().manifestFileName
                && file.name != appConfig().syncFileName
                && file.name != ".included_files_old"
                && file.name != ".included_files"
            ) {
                file.atomicMoveTo(target.child(file.name))
            }
        }

        val toAdd = target.launcherName
        if(!component.includedFiles.contains(toAdd)) {
            component.includedFiles += toAdd
            component.write()
        }

        LOGGER.info { "Moved root files to directory for ${component.type}: ${component.id}" }
    }

    @Throws(IOException::class)
    fun upgradeIncludedFiles(component: Component) {
        LOGGER.info { "Upgrading included files for ${component.type}: ${component.id}..." }

        val dir = LauncherFile.of(component.directory, ".included_files")
        val files = dir.listFiles()
        for(file in files) {
            file.atomicMoveTo(LauncherFile.of(component.directory, file.name))
        }
        dir.remove()

        LauncherFile.of(component.directory, ".included_files_old").existsOrNull()?.remove()
        LOGGER.info { "Upgraded included files for ${component.type}: ${component.id}" }
    }

    @Throws(IOException::class)
    fun upgradeTexturePacksIncludedFiles(statusProvider: StatusProvider) {
        LOGGER.info { "Upgrading texturepacks included files..." }
        val texturepacksProvider = statusProvider.subStep(PatchStep.TEXTUREPACKS_INCLUDED_FILES, 2)
        texturepacksProvider.next()
        val files = Pre2_1LauncherFiles()
        files.reload()

        texturepacksProvider.total = files.resourcepackComponents.size + 1
        for(resourcepacks in files.resourcepackComponents) {
            texturepacksProvider.next(resourcepacks.name.value)
            if(!resourcepacks.includedFiles.contains("texturepacks/")) {
                resourcepacks.includedFiles += "texturepacks/"
                resourcepacks.write()
            }
        }
        texturepacksProvider.finish()
        LOGGER.info { "Upgraded texturepacks included files" }
    }

    @Throws(IOException::class)
    fun removeResourcepacksDirGameArguments(statusProvider: StatusProvider) {
        LOGGER.info { "Removing resourcepacks directory game arguments..." }
        val resourcepacksProvider = statusProvider.subStep(PatchStep.REMOVE_RESOURCEPACKS_ARGUMENT, 2)
        resourcepacksProvider.next()

        val files = Pre2_1LauncherFiles()
        files.reload()

        resourcepacksProvider.total = files.versionComponents.size + 1

        files.versionComponents.forEach {
            resourcepacksProvider.next(it.name.value)
            it.gameArguments.removeIf { arg ->
                arg.argument == "--resourcePackDir" || arg.argument == "\${resourcepack_directory}"
            }
            it.write()
        }
        resourcepacksProvider.finish()
        LOGGER.info { "Removed resourcepacks directory game arguments" }
    }

    @Throws(IOException::class)
    fun removeLoginFile(statusProvider: StatusProvider) {
        LOGGER.info { "Removing login file..." }
        val loginFileProvider = statusProvider.subStep(PatchStep.REMOVE_LOGIN, 1)
        loginFileProvider.next()
        val loginFile = LauncherFile.of(appConfig().metaDir, "secrets.auth")
        if(loginFile.exists()) {
            loginFile.remove()
        }
        loginFileProvider.finish()
        LOGGER.info { "Removed login file" }
    }

    @Throws(IOException::class)
    fun upgradeSettings(statusProvider: StatusProvider) {
        LOGGER.info { "Upgrading settings..." }
        val upgradeSettingsProvider = statusProvider.subStep(PatchStep.UPGRADE_SETTINGS, 1)
        upgradeSettingsProvider.next()
        AppSettings.dataVersion.value = appConfig().dataVersion.toString()
        AppSettings.save()
        upgradeSettingsProvider.finish()
        LOGGER.info { "Upgraded settings" }
    }

    companion object {
        val LOGGER = KotlinLogging.logger {  }
    }
}