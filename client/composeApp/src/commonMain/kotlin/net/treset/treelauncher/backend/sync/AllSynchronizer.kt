package net.treset.treelauncher.backend.sync

import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.util.exception.FileLoadException
import java.io.IOException

class AllSynchronizer(files: LauncherFiles, callback: SyncCallback?) : FileSynchronizer(callback) {
    private val files: LauncherFiles

    init {
        this.files = files
    }

    @Throws(IOException::class)
    override fun upload() {
        synchronize(true)
    }

    @Throws(IOException::class)
    override fun download() {
        synchronize(false)
    }

    @Throws(IOException::class)
    private fun synchronize(upload: Boolean) {
        val exceptions: MutableList<IOException> = mutableListOf()
        //TODO: Make parallel
        files.instanceComponents.forEach { details ->
            if (!SyncService.isSyncing(details.first)) {
                return@forEach
            }
            val data = try {
                InstanceData.of(details, files)
            } catch (e: FileLoadException) {
                exceptions.add(IOException(e))
                return@forEach
            }
            val synchronizer = InstanceSynchronizer(data, files, callback)
            try {
                if (upload) {
                    synchronizer.upload()
                } else {
                    synchronizer.download()
                }
            } catch (e: IOException) {
                exceptions.add(e)
            }
        }
        val manifests: MutableList<LauncherManifest> = mutableListOf()
        manifests.addAll(files.savesComponents)
        manifests.addAll(files.resourcepackComponents)
        manifests.addAll(files.optionsComponents)
        manifests.addAll(files.modsComponents.map{it.first}.toList())
        manifests.parallelStream().forEach { manifest: LauncherManifest ->
            if (!SyncService.isSyncing(manifest)) {
                return@forEach
            }
            val synchronizer = ManifestSynchronizer(manifest, files, callback)
            try {
                if (upload) {
                    synchronizer.upload()
                } else {
                    synchronizer.download()
                }
            } catch (e: IOException) {
                exceptions.add(e)
            }
        }
        if (exceptions.isNotEmpty()) {
            throw exceptions[0]
        }
    }
}
