package net.treset.treelauncher.backend.data.patcher

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.Pre2_5LauncherFiles
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.util.Version
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException

class DataPatcher {
    data class PatchStatus(
        val step: PatchStep,
        val message: String
    )

    enum class PatchStep {
        CREATE_BACKUP,
        REMOVE_BACKUP_EXCLUDED_FILES,
        UPGRADE_SETTINGS,
        GAME_DATA_COMPONENTS,
        INCLUDED_FILES,
        REMOVE_RESOURCEPACKS_ARGUMENT,
        ADD_GAME_DATA_INCLUDED_FILES,
        TEXTUREPACKS_INCLUDED_FILES,
        REMOVE_LOGIN
    }

    private class UpgradeFunction(
        val function: (onStatus: (PatchStatus) -> Unit) -> Unit,
        val applies: () -> Boolean
    ) {
        constructor(function: (onStatus: (PatchStatus) -> Unit) -> Unit, vararg version: Version) : this(function, {
            version.any { appConfig().dataVersion >= it && Version.fromString(appSettings().dataVersion) < it }
        })

        fun execute(onStatus: (PatchStatus) -> Unit) {
            if(applies()) {
                function(onStatus)
            }
        }
    }

    private val upgradeMap: Array<UpgradeFunction> = arrayOf(
        UpgradeFunction(this::moveGameDataComponents, Version(1,0,0)),
        UpgradeFunction(this::removeBackupExcludedFiles, Version(1,0,0)),
        UpgradeFunction(this::upgradeIncludedFiles, Version(2,0,0)),
        UpgradeFunction(this::addNewIncludedFilesToManifest, Version(2,0,0)),
        UpgradeFunction(this::removeResourcepacksDirGameArguments, Version(2,0,0)),
        UpgradeFunction(this::upgradeTexturePacksIncludedFiles, Version(2,0,0)),
        UpgradeFunction(this::removeLoginFile, Version(2,0,0)),
        UpgradeFunction(this::upgradeSettings, Version(1,0,0), Version(2,0,0))
    )

    fun upgradeNeeded(): Boolean {
        return upgradeMap.any { it.applies() }
    }

    @Throws(IOException::class)
    fun performUpgrade(backup: Boolean, onStatus: (PatchStatus) -> Unit) {
        if(!upgradeNeeded()) {
            return
        }

        LOGGER.info { "Performing data upgrade: v${appSettings().dataVersion} -> v${appConfig().dataVersion} " }
        if(backup) {
            backupFiles(onStatus)
        }
        for(upgrade in upgradeMap) {
            upgrade.execute(onStatus)
        }
        LOGGER.info { "Data upgrade complete" }
    }

    @Throws(IOException::class)
    fun backupFiles(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Creating backup..." }
        onStatus(PatchStatus(PatchStep.CREATE_BACKUP, ""))
        val dir = LauncherFile.ofData()
        val backupDir = LauncherFile.ofData(".backup")
        if(backupDir.exists()) {
            backupDir.remove()
        }
        dir.copyTo(backupDir)
        LOGGER.info { "Created backup" }
    }

    @Throws(IOException::class)
    fun moveGameDataComponents(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Moving game data components..."}
        onStatus(PatchStatus(PatchStep.GAME_DATA_COMPONENTS, ""))

        val files = Pre2_5LauncherFiles()
        files.reloadAll()
        val gameDataDir = LauncherFile.ofData(files.launcherDetails.gamedataDir)

        LOGGER.info { "Moving mods components..." }
        val modsDir = LauncherFile.ofData(files.launcherDetails.modsDir)
        LauncherFile.of(files.modsManifest.directory, files.gameDetailsManifest.components[0]).moveTo(
            LauncherFile.of(modsDir, appConfig().manifestFileName)
        )
        for(file in gameDataDir.listFiles()) {
            if(file.isDirectory && file.name.startsWith(files.modsManifest.prefix)) {
                onStatus(PatchStatus(PatchStep.GAME_DATA_COMPONENTS, file.name))
                file.moveTo(LauncherFile.of(modsDir, file.name))
            }
        }
        LOGGER.info { "Moved mods components" }

        LOGGER.info { "Moving saves components..." }
        val savesDir = LauncherFile.ofData(files.launcherDetails.savesDir)
        LauncherFile.of(files.savesManifest.directory, files.gameDetailsManifest.components[1]).moveTo(
            LauncherFile.of(savesDir, appConfig().manifestFileName)
        )
        for(file in gameDataDir.listFiles()) {
            if(file.isDirectory && file.name.startsWith(files.savesManifest.prefix)) {
                onStatus(PatchStatus(PatchStep.GAME_DATA_COMPONENTS, file.name))
                file.moveTo(LauncherFile.of(savesDir, file.name))
            }
        }
        LOGGER.info { "Moved saves components" }

        LOGGER.info { "Removing old manifest..." }
        LauncherFile.ofData(files.launcherDetails.gamedataDir, appConfig().manifestFileName).remove()
        LOGGER.info { "Removed old manifest" }

        LOGGER.info { "Moved game data components" }
    }

