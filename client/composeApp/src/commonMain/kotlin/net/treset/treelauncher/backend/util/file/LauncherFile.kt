package net.treset.treelauncher.backend.util.file

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.json.JsonParsable
import net.treset.treelauncher.backend.config.appConfig
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.io.path.isDirectory

class LauncherFile(pathname: String) : File(pathname) {
    @Throws(IOException::class)
    fun read(): ByteArray {
        if (!isFile()) throw IOException("File does not exist: $absolutePath")
        return Files.readAllBytes(toPath())
    }

    @Throws(IOException::class)
    fun readString(): String {
        return String(read())
    }

    fun isChildOf(parent: File): Boolean {
        return parent.isDirectory() && toPath().startsWith(parent.toPath())
    }

    @Throws(IOException::class)
    fun copyTo(dst: LauncherFile, vararg options: CopyOption) {
        copyTo(dst, { true }, *options)
    }

    @Throws(IOException::class)
    fun copyTo(dst: LauncherFile, copyChecker: (String) -> Boolean, vararg options: CopyOption) {
        moveOrCopy(dst, copyChecker, false, *options)
    }

    @Throws(IOException::class)
    fun moveTo(dst: LauncherFile, vararg options: CopyOption) {
        moveTo(dst, { true }, *options)
    }

    @Throws(IOException::class)
    fun moveTo(dst: LauncherFile, copyChecker: (String) -> Boolean, vararg options: CopyOption) {
        moveOrCopy(dst, copyChecker, true, *options)
    }

    @Throws(IOException::class)
    private fun moveOrCopy(dst: LauncherFile, copyChecker: (String) -> Boolean, move: Boolean, vararg options: CopyOption) {
        if (!exists()) throw IOException("File does not exist: $absolutePath")
        Files.createDirectories(dst.parentFile.toPath())
        if (isDirectory()) {
            dst.createDir()
            try {
                Files.walk(Path.of(path)).use { stream ->
                    val exceptions: MutableList<IOException> = ArrayList()
                    val sourceLength = path.length
                    stream.forEach { src: Path ->
                        if (!copyChecker(src.fileName.toString()) || src.toString() == absolutePath) {
                            return@forEach
                        }
                        val destinationF = of(dst.path, src.toString().substring(sourceLength))
                        if(src.isDirectory() && destinationF.isDirectory) {
                            return@forEach
                        }
                        try {
                            Files.copy(src, destinationF.toPath(), *options)
                        } catch (e: IOException) {
                            exceptions.add(e)
                        }
                    }
                    if (exceptions.isNotEmpty()) {
                        throw IOException(
                            "Unable to copy directory: ${exceptions.size} file copies failed: source=$this, destination=$dst",
                            exceptions[0]
                        )
                    }
                }
            } catch (e: IOException) {
                throw IOException("Unable to copy directory: source=$this, destination=$dst", e)
            }
        } else {
            Files.copy(toPath(), dst.toPath(), *options)
        }
        if(move) {
            remove()
        }
    }

    @Throws(IOException::class)
    fun atomicMoveTo(dst: LauncherFile, vararg options: CopyOption) {
        if (!exists()) throw IOException("File does not exist: $absolutePath")
        Files.createDirectories(dst.parentFile.toPath())
        Files.move(toPath(), dst.toPath(), StandardCopyOption.ATOMIC_MOVE, *options)
    }

    @Throws(IOException::class)
    fun write(content: ByteArray) {
        if (!exists()) {
            createFile()
        }
        Files.write(toPath(), content)
    }

    @Throws(IOException::class)
    fun write(content: String) {
        write(content.toByteArray())
    }

    @Throws(IOException::class)
    fun write(content: JsonParsable) {
        write(content.toJson())
    }

    @Throws(IOException::class)
    fun createDir() {
        if (!exists()) {
            Files.createDirectories(toPath())
        }
    }

    @Throws(IOException::class)
    fun createFile() {
        if (!exists()) {
            Files.createDirectories(getAbsoluteFile().getParentFile().toPath())
            Files.createFile(toPath())
        }
    }

    @Throws(IOException::class)
    fun remove() {
        if (!exists()) {
            throw IOException("File does not exist: $this")
        }
        if (isDirectory()) {
            val exceptionQueue = ArrayList<Exception>()
            Files.walk(toPath()).use { pathStream ->
                pathStream.sorted(Comparator.reverseOrder())
                    .map { obj: Path -> obj.toFile() }
                    .forEach { f: File ->
                        try {
                            Files.delete(f.toPath())
                        } catch (e: IOException) {
                            exceptionQueue.add(e)
                        }
                    }
            }
            if (exceptionQueue.isNotEmpty()) {
                throw IOException("Failed to delete directory: $this", exceptionQueue[0])
            }
        } else {
            Files.delete(toPath())
        }
    }

    @get:Throws(IOException::class)
    val isDirEmpty: Boolean
        get() {
            if (!isDirectory()) {
                throw IOException("File is not a directory: $this")
            }
            Files.newDirectoryStream(toPath()).use { dirStream -> return !dirStream.iterator().hasNext() }
        }

    override fun listFiles(): Array<LauncherFile> {
        val files = super.listFiles() ?: return arrayOf()
        return files.map { file -> of(file.path) }.toTypedArray()
    }

    @Throws(IOException::class)
    fun hash(): String {
        val content = read()
        val md: MessageDigest = try {
            MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e) // this doesn't happen
        }
        val encrypted = md.digest(content)
        val encryptedString = StringBuilder(BigInteger(1, encrypted).toString(16))
        for (i in encryptedString.length..31) {
            encryptedString.insert(0, "0")
        }
        return encryptedString.toString()
    }

    fun open() {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
            try {
                desktop.open(this)
            } catch (e: IOException) {
                LOGGER.warn(e) { "Failed to open file: $this" }
            }
        }
    }

    fun getLauncherName(): String {
        return if(isDirectory()) { "${name}/" } else { name }
    }

    fun existsOrNull(): LauncherFile? {
        return if(exists()) this else null
    }

    companion object {
        val LOGGER = KotlinLogging.logger {  }

        fun of(vararg parts: String): LauncherFile {
            val path = StringBuilder()
            for (i in parts.indices) {
                if (parts[i].isBlank()) continue
                path.append(parts[i])
                if (i != parts.size - 1
                    && !parts[i].endsWith(separator)
                    && !parts[i].endsWith("/")
                    && !parts[i].endsWith("\\")
                ) path.append(separator)
            }
            return LauncherFile(path.toString())
        }

        fun of(file: File): LauncherFile {
            return LauncherFile(file.path)
        }

        fun of(file: File, vararg parts: String): LauncherFile {
            val firstPath = file.path
            val allParts = mutableListOf(firstPath)
            allParts.addAll(parts)
            return of(*allParts.toTypedArray())
        }

        fun of(file: File, launcherFile: LauncherFile): LauncherFile {
            return of(file, launcherFile.path)
        }

        fun ofData(vararg parts: String): LauncherFile {
            return of(appConfig().baseDir, *parts)
        }
    }
}
