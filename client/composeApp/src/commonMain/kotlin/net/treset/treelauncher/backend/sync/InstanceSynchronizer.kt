package net.treset.treelauncher.backend.sync

import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.fabric.FabricLoader
import net.treset.mc_version_loader.fabric.FabricProfile
import net.treset.mc_version_loader.fabric.FabricVersionDetails
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.mc_version_loader.launcher.LauncherVersionDetails
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails
import net.treset.mc_version_loader.util.DownloadStatus
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.creation.VersionCreator
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.data.LauncherFiles
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException
import java.util.*

class InstanceSynchronizer : ManifestSynchronizer {
    var instanceData: InstanceData
    var isUpdateEverything = false

    constructor(
        instanceData: InstanceData,
        files: LauncherFiles,
        callback: SyncCallback?
    ) : super(instanceData.instance.first, files, callback) {
        this.instanceData = instanceData
    }

    constructor(
        instanceData: InstanceData,
        files: LauncherFiles,
        updateEverything: Boolean,
        callback: SyncCallback?
    ) : super(instanceData.instance.first, files, callback) {
        this.instanceData = instanceData
        isUpdateEverything = updateEverything
    }

    @Throws(IOException::class)
    override fun upload() {
        super.upload()
        uploadDependency(instanceData.savesComponent)
        uploadDependency(instanceData.resourcepacksComponent)
        uploadDependency(instanceData.optionsComponent)
        instanceData.modsComponent?.let {
            uploadDependency(it.first)
        }
    }

    @Throws(IOException::class)
    override fun uploadAll(data: ComponentData) {
        uploadVersionFile()
        super.uploadAll(data)
    }

    @Throws(IOException::class)
    override fun uploadDifference(difference: List<String>) {
        uploadVersionFile()
        super.uploadDifference(difference)
    }

    @Throws(IOException::class)
    protected fun downloadFiles(difference: List<String>) {
        var detailsFileName: String? = null
        val newDifference: MutableList<String> = mutableListOf()
        for (file in difference) {
            if (file == "version.json") {
                try {
                    downloadVersion()
                } catch (e: Exception) {
                    throw IOException("Failed to download version file", e)
                }
            } else if (file == appConfig().manifestFileName || file == instanceData.instance.first.details) {
                if (detailsFileName == null) {
                    detailsFileName = downloadDetails()
                }
            } else {
                newDifference.add(file)
            }
        }
        if (detailsFileName != null) {
            LauncherFile.of(instanceData.instance.first.directory, detailsFileName)
                .write(instanceData.instance.second)
            newDifference.remove(detailsFileName)
        }
        super.downloadFiles(newDifference.toTypedArray())
    }

    @Throws(IOException::class)
    protected fun downloadDetails(): String {
        val details = downloadManifest()
        val service = SyncService()
        val out = service.downloadFile("instance", instanceData.instance.first.id, details)
        val newDetails: LauncherInstanceDetails = LauncherInstanceDetails.fromJson(String(out))
        instanceData.instance.second.features = newDetails.features
        instanceData.instance.second.jvmArguments = newDetails.jvmArguments
        instanceData.instance.second.ignoredFiles = newDetails.ignoredFiles
        instanceData.instance.second.lastPlayed = newDetails.lastPlayed
        instanceData.instance.second.totalTime = newDetails.totalTime
        instanceData.instance.second.savesComponent = downloadDependency(newDetails.savesComponent, LauncherManifestType.SAVES_COMPONENT)
        instanceData.instance.second.resourcepacksComponent = downloadDependency(newDetails.resourcepacksComponent, LauncherManifestType.RESOURCEPACKS_COMPONENT)
        instanceData.instance.second.optionsComponent = downloadDependency(newDetails.optionsComponent, LauncherManifestType.OPTIONS_COMPONENT)
        instanceData.instance.second.modsComponent = downloadDependency(newDetails.modsComponent, LauncherManifestType.MODS_COMPONENT)
        return details
    }

    @Throws(IOException::class)
    protected fun downloadManifest(): String {
        val service = SyncService()
        val out = service.downloadFile(
            "instance",
            instanceData.instance.first.id,
            appConfig().manifestFileName
        )
        val manifest = LauncherManifest.fromJson(String(out))
        LauncherFile.of(instanceData.instance.first.directory, appConfig().manifestFileName).write(manifest)
        return manifest.details
    }

    @Throws(IOException::class)
    protected fun downloadDependency(id: String, type: LauncherManifestType): String {
        val parentManifest: LauncherManifest
        val otherComponents: Array<LauncherManifest>
        val currentManifest: LauncherManifest?
        when (type) {
            LauncherManifestType.SAVES_COMPONENT -> {
                parentManifest = files.savesManifest
                otherComponents = files.savesComponents
                currentManifest = instanceData.savesComponent
            }

            LauncherManifestType.RESOURCEPACKS_COMPONENT -> {
                parentManifest = files.resourcepackManifest
                otherComponents = files.resourcepackComponents
                currentManifest = instanceData.resourcepacksComponent
            }

            LauncherManifestType.OPTIONS_COMPONENT -> {
                parentManifest = files.optionsManifest
                otherComponents = files.optionsComponents
                currentManifest = instanceData.optionsComponent
            }

            LauncherManifestType.MODS_COMPONENT -> {
                parentManifest = files.modsManifest
                otherComponents = files.modsComponents.map{it.first}.toTypedArray()
                currentManifest = instanceData.modsComponent?.first
            }

            else -> throw IOException("Unknown component type: $type")
        }
        if (currentManifest == null || id != currentManifest.id) {
            val existingManifest: LauncherManifest? = otherComponents.firstOrNull { it.id == id }
            if (existingManifest != null) {
                if (isUpdateEverything) {
                    val synchronizer = ManifestSynchronizer(existingManifest, files, callback)
                    synchronizer.download()
                }
            } else {
                val fakeManifest = LauncherManifest(
                    getStringFromType(type, files.launcherDetails.typeConversion),
                    files.launcherDetails.typeConversion,
                    id,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                fakeManifest.directory = LauncherFile.of(parentManifest.directory, "${parentManifest.prefix}_$id").path
                val synchronizer = ManifestSynchronizer(fakeManifest, files, callback)
                synchronizer.download()
            }
        }
        return id
    }

    @Throws(IOException::class, ComponentCreationException::class, FileDownloadException::class)
    protected fun downloadVersion() {
        val service = SyncService()
        setStatus(SyncStatus(SyncStep.DOWNLOADING, DownloadStatus(0, 0, "version.json", false)))
        val details: LauncherVersionDetails = LauncherVersionDetails.fromJson(
            String(service.downloadFile("instance", instanceData.instance.first.id, "version.json")),
            LauncherVersionDetails::class.java
        )
        val creator: VersionCreator
        if (details.versionId != instanceData.versionComponents[0].second.versionId) {
            creator = when (details.versionType) {
                "vanilla" -> {
                    getVanillaCreator(details)
                }
                "fabric" -> {
                    getFabricCreator(details)
                }
                else -> {
                    throw IOException("Getting version returned unknown version type: ${details.versionType}")
                }
            }
            creator.statusCallback = { status -> setStatus(SyncStatus(SyncStep.CREATING, status.downloadStatus)) }
            val id: String = creator.execute()
            instanceData.instance.second.versionComponent = id
        }
    }

    @Throws(IOException::class, FileDownloadException::class)
    protected fun getVanillaCreator(details: LauncherVersionDetails): VersionCreator {
        val version: MinecraftVersion = MinecraftGame.getReleases()
            .firstOrNull { it.id == details.versionId || it.id == details.versionNumber }?:
            throw IOException("Failed to find version: " + details.versionId)
        val url: String = version.url
        val versionDetails: MinecraftVersionDetails = MinecraftGame.getVersionDetails(url)
        return VersionCreator(
            instanceData.instance.first.typeConversion,
            files.versionManifest,
            versionDetails,
            files,
            LauncherFile.of(files.mainManifest.directory, files.launcherDetails.librariesDir)
        )
    }

    @Throws(IOException::class, FileDownloadException::class)
    protected fun getFabricCreator(details: LauncherVersionDetails): VersionCreator {
        val version: FabricVersionDetails =
            FabricLoader.getFabricVersionDetails(details.versionNumber, details.loaderVersion)
        val profile: FabricProfile =
            FabricLoader.getFabricProfile(details.versionNumber, details.loaderVersion)
        return VersionCreator(
            instanceData.instance.first.typeConversion,
            files.versionManifest,
            version,
            profile,
            files,
            LauncherFile.of(files.mainManifest.directory, files.launcherDetails.librariesDir)
        )
    }

    @Throws(IOException::class)
    protected fun uploadDependency(manifest: LauncherManifest?) {
        if (manifest != null && (isUpdateEverything || !SyncService.isSyncing(manifest))) {
            val synchronizer = ManifestSynchronizer(manifest, files, callback)
            synchronizer.upload()
        }
    }

    @Throws(IOException::class)
    protected fun uploadVersionFile() {
        val details = LauncherVersionDetails(
            instanceData.versionComponents[0].second.versionNumber,
            instanceData.versionComponents[0].second.versionType,
            instanceData.versionComponents[0].second.loaderVersion,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            instanceData.versionComponents[0].second.versionId
        )
        val service = SyncService()
        setStatus(SyncStatus(SyncStep.UPLOADING, DownloadStatus(0, 0, "version.json", false)))
        service.uploadFile(
            "instance",
            instanceData.instance.first.id,
            "version.json",
            details.toJson().toByteArray()
        )
    }
}