    @Throws(IOException::class)
    fun upgradeIncludedFiles(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Upgrading included files..." }
        onStatus(PatchStatus(PatchStep.INCLUDED_FILES, ""))

        val files = LauncherFiles()
        files.reloadAll()

        for(instance in files.instanceComponents) {
            onStatus(PatchStatus(PatchStep.INCLUDED_FILES, "instance: ${instance.first.id}"))
            upgradeIncludedFiles(instance.first)
        }

        for(mods in files.modsComponents) {
            onStatus(PatchStatus(PatchStep.INCLUDED_FILES, "mods: ${mods.first.id}"))
            moveRootFilesToDirectory(mods.first, "mods")
            upgradeIncludedFiles(mods.first)
        }

        for(saves in files.savesComponents) {
            onStatus(PatchStatus(PatchStep.INCLUDED_FILES, "saves: ${saves.id}"))
            moveRootFilesToDirectory(saves, "saves")
            upgradeIncludedFiles(saves)
        }

        for(resourcepacks in files.resourcepackComponents) {
            onStatus(PatchStatus(PatchStep.INCLUDED_FILES, "resourcepacks: ${resourcepacks.id}"))
            moveRootFilesToDirectory(resourcepacks, "resourcepacks")
            upgradeIncludedFiles(resourcepacks)
        }

        for(options in files.optionsComponents) {
            onStatus(PatchStatus(PatchStep.INCLUDED_FILES, "options: ${options.id}"))
            upgradeIncludedFiles(options)
        }

        LOGGER.info { "Upgraded included files" }
    }

    @Throws(IOException::class)
    fun moveRootFilesToDirectory(component: ComponentManifest, dirName: String) {
        LOGGER.info { "Moving root files to directory for ${component.type}: ${component.id}..." }

        val dir = LauncherFile.of(component.directory)
        val files = dir.listFiles()
        for(file in files) {
            if(file.name != dirName
                && file.name != appConfig().manifestFileName
                && file.name != component.details
                && file.name != appConfig().syncFileName
                && file.name != ".included_files_old"
                && file.name != ".included_files"
            ) {
                file.moveTo(LauncherFile.of(dir, dirName, file.name))
            }
        }

        LOGGER.info { "Moved root files to directory for ${component.type}: ${component.id}" }
    }

    @Throws(IOException::class)
    fun upgradeIncludedFiles(component: ComponentManifest) {
        LOGGER.info { "Upgrading included files for ${component.type}: ${component.id}..." }

        val dir = LauncherFile.of(component.directory, ".included_files")
        val files = dir.listFiles()
        for(file in files) {
            file.moveTo(LauncherFile.of(component.directory, file.name))
        }
        dir.remove()

        LauncherFile.of(component.directory, ".included_files_old").existsOrNull()?.remove()
        LOGGER.info { "Upgraded included files for ${component.type}: ${component.id}" }
    }

