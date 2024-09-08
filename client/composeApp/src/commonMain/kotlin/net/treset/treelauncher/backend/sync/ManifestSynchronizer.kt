package net.treset.treelauncher.backend.sync

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.json.SerializationException
import net.treset.mcdl.util.DownloadStatus
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.FormatString
import net.treset.treelauncher.backend.util.string.UrlString
import java.io.File
import java.io.IOException
import java.nio.file.Files

open class ManifestSynchronizer(var manifest: ComponentManifest, protected var files: LauncherFiles, callback: SyncCallback?) :
    FileSynchronizer(callback) {
    @Throws(IOException::class)
    override fun upload() {
        if (SyncService.isSyncing(manifest)) {
            uploadExisting()
        } else {
            uploadNew()
        }
    }

    @Throws(IOException::class)
    override fun download() {
        if (SyncService.isSyncing(manifest)) {
            downloadExisting()
        } else {
            downloadNew()
        }
    }

    @Throws(IOException::class)
    protected fun uploadExisting() {
        setStatus(SyncStatus(SyncStep.COLLECTING, null))
        val currentData = currentComponentData
        val newData = calculateComponentData(currentData.version)
        val difference = compareHashes(
            ComponentData.HashEntry("", currentData.hashTree),
            ComponentData.HashEntry("", newData.hashTree),
            LauncherFile.of("./")
        )
        uploadDifference(difference)
        completeUpload(newData)
        setStatus(SyncStatus(SyncStep.FINISHED, null))
    }

    @Throws(IOException::class)
    protected fun uploadNew() {
        setStatus(SyncStatus(SyncStep.STARTING, null))
        LOGGER.debug { "Sync file not found, creating new sync file" }
        setStatus(SyncStatus(SyncStep.COLLECTING, null))
        val data = calculateComponentData(0)
        setStatus(SyncStatus(SyncStep.UPLOADING, null))
        val service = SyncService()
        service.newComponent(SyncService.convertType(manifest.type), manifest.id)
        uploadAll(data)
        LOGGER.debug { "Completing upload..." }
        val version = completeUpload(data)
        LOGGER.debug { "Upload complete: version=$version" }
        setStatus(SyncStatus(SyncStep.FINISHED, null))
    }

    @Throws(IOException::class)
    protected fun downloadExisting() {
        val data = currentComponentData
        downloadDifference(data.version)
    }

    @Throws(IOException::class)
    protected open fun downloadNew() {
        val dir = File(manifest.directory)
        if (!dir.exists()) {
            Files.createDirectories(dir.toPath())
        }
        downloadDifference(0)
        val parent: ParentManifest = parentManifest
        LOGGER.debug { "Adding component to parent manifest" }
        parent.components.add(manifest.id)
        var fileName: String = appConfig().manifestFileName
        LauncherFile.of(parent.directory, fileName).write(parent)
    }

    @get:Throws(IOException::class)
    protected val parentManifest: ParentManifest
        get() = when (manifest.type) {
            LauncherManifestType.SAVES_COMPONENT -> {
                files.savesManifest
            }

            LauncherManifestType.MODS_COMPONENT -> {
                files.modsManifest
            }

            LauncherManifestType.RESOURCEPACKS_COMPONENT -> {
                files.resourcepackManifest
            }

            LauncherManifestType.OPTIONS_COMPONENT -> {
                files.optionsManifest
            }

            LauncherManifestType.INSTANCE_COMPONENT -> {
                files.instanceManifest
            }

            else -> throw IOException("Invalid component type")
        }

    @Throws(IOException::class)
    protected open fun uploadAll(data: ComponentData) {
        val service = SyncService()
        uploadDirectory(
            data.hashTree,
            SyncService.convertType(manifest.type),
            manifest.id,
            manifest.directory,
            "",
            0,
            data.fileAmount,
            service
        )
    }

    @Throws(IOException::class)
    protected fun uploadDirectory(
        entries: List<ComponentData.HashEntry>,
        type: String,
        id: String,
        basePath: String,
        filePath: String,
        currentAmount: Int,
        totalAmount: Int,
        service: SyncService
    ): Int {
        var current = currentAmount
        for (child in entries) {
            child.children?.let {
                current = uploadDirectory(
                    it,
                    type,
                    id,
                    basePath,
                    LauncherFile.of(filePath, child.path).path,
                    current,
                    totalAmount,
                    service
                )
            }?: {
                LOGGER.debug { "Uploading file: ${child.path}" }
                current++
                setStatus(SyncStatus(SyncStep.UPLOADING, DownloadStatus(current, totalAmount, child.path)))
                val file: LauncherFile = LauncherFile.of(basePath, filePath, child.path)
                val content: ByteArray = file.read()
                service.uploadFile(type, id, file.path, content)
            }
        }
        return current
    }

    @Throws(IOException::class)
    protected open fun uploadDifference(difference: List<String>) {
        if (difference.isEmpty()) {
            LOGGER.debug { "No difference found" }
            return
        }
        setStatus(SyncStatus(SyncStep.UPLOADING, null))
        val syncService = SyncService()
        var index = 0
        for (path in difference) {
            index++
            setStatus(SyncStatus(SyncStep.UPLOADING, DownloadStatus(index, difference.size, path)))
            LOGGER.debug { "Difference: $path" }
            val file: LauncherFile = LauncherFile.of(manifest.directory, path)
            val content: ByteArray = if (file.isFile()) {
                file.read()
            } else {
                byteArrayOf()
            }
            syncService.uploadFile(SyncService.convertType(manifest.type), manifest.id, path, content)
        }
    }

    @Throws(IOException::class)
    protected fun completeUpload(newData: ComponentData): Int {
        val version = SyncService().complete(SyncService.convertType(manifest.type), manifest.id)
        newData.version = version
        syncFile.write(newData)
        return version
    }

    protected fun compareHashes(
        oldEntry: ComponentData.HashEntry,
        newEntry: ComponentData.HashEntry,
        path: LauncherFile
    ): List<String> {
        val difference = ArrayList<String>()
        if (oldEntry.children == null && newEntry.children == null) {
            if (oldEntry.hash != newEntry.hash) {
                LOGGER.debug { "Adding changed file: $path" }
                difference.add(path.path)
            }
        } else if (oldEntry.children != null && newEntry.children != null) {
            var j = 0
            for (i in oldEntry.children!!.indices) {
                var found = false
                for (k in j until newEntry.children!!.size) {
                    if (oldEntry.children!![i].path == newEntry.children!![k].path) {
                        found = true
                        for (l in j until k) {
                            LOGGER.debug { "Adding added file: $path/${newEntry.children!![l].path}" }
                            difference.addAll(
                                getAllChildren(
                                    newEntry.children!![l],
                                    LauncherFile.of(path, newEntry.children!![l].path)
                                )
                            )
                        }
                        difference.addAll(
                            compareHashes(
                                oldEntry.children!![i],
                                newEntry.children!![k],
                                LauncherFile.of(path, oldEntry.children!![i].path)
                            )
                        )
                        j = k + 1
                        break
                    }
                }
                if (!found) {
                    LOGGER.debug { "Adding deleted file: ${oldEntry.children!![i].path}" }
                    difference.addAll(
                        getAllChildren(
                            oldEntry.children!![i],
                            LauncherFile.of(path, oldEntry.children!![i].path)
                        )
                    )
                }
            }
        } else if (oldEntry.children == null) {
            LOGGER.debug { "Adding file that became folder: ${newEntry.path}" }
            difference.add(oldEntry.path)
            difference.addAll(getAllChildren(newEntry, path))
        } else {
            LOGGER.debug { "Adding folder that became file: ${newEntry.path}" }
            difference.addAll(getAllChildren(oldEntry, path))
            difference.add(newEntry.path)
        }
        return difference
    }

    private fun getAllChildren(entry: ComponentData.HashEntry, path: LauncherFile): List<String> {
        val children = ArrayList<String>()
        if (entry.children == null) {
            children.add(path.path)
        } else {
            for (child in entry.children!!) {
                children.addAll(getAllChildren(child, LauncherFile.of(path, child.path)))
            }
        }
        return children
    }

    @get:Throws(IOException::class)
    protected val currentComponentData: ComponentData
        get() {
            val syncFile: LauncherFile = LauncherFile.of(manifest.directory, appConfig().syncFileName)
            if (!syncFile.exists()) {
                throw IOException("Sync file not found")
            }
            val componentData: String = syncFile.readString()
            return try {
                ComponentData.fromJson(componentData)
            } catch (e: SerializationException) {
                throw IOException("Failed to parse sync file", e)
            }
        }

    @Throws(IOException::class)
    protected fun downloadDifference(currentVersion: Int) {
        setStatus(SyncStatus(SyncStep.COLLECTING, null))
        val response = SyncService()[SyncService.convertType(manifest.type), manifest.id, currentVersion]
        if (response.version == currentVersion) {
            LOGGER.debug { "Component is up to date" }
            setStatus(SyncStatus(SyncStep.FINISHED, null))
            return
        }
        LOGGER.debug { "Downloading component: version=$currentVersion -> $response.version" }
        setStatus(SyncStatus(SyncStep.DOWNLOADING, null))
        downloadFiles(response.difference)
        LOGGER.debug { "Updating sync file" }
        setStatus(SyncStatus(SyncStep.COLLECTING, null))
        updateSyncFile(response.version)
        LOGGER.debug { "Update complete" }
        setStatus(SyncStatus(SyncStep.FINISHED, null))
    }

    @Throws(IOException::class)
    protected open fun downloadFiles(difference: Array<String>) {
        val service = SyncService()
        val type = SyncService.convertType(manifest.type)
        val basePath: String = manifest.directory
        for ((amount, rawPath) in difference.withIndex()) {
            val path: String = try {
                UrlString.decoded(rawPath).get()
            } catch (e: FormatString.FormatException) {
                LOGGER.warn { "Unable to decode filepath: $rawPath, this may be due to no url encoding being used, continuing with possibly encoded path, error: $e" }
                rawPath
            }
            LOGGER.debug { "Downloading file: $path" }
            setStatus(SyncStatus(SyncStep.DOWNLOADING, DownloadStatus(amount + 1, difference.size, path)))
            val content = service.downloadFile(type, manifest.id, path)
            val file: LauncherFile = LauncherFile.of(basePath, path)
            if (content.isEmpty()) {
                if (file.isFile()) {
                    LOGGER.debug { "Deleting file or dir: $path" }
                    file.remove()
                }
            } else {
                if (!file.isFile()) {
                    if (file.isDirectory()) {
                        file.remove()
                    }
                    file.createFile()
                }
                file.write(content)
            }
        }
    }

    @Throws(IOException::class)
    protected fun updateSyncFile(version: Int): ComponentData {
        val data = calculateComponentData(version)
        syncFile.write(data)
        return data
    }

    private val syncFile: LauncherFile
        get() = LauncherFile.of(manifest.directory, appConfig().syncFileName)

    @Throws(IOException::class)
    protected fun calculateComponentData(version: Int): ComponentData {
        LOGGER.debug { "Collecting component data for component: ${manifest.id}" }
        val startTime = System.currentTimeMillis()
        val componentDir = File(manifest.directory)
        resetCount()
        val result = hashDirectoryContents(componentDir)
        LOGGER.debug { "Component data collected in ${System.currentTimeMillis() - startTime}ms" }
        return ComponentData(version, resetCount(), result)
    }

    @Throws(IOException::class)
    protected fun hashDirectoryContents(dir: File): List<ComponentData.HashEntry> {
        val children = dir.listFiles { _: File?, name: String -> name != "data.sync" && name != ".included_files_old" } ?: return listOf()
        val files: MutableList<Pair<File, Int>> = mutableListOf()
        val hashTree: MutableList<ComponentData.HashEntry> = mutableListOf()
        for (i in children.indices) {
            files.add(Pair(children[i], i))
        }
        val exceptions: MutableList<IOException> = mutableListOf()
        files.parallelStream().forEach { file: Pair<File, Int> ->
            if (file.first.isDirectory()) {
                val result: List<ComponentData.HashEntry> = try {
                    hashDirectoryContents(file.first)
                } catch (e: IOException) {
                    exceptions.add(e)
                    return@forEach
                }
                hashTree[file.second] = ComponentData.HashEntry(file.first.getName(), result)
            } else if (file.first.isFile()) {
                addCount(1)
                try {
                    hashTree[file.second] = ComponentData.HashEntry(file.first.getName(), LauncherFile.of(file.first).hash())
                } catch (e: IOException) {
                    exceptions.add(e)
                }
            }
        }
        if (exceptions.isNotEmpty()) {
            throw exceptions[0]
        }
        return hashTree
    }

    private var count = 0

    protected fun addCount(amount: Int) {
        count += amount
    }

    protected fun resetCount(): Int {
        val count = count
        this.count = 0
        return count
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
