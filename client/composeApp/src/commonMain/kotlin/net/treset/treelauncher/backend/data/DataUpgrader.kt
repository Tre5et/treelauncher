package net.treset.treelauncher.backend.data

import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.util.Version
import net.treset.treelauncher.backend.util.exception.FileLoadException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException

class DataUpgrader(
    currentVersion: String,
    previousVersion: String
) {
    private val files = LauncherFiles()
    private val currVer = Version.fromString(currentVersion)
    private val prevVer = Version.fromString(previousVersion)

    enum class UpgradeStep {
        REMOVE_BACKUP_EXCLUDED_FILES,
        UPGRADE_SETTINGS
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
        UpgradeFunction(this::removeBackupExcludedFiles) { currVer >= Version(2, 5, 0) && prevVer < Version(2, 5, 0) },
        UpgradeFunction(this::upgradeSettings) { currVer >= Version(2, 5, 0) && prevVer < Version(2, 5, 0) }
    )

    @Throws(IOException::class)
    fun performUpgrade(onStep: (UpgradeStep) -> Unit) {
        if(currVer <= prevVer) {
            return
        }

        try {
            files.reloadAll()
        } catch (e: FileLoadException) {
            throw IOException("Unable to reload files", e)
        }
        for(upgrade in upgradeMap) {
            upgrade.execute(onStep)
        }
    }

    @Throws(IOException::class)
    fun removeBackupExcludedFiles(onStep: (UpgradeStep) -> Unit) {
        onStep(UpgradeStep.REMOVE_BACKUP_EXCLUDED_FILES)
        files.instanceComponents.forEach {
            it.second.ignoredFiles = it.second.ignoredFiles.filter { file ->
                !PatternString(file, true).matches("backups/")
            }
            LauncherFile.of(it.first.directory, it.first.details).write(it.second)
        }
    }

    fun upgradeSettings(onStep: (UpgradeStep) -> Unit) {
        onStep(UpgradeStep.UPGRADE_SETTINGS)
        appSettings().version = currVer.toString()
    }
}