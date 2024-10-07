package net.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.util.DownloadStatus
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.manifest.Component
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.*
import net.treset.treelauncher.backend.util.file.LauncherFile
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.random.Random

abstract class ComponentCreator<T: Component, D: CreationData>(
    val parent: ParentManifest,
    val onStatus: (Status) -> Unit

) {
    var id = createHash()
    val directory: LauncherFile
        get() = LauncherFile.of(parent.directory, "${parent.prefix}_${id}")
    val file: LauncherFile
        get() = LauncherFile.of(parent.directory, "${parent.prefix}_${id}", appConfig().manifestFileName)

    abstract val step: StringProvider

    abstract val newTotal: Int

    @Throws(IOException::class)
    open fun new(
        data: D
    ): T {
        LOGGER.debug { "Creating new component ${data.name}" }

        val statusProvider = CreationProvider(step, newTotal + 2, onStatus)

        statusProvider.next("Creating component $id") //TODO: make localized

        val new = createNew(data, statusProvider)
        new.write()
        parent.components += new.id
        parent.write()

        statusProvider.finish("Done") //TODO: make localized
        return new
    }

    abstract fun createNew(
        data: D,
        statusProvider: CreationProvider
    ): T

    abstract val inheritTotal: Int

    @Throws(IOException::class)
    open fun inherit(
        component: T,
        data: D
    ): T {
        LOGGER.debug { "Inheriting component ${component.id} -> $id" }

        val statusProvider = CreationProvider(step, inheritTotal + 2, onStatus)

        statusProvider.next("Inheriting ${component.name}") // TODO: make localized
        val new = createInherit(data, statusProvider)
        component.copyData(new)
        new.write()

        statusProvider.next("Copying files") // TODO: make localized
        component.directory.listFiles().forEach {
            if(it.name != appConfig().manifestFileName) {
                it.copyTo(LauncherFile.of(new.directory, it.name))
            }
        }

        statusProvider.finish("Done") // TODO: make localized

        parent.components += new.id
        parent.write()
        return new
    }

    abstract fun createInherit(
        data: D,
        statusProvider: CreationProvider
    ): T

    open fun use(
        component: T
    ): T {
        LOGGER.debug { "Using component ${component.id}" }
        onStatus(Status(step, DetailsProvider("Done", 1, 1))) // TODO: make localized
        return component
    }

    fun createHash(): String {
        val md = try {
            MessageDigest.getInstance("SHA-1")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e) // this doesn't happen
        }
        val encrypted: ByteArray = md.digest((Random(System.nanoTime()).nextBytes(256)))
        val encryptedString = StringBuilder(BigInteger(1, encrypted).toString(16))
        for (i in encryptedString.length..31) {
            encryptedString.insert(0, "0")
        }
        return encryptedString.toString()
    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
    }
}

open class CreationData(
    val name: String
)

object CreationStep {
    val STARTING = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.starting() }
    val FINISHING = FormatStringProvider { net.treset.treelauncher.localization.strings().creator.status.finishing() }
}

class CreationProvider(
    val step: StringProvider,
    var total: Int,
    val onStatus: (Status) -> Unit
) {
    var index = 1

    fun next(
        message: () -> String
    ) = onStatus(Status(step, DetailsProvider(message, index++, if(total >= index) total else index), index / total.toFloat()))

    fun next(
        message: String
    ) = next { message }

    fun download(status: DownloadStatus, before: Int, after: Int) {
        index = status.currentAmount + before
        total = status.totalAmount + before + after
        onStatus(Status(step, DetailsProvider(status.currentFile, index, total), index / total.toFloat()))
    }

    fun finish(
        message: () -> String
    ) = onStatus(Status(step, DetailsProvider(message, total, total), 1f))

    fun finish(
        message: String
    ) = finish { message }

    fun subStep(
        step: StringProvider,
        total: Int
    ) = CreationProvider(step, total, onStatus)
}