package net.treset.treelauncher.backend.data.patcher

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.Pre2_5LauncherFiles
import net.treset.treelauncher.backend.util.Version
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException

class DataPatcher(
    currentVersion: String,
    previousVersion: String
) {
    private val currVer = Version.fromString(currentVersion)
    private val prevVer = Version.fromString(previousVersion)

    enum class UpgradeStep {
        REMOVE_BACKUP_EXCLUDED_FILES,
        UPGRADE_SETTINGS,
        UPDATE_GAME_DATA_COMPONENTS
    }

    private class UpgradeFunction(
        val function: (onStep: (UpgradeStep) -> Unit) -> Unit,
        val condition: () -> Boolean
    ) {
        fun execute(onStep: (UpgradeStep) -> Unit) {
            if(condition()) {
                function(onStep)
            }
        }
    }

    private val upgradeMap: Array<UpgradeFunction> = arrayOf(
        UpgradeFunction(this::moveGameDataComponents) { currVer >= Version(2, 5, 0) && prevVer < Version(2, 5, 0) },
        UpgradeFunction(this::removeBackupExcludedFiles) { currVer >= Version(2, 5, 0) && prevVer < Version(2, 5, 0) },
        UpgradeFunction(this::upgradeSettings) { currVer >= Version(2, 5, 0) && prevVer < Version(2, 5, 0) }
    )

    @Throws(IOException::class)
    fun performUpgrade(onStep: (UpgradeStep) -> Unit) {
        if(currVer <= prevVer) {
            return
        }

        LOGGER.info { "Performing data upgrade: v${prevVer} -> v${currVer} " }
        for(upgrade in upgradeMap) {
            upgrade.execute(onStep)
        }
        LOGGER.info { "Data upgrade complete" }
    }

    @Throws(IOException::class)
    fun moveGameDataComponents(onStep: (UpgradeStep) -> Unit) {
        LOGGER.info { "Moving game data components..."}
        val files = Pre2_5LauncherFiles()
        files.reloadAll()

        onStep(UpgradeStep.UPDATE_GAME_DATA_COMPONENTS)
        val gameDataDir = LauncherFile.ofData(files.launcherDetails.gamedataDir)

        LOGGER.info { "Moving mods components..." }
        val modsDir = LauncherFile.ofData(files.launcherDetails.modsDir)
        LauncherFile.of(files.modsManifest.directory, files.gameDetailsManifest.components[0]).moveTo(
            LauncherFile.of(modsDir, appConfig().manifestFileName)
        )
        for(file in gameDataDir.listFiles()) {
            if(file.isDirectory && file.name.startsWith(files.modsManifest.prefix)) {
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
                file.moveTo(LauncherFile.of(savesDir, file.name))
            }
        }
        LOGGER.info { "Moved saves components" }

        LOGGER.info { "Removing old manifest..." }
        LauncherFile.of(files.launcherDetails.gamedataDir, appConfig().manifestFileName).remove()
        LOGGER.info { "Removed old manifest" }

        LOGGER.info { "Moved game data components" }
    }

    @Throws(IOException::class)
    fun removeBackupExcludedFiles(onStep: (UpgradeStep) -> Unit) {
        LOGGER.info { "Removing backup excluded files from instances..." }
        val files = LauncherFiles()
        files.reloadAll()

        onStep(UpgradeStep.REMOVE_BACKUP_EXCLUDED_FILES)
        files.instanceComponents.forEach {
            it.second.ignoredFiles = it.second.ignoredFiles.filter { file ->
                !PatternString(file, true).matches("backups/")
            }
            LauncherFile.of(it.first.directory, it.first.details).write(it.second)
        }
        LOGGER.info { "Removed backup excluded files from instances" }
    }

    fun upgradeSettings(onStep: (UpgradeStep) -> Unit) {
        LOGGER.info { "Upgrading settings..." }
        onStep(UpgradeStep.UPGRADE_SETTINGS)
        appSettings().version = currVer.toString()
        LOGGER.info { "Upgraded settings" }
    }

    companion object {
        val LOGGER = KotlinLogging.logger {  }
    }
}