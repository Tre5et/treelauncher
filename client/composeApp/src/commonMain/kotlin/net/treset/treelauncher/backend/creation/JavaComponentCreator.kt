package net.treset.treelauncher.backend.creation

import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.java.*
import net.treset.mc_version_loader.util.DownloadStatus
import net.treset.mc_version_loader.util.OsUtil
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile

class JavaComponentCreator(
    name: String,
    typeConversion: Map<String, LauncherManifestType>,
    componentsManifest: ParentManifest
) : GenericComponentCreator(
    LauncherManifestType.JAVA_COMPONENT,
    null,
    null,
    name,
    typeConversion,
    null,
    null,
    componentsManifest
) {
    init {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.JAVA, null)
    }

    @Throws(ComponentCreationException::class)
    override fun createComponent(): String {
        val result = super.createComponent()
        if (newManifest == null) {
            attemptCleanup()
            throw ComponentCreationException("Failed to create java component: invalid data")
        }
        val java: JavaRuntimes = try {
            MinecraftJava.getJavaRuntimes()
        } catch (e: FileDownloadException) {
            attemptCleanup()
            throw ComponentCreationException("Failed to create java component: failed to download java runtime json", e)
        }
        val osIdentifier: String = try {
            OsUtil.getJavaIdentifier()
        } catch (e: IllegalArgumentException) {
            attemptCleanup()
            throw ComponentCreationException("Failed to create java component: failed to get os identifier", e)
        }
        val os = java[osIdentifier]
        if (os?.entries == null || os.entries.isEmpty()) {
            attemptCleanup()
            throw ComponentCreationException("Failed to create java component: failed to get os runtime")
        }

        val release = os[name]?.get(0)
        if (release?.manifest == null || release.manifest.url == null) {
            attemptCleanup()
            throw ComponentCreationException("Failed to create java component: failed to get release")
        }
        val files: List<JavaFile> = try {
            MinecraftJava.getJavaFiles(release.manifest.url)
        } catch (e: FileDownloadException) {
            throw ComponentCreationException("Failed to create java component: failed to download java file manifest", e)
        }
        val baseDir = LauncherFile(newManifest!!.directory)
        try {
            MinecraftJava.downloadJavaFiles(
                baseDir,
                files
            ) { status: DownloadStatus? ->
                setStatus(
                    CreationStatus(
                        CreationStatus.DownloadStep.JAVA,
                        status
                    )
                )
            }
        } catch (e: FileDownloadException) {
            throw ComponentCreationException("Failed to create java component: failed to download java files", e)
        }
        return result
    }

    @Throws(ComponentCreationException::class)
    override fun useComponent(): String {
        attemptCleanup()
        throw ComponentCreationException("Unable to use java component: unsupported")
    }

    @Throws(ComponentCreationException::class)
    override fun inheritComponent(): String {
        attemptCleanup()
        throw ComponentCreationException("Unable to inherit java component: unsupported")
    }
}
