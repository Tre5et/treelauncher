package net.treset.treelauncher.backend.creation

import net.treset.mcdl.exception.FileDownloadException
import net.treset.mcdl.java.JavaFile
import net.treset.mcdl.java.JavaRuntimes
import net.treset.mcdl.util.OsUtil
import net.treset.treelauncher.backend.data.manifest.JavaComponent
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.FormatStringProvider
import net.treset.treelauncher.backend.util.Status
import java.io.IOException

class JavaComponentCreator(
    parent: ParentManifest,
    onStatus: (Status) -> Unit
) : ComponentCreator<JavaComponent, JavaCreationData>(parent, onStatus) {
    @Throws(IOException::class)
    override fun new(data: JavaCreationData): JavaComponent {
        data.currentComponents.firstOrNull { it.name == data.name }?.let {
            LOGGER.debug { "Matching java component already exists, using instead: name=${data.name}" }
            return use(it)
        }
        return super.new(data)
    }

    @Throws(IOException::class)
    override fun createNew(data: JavaCreationData, statusProvider: CreationProvider): JavaComponent {
        statusProvider.next("Retrieving java version") //TODO: localize
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

        statusProvider.next("Retrieving files") //TODO: localize
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

    @Throws(IOException::class)
    override fun createInherit(data: JavaCreationData, statusProvider: CreationProvider): JavaComponent {
        throw IOException("Java inheritance not supported")
    }

    override val step = JAVA
    override val newTotal = 2
    override val inheritTotal = 0
}

class JavaCreationData(
    name: String,
    val currentComponents: Array<JavaComponent>
): CreationData(name)

val JAVA = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.java() }