    @Throws(IOException::class)
    fun upgradeTexturePacksIncludedFiles(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Upgrading texturepacks included files..." }
        onStatus(PatchStatus(PatchStep.TEXTUREPACKS_INCLUDED_FILES, ""))
        val files = LauncherFiles()
        files.reloadAll()

        for(resourcepacks in files.resourcepackComponents) {
            onStatus(PatchStatus(PatchStep.TEXTUREPACKS_INCLUDED_FILES, resourcepacks.id))
            if(!resourcepacks.includedFiles.contains("texturepacks/")) {
                resourcepacks.includedFiles += "texturepacks/"
                LauncherFile.of(resourcepacks.directory, appConfig().manifestFileName).write(resourcepacks)
            }
        }
        LOGGER.info { "Upgraded texturepacks included files" }
    }

    @Throws(IOException::class)
    fun removeResourcepacksDirGameArguments(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Removing resourcepacks directory game arguments..." }
        onStatus(PatchStatus(PatchStep.REMOVE_RESOURCEPACKS_ARGUMENT, ""))
        val files = LauncherFiles()
        files.reloadAll()
        files.versionComponents.forEach {
            onStatus(PatchStatus(PatchStep.REMOVE_RESOURCEPACKS_ARGUMENT, it.first.name))
            it.second.gameArguments = it.second.gameArguments.filter { arg ->
                arg.argument != "--resourcePackDir" && arg.argument != "\${resourcepack_directory}"
            }
            LauncherFile.of(it.first.directory, it.first.details).write(it.second)
        }
        LOGGER.info { "Removed resourcepacks directory game arguments" }
    }

    @Throws(IOException::class)
    fun addNewIncludedFilesToManifest(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Adding new included files..." }
        onStatus(PatchStatus(PatchStep.ADD_GAME_DATA_INCLUDED_FILES, ""))
        val files = LauncherFiles()
        files.reloadAll()

        for(saves in files.savesComponents) {
            onStatus(PatchStatus(PatchStep.ADD_GAME_DATA_INCLUDED_FILES, "saves: ${saves.id}"))
            saves.includedFiles += "saves/"
            LauncherFile.of(saves.directory, appConfig().manifestFileName).write(saves)
        }

        for(resourcepacks in files.resourcepackComponents) {
            onStatus(PatchStatus(PatchStep.ADD_GAME_DATA_INCLUDED_FILES, "resourcepacks: ${resourcepacks.id}"))
            resourcepacks.includedFiles += "resourcepacks/"
            LauncherFile.of(resourcepacks.directory, appConfig().manifestFileName).write(resourcepacks)
        }

        for(mods in files.modsComponents) {
            onStatus(PatchStatus(PatchStep.ADD_GAME_DATA_INCLUDED_FILES, "mods: ${mods.first.id}"))
            mods.first.includedFiles += "mods/"
            LauncherFile.of(mods.first.directory, appConfig().manifestFileName).write(mods.first)
        }
    }

    @Throws(IOException::class)
    fun removeBackupExcludedFiles(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Removing backup excluded files from instances..." }
        onStatus(PatchStatus(PatchStep.REMOVE_BACKUP_EXCLUDED_FILES, ""))

        val files = LauncherFiles()
        files.reloadAll()
        files.instanceComponents.forEach {
            onStatus(PatchStatus(PatchStep.REMOVE_BACKUP_EXCLUDED_FILES, it.first.id))
            it.second.ignoredFiles = it.second.ignoredFiles.filter { file ->
                !PatternString(file, true).matches("backups/")
            }
            LauncherFile.of(it.first.directory, it.first.details).write(it.second)
        }
        LOGGER.info { "Removed backup excluded files from instances" }
    }

    @Throws(IOException::class)
    fun upgradeSettings(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Upgrading settings..." }
        onStatus(PatchStatus(PatchStep.UPGRADE_SETTINGS, ""))
        appSettings().dataVersion = appConfig().dataVersion.toString()
        appSettings().save()
        LOGGER.info { "Upgraded settings" }
    }

    @Throws(IOException::class)
    fun removeLoginFile(onStatus: (PatchStatus) -> Unit) {
        LOGGER.info { "Removing login file..." }
        onStatus(PatchStatus(PatchStep.REMOVE_LOGIN, ""))
        val loginFile = LauncherFile.of(appConfig().metaDir, "secrets.auth")
        if(loginFile.exists()) {
            loginFile.remove()
        }
        LOGGER.info { "Removed login file" }
    }

    companion object {
        val LOGGER = KotlinLogging.logger {  }
    }
}