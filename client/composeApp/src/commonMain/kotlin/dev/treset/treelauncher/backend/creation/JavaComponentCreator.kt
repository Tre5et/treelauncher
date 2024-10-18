package dev.treset.treelauncher.backend.creation

import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.java.JavaFile
import dev.treset.mcdl.java.JavaRuntimes
import dev.treset.mcdl.util.OsUtil
import dev.treset.treelauncher.backend.data.manifest.JavaComponent
import dev.treset.treelauncher.backend.data.manifest.ParentManifest
import dev.treset.treelauncher.backend.util.FormatStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.localization.Strings
import java.io.IOException

class JavaComponentCreator(
    data: JavaCreationData,
    statusProvider: StatusProvider
) : NewComponentCreator<JavaComponent, JavaCreationData>(data, statusProvider) {
    constructor(
        data: JavaCreationData,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun create(): JavaComponent {
        data.currentComponents.firstOrNull { it.name == data.name }?.let {
            LOGGER.debug { "Matching java component already exists, using instead: name=${data.name}" }
            return it
        }
        return super.create()
    }

    @Throws(IOException::class)
    override fun createNew(statusProvider: StatusProvider): JavaComponent {
        statusProvider.next()
        val java = try {
            JavaRuntimes.get()
        } catch (e: FileDownloadException) {
            throw IOException("Failed to create java component: failed to download java runtime json", e)
        }
        val osIdentifier: String = try {
            OsUtil.getJavaIdentifier()
        } catch (e: IllegalArgumentException) {
            throw IOException("Failed to create java component: failed to get os identifier", e)
        }
        val os = java[osIdentifier]
        if (os?.entries == null || os.entries.isEmpty()) {
            throw IOException("Failed to create java component: failed to get os runtime")
        }
        val release = os[data.name]?.get(0)
        if (release?.manifest == null || release.manifest.url == null) {
            throw IOException("Failed to create java component: failed to get release")
        }

        statusProvider.next()
        val files = try {
            JavaFile.getAll(release.manifest.url)
        } catch (e: FileDownloadException) {
            throw IOException("Failed to create java component: failed to download java file manifest", e)
        }

        try {
            JavaFile.downloadAll(
                files,
                directory
            ) {
                statusProvider.download(it, 3, 1)
            }
        } catch (e: FileDownloadException) {
            throw IOException("Failed to create java component: failed to download java files", e)
        }

        return JavaComponent(
            id = id,
            name = data.name,
            file = file
        )
    }

    override val step = JAVA
    override val total = 2
}

class JavaCreationData(
    name: String,
    val currentComponents: Array<JavaComponent>,
    parent: ParentManifest
): NewCreationData(name, parent)

val JAVA = FormatStringProvider { Strings.creator.status.java() }
