package dev.treset.treelauncher.backend.creation

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.data.manifest.ParentManifest
import dev.treset.treelauncher.backend.util.*
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.localization.Strings
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.random.Random

abstract class ComponentCreator<T: Component, D: CreationData>(
    val data: D,
    var statusProvider: StatusProvider
) {
    constructor(
        data: D,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    var id = createHash()
    val directory: LauncherFile
        get() = LauncherFile.of(data.parent.directory, "${data.parent.prefix}_${id}")
    val file: LauncherFile
        get() = LauncherFile.of(data.parent.directory, "${data.parent.prefix}_${id}", appConfig().manifestFileName)

    @Throws(IOException::class)
    abstract fun create(): T

    abstract val step: StringProvider
    open val total: Int = 0

    private fun createHash(): String {
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

abstract class NewComponentCreator<T: Component, D: NewCreationData>(
    data: D,
    statusProvider: StatusProvider
): ComponentCreator<T, D>(data, statusProvider) {
    constructor(
        data: D,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun create(): T {
        LOGGER.debug { "Creating new component ${data.name}" }

        val newProvider = statusProvider.subStep(step, total + 2)
        newProvider.next()

        val new = createNew(newProvider)
        new.write()
        data.parent.components += new.id.value
        data.parent.write()

        newProvider.finish()
        return new
    }

    protected abstract fun createNew(
        statusProvider: StatusProvider
    ): T
}

abstract class InheritComponentCreator<T: Component, D: InheritCreationData<T>>(
    data: D,
    statusProvider: StatusProvider
): ComponentCreator<T, D>(data, statusProvider) {
    constructor(
        data: D,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun create(): T {
        LOGGER.debug { "Inheriting component ${data.component.id} -> $id" }

        val inheritProvider = statusProvider.subStep(step, total + 2)
        inheritProvider.next()

        val new = createInherit(inheritProvider)
        data.component.copyData(new)
        new.write()

        inheritProvider.next(Strings.creator.status.message.inheritFiles())
        data.component.directory.listFiles().forEach {
            if(it.name != appConfig().manifestFileName) {
                it.copyTo(LauncherFile.of(new.directory, it.name))
            }
        }

        inheritProvider.finish()

        data.parent.components += new.id.value
        data.parent.write()
        return new
    }

    protected abstract fun createInherit(
        statusProvider: StatusProvider
    ): T
}

abstract class UseComponentCreator<T: Component, D: UseCreationData<T>>(
    data: D,
    statusProvider: StatusProvider
): ComponentCreator<T, D>(data, statusProvider) {
    constructor(
        data: D,
        onStatus: (Status) -> Unit
    ) : this(data, StatusProvider(null, 0, onStatus))

    @Throws(IOException::class)
    override fun create(): T {
        LOGGER.debug { "Using component ${data.component.id}" }
        statusProvider.subStep(step, 1).finish()
        return data.component
    }
}

open class CreationData(
    val parent: ParentManifest
)

open class NewCreationData(
    val name: String,
    parent: ParentManifest
): CreationData(parent)

open class InheritCreationData<T: Component>(
    val name: String,
    val component: T,
    parent: ParentManifest
): CreationData(parent)

open class UseCreationData<T: Component>(
    val component: T,
    parent: ParentManifest
): CreationData(parent)

object CreationStep {
    val STARTING = FormatStringProvider { Strings.creator.status.starting() }
    val FINISHING = FormatStringProvider { Strings.creator.status.finishing() }
}

