package net.treset.treelauncher.backend.sync

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.mc_version_loader.util.DownloadStatus
import java.io.IOException

abstract class FileSynchronizer(var callback: SyncCallback?) {
    interface SyncCallback {
        fun set(status: SyncStatus?)
    }

    class SyncStatus(val step: SyncStep, val status: DownloadStatus?)
    enum class SyncStep(val translationKey: String) {
        STARTING("sync.status.starting"),
        COLLECTING("sync.status.collecting"),
        UPLOADING("sync.status.uploading"),
        DOWNLOADING("sync.status.downloading"),
        CREATING("sync.status.creating"),
        FINISHED("sync.status.finished")

    }

    protected fun setStatus(status: SyncStatus) {
        callback?.set(SyncStatus(status.step, status.status))
    }

    @Throws(IOException::class)
    abstract fun upload()
    @Throws(IOException::class)
    abstract fun download()

    companion object {
        protected val LOGGER = KotlinLogging.logger {}
        fun getStringFromType(type: LauncherManifestType, typeConversion: Map<String, LauncherManifestType>): String {
            for ((key, value) in typeConversion) {
                if (value == type) {
                    return key
                }
            }
            throw IllegalArgumentException("Unable to find string for type $type")
        }

        fun getChildType(type: LauncherManifestType): LauncherManifestType {
            return when (type) {
                LauncherManifestType.INSTANCES -> LauncherManifestType.INSTANCE_COMPONENT
                LauncherManifestType.VERSIONS -> LauncherManifestType.VERSION_COMPONENT
                LauncherManifestType.SAVES -> LauncherManifestType.SAVES_COMPONENT
                LauncherManifestType.RESOURCEPACKS -> LauncherManifestType.RESOURCEPACKS_COMPONENT
                LauncherManifestType.MODS -> LauncherManifestType.MODS_COMPONENT
                LauncherManifestType.OPTIONS -> LauncherManifestType.OPTIONS_COMPONENT
                else -> throw IllegalArgumentException("Unable to find child type for type $type")
            }
        }
    }
}
