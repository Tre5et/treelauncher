package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.backend.util.exception.ComponentCreationException
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.string.PatternString
import java.io.IOException
import java.math.BigInteger
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

abstract class GenericComponentCreator(
    var type: LauncherManifestType,
    var uses: LauncherManifest?,
    var inheritsFrom: LauncherManifest?,
    var name: String?,
    var typeConversion: Map<String, LauncherManifestType>?,
    var includedFiles: Array<PatternString>?,
    var details: String?,
    var componentsManifest: LauncherManifest?
) : ComponentCreator {
    protected var newManifest: LauncherManifest? = null
    open var statusCallback: (CreationStatus) -> Unit = {}
    protected var defaultStatus: CreationStatus? = null

    @get:Throws(ComponentCreationException::class)
    override val id: String
        get() = execute()

    @Throws(ComponentCreationException::class)
    fun execute(): String {
        defaultStatus?.let {
            setStatus(it)
        }
        uses?.let {
            return useComponent()
        }
        if (name == null) {
            throw ComponentCreationException("Unable to create ${type.toString().lowercase(Locale.getDefault())} component: invalid parameters")
        }
        return if (inheritsFrom != null) {
            inheritComponent()
        } else createComponent()
    }

    @Throws(ComponentCreationException::class)
    protected open fun createComponent(): String {
        val manifestType = try {
            getManifestType(type, typeConversion?: throw ComponentCreationException("Unable to create ${type.toString().lowercase(Locale.getDefault())} component: invalid parameters"))
        } catch (e: IllegalArgumentException) {
            throw ComponentCreationException("Unable to create ${type.toString().lowercase(Locale.getDefault())} component: unable to get manifest type", e)
        }
        LauncherManifest(
            manifestType,
            typeConversion,
            null,
            details,
            null,
            name,
            includedFiles?.map { s -> s.get() }?.toList(),
            null
        ).let {
            it.id = hash(it)
            newManifest = it
            try {
                writeNewManifest()
            } catch (e: ComponentCreationException) {
                attemptCleanup()
                throw ComponentCreationException("Unable to create ${type.toString().lowercase(Locale.getDefault())} component: unable to write manifest", e)
            }
            LOGGER.debug { "Created ${type.toString().lowercase(Locale.getDefault())} component: id=${it.id}" }
            return it.id
        }
    }

    @Throws(ComponentCreationException::class)
    protected open fun useComponent(): String {
        uses?.let {
            if (it.type != type || it.id == null) {
                throw ComponentCreationException("Unable to use ${type.toString().lowercase(Locale.getDefault())} component: invalid component specified")
            }
            LOGGER.debug { "Using ${type.toString().lowercase(Locale.getDefault())} component: id=${it.id}" }
            return it.id
        }?: throw ComponentCreationException("Unable to use ${type.toString().lowercase(Locale.getDefault())} component: invalid component specified")
    }

    @Throws(ComponentCreationException::class)
    protected open fun inheritComponent(): String {
        inheritsFrom?.let {
            if (it.type != type) {
                throw ComponentCreationException("Unable to inherit ${type.toString().lowercase(Locale.getDefault())} component: invalid component specified")
            }
            val manifestType = getManifestType(type, it.typeConversion)

            LauncherManifest(
                manifestType,
                it.typeConversion,
                null,
                it.details,
                it.prefix,
                name,
                it.includedFiles,
                it.components
            ).let { manifest ->
                manifest.id = hash(manifest)
                newManifest = manifest
                try {
                    writeNewManifest()
                } catch (e: ComponentCreationException) {
                    attemptCleanup()
                    throw ComponentCreationException("Unable to inherit ${type.toString().lowercase(Locale.getDefault())} component: unable to write manifest: id=${manifest.id}", e)
                }
                try {
                    copyFiles(it, manifest)
                } catch (e: ComponentCreationException) {
                    attemptCleanup()
                    throw ComponentCreationException("Unable to inherit ${type.toString().lowercase(Locale.getDefault())} component: unable to copy files: id=${manifest.id}", e)
                }
                LOGGER.debug { "Inherited ${type.toString().lowercase(Locale.getDefault())} component: id=${manifest.id}" }
                return manifest.id
            }
        }?: throw ComponentCreationException("Unable to inherit ${type.toString().lowercase(Locale.getDefault())} component: invalid component specified")
    }

    @Throws(ComponentCreationException::class)
    fun copyFiles(oldManifest: LauncherManifest, newManifest: LauncherManifest) {
        if (!isValid || oldManifest.directory == null || newManifest.directory == null) {
            throw ComponentCreationException("Unable to copy files: invalid parameters")
        }
        try {
            LauncherFile.of(oldManifest.directory)
                .copyTo(
                    LauncherFile.of(newManifest.directory),
                    {
                        filename -> !filename.equals(
                        appConfig().manifestFileName)
                    },
                    StandardCopyOption.REPLACE_EXISTING
                )
        } catch (e: IOException) {
            throw ComponentCreationException("Unable to copy files: unable to copy files", e)
        }
        LOGGER.debug { "Copied files: src=${oldManifest.directory}, dst=${newManifest.directory}"}
    }

    @Throws(ComponentCreationException::class)
    fun writeNewManifest() {
        if (!isValid || newManifest == null) {
            throw ComponentCreationException("Unable to write manifest: invalid parameters")
        }
        newManifest?.let {
            componentsManifest?.let { componentsMan ->
                it.directory = LauncherFile.of(
                    componentsMan.directory,
                    "${componentsMan.prefix}_${it.id}"
                ).path
                try {
                    LauncherFile.of(it.directory, appConfig().manifestFileName).write(it)
                } catch (e: IOException) {
                    throw ComponentCreationException(
                        "Unable to write manifest: unable to write manifest to file: id=${it.id}, path=${it.directory}/${appConfig().manifestFileName}",
                        e
                    )
                }
                val components = componentsMan.components.toMutableList()
                components.add(it.id)
                componentsMan.components = components
                try {
                    LauncherFile.of(componentsMan.directory, parentManifestFileName).write(componentsMan)
                } catch (e: IOException) {
                    throw ComponentCreationException(
                        "Unable to write manifest: unable to write parent manifest to file: id=${it.id}, path=${componentsMan.directory}/${parentManifestFileName}",
                        e
                    )
                }
                if (it.includedFiles != null) {
                    try {
                        LauncherFile.of(it.directory, appConfig().includedFilesDirName).createDir()
                    } catch (e: IOException) {
                        throw ComponentCreationException("Unable to write manifest: unable to create included files directory: id=${it.id}, path=${it.directory}/${appConfig().includedFilesDirName}")
                    }
                }
                LOGGER.debug {
                    "Wrote manifest: path=${it.directory}/${appConfig().manifestFileName}"
                }
            }
        }
    }

    open fun attemptCleanup(): Boolean {
        LOGGER.debug { "Attempting cleanup" }
        var success = true
        newManifest?.let {
            componentsManifest?.let { componentsMan ->
                if (it.directory != null) {
                    val directory: LauncherFile = LauncherFile.of(it.directory)
                    if (directory.isDirectory()) {
                        try {
                            directory.remove()
                            LOGGER.debug { "Cleaned up manifest: path=${it.directory}" }
                        } catch (e: IOException) {
                            LOGGER.warn(e) { "Unable to cleanup: unable to delete directory: continuing: path=${it.directory}" }
                            success = false
                        }
                    }
                }

                if (componentsMan.components != null && it.id != null && componentsMan.components.contains(
                        it.id
                    )
                ) {
                    val components: MutableList<String> = ArrayList<String>(componentsMan.components)
                    components.remove(it.id)
                    componentsMan.components = components
                    try {
                        LauncherFile.of(componentsMan.directory, parentManifestFileName).write(componentsMan)
                        LOGGER.debug { "Cleaned up parent manifest: id=${it.id}" }
                    } catch (e: IOException) {
                        LOGGER.warn(e) { "Unable to cleanup: unable to write parent manifest to file: continuing: id=${it.id} path=${componentsMan.directory}/${parentManifestFileName}" }
                        success = false
                    }
                }
            }
        }

        LOGGER.debug { if (success) "Cleanup successful" else "Cleanup unsuccessful" }
        return success
    }

    protected open val parentManifestFileName: String
        get() = appConfig().manifestFileName

    @Throws(IllegalArgumentException::class)
    fun getManifestType(type: LauncherManifestType, typeConversion: Map<String, LauncherManifestType>): String {
        for ((key, value) in typeConversion) {
            if (value == type) {
                return key
            }
        }
        throw IllegalArgumentException("Unable to get manifest type: no type found: type=${type.toString().lowercase(Locale.getDefault())}")
    }

    private val isValid: Boolean
        get() = componentsManifest?.let {
            isComponentManifest && it.directory != null && it.prefix != null
        }?: false
    private val isComponentManifest: Boolean
        get() = componentsManifest?.let {
            it.type == LauncherManifestType.INSTANCES || it.type == LauncherManifestType.OPTIONS || it.type == LauncherManifestType.VERSIONS || it.type == LauncherManifestType.RESOURCEPACKS || it.type == LauncherManifestType.SAVES || it.type == LauncherManifestType.MODS || it.type == LauncherManifestType.JAVAS
        }?: false

    fun setStatus(status: CreationStatus) {
        statusCallback(status)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        fun hash(source: Any?): String {
            val md = try {
                MessageDigest.getInstance("SHA-1")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e) // this doesn't happen
            }
            val encrypted: ByteArray = md.digest((source.toString() + System.nanoTime()).toByteArray())
            val encryptedString = StringBuilder(BigInteger(1, encrypted).toString(16))
            for (i in encryptedString.length..31) {
                encryptedString.insert(0, "0")
            }
            return encryptedString.toString()
        }
    }
}
