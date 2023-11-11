package net.treset.treelauncher.updater

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Files
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import kotlin.io.path.Path


var applicationPath: String? = null
var openGui = false

fun main(args: Array<String>) {
    println("Starting updater...")

    parseArgs(args)

    openGui()

    try {
        readUpdate()
    } catch (e: IOException) {
        println(e)
        File("updater.json").writeText(Json.encodeToString(UpdaterStatus(Status.FAILURE, "Failed to read update file.", listOf(e.stackTraceToString()))))
        closeGui()
        return
    }

    try {
        File("updater.json").writeText(Json.encodeToString(UpdaterStatus(Status.UPDATING)))
        File("updater.json").writeText(Json.encodeToString(executeUpdate()))
    } catch (e: IOException) {
        println(e)
    }

    restartApplication()
    closeGui()
}

fun parseArgs(args: Array<String>) {
    for(arg in args) {
        if(arg.startsWith("-r")) {
            applicationPath = arg.substring(2)
        } else if(arg.startsWith("-gui")) {
            openGui = true
        }
    }
}

var frame: JFrame? = null
fun openGui() {
    if(!openGui) {
        return
    }

    val label = JTextArea("Starting updater...\n\r")
    label.setBounds(0, 0, 600, 300)
    System.setOut(PrintStream(object : OutputStream() {
        @Throws(IOException::class)
        override fun write(b: Int) {
            val c = b.toChar()
            label.text += c
        }
    }))


    frame = JFrame("Updater")
    frame?.let {
        it.setSize(600, 300)
        it.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
        it.layout = null

        it.add(JScrollPane(label))

        it.isVisible = true
    }
}

fun closeGui() {
    frame?.let {
        it.isVisible = false
        it.dispose()
    }
}

fun executeUpdate() : UpdaterStatus {
    val backedUpFiles = mutableListOf<File>()
    val exceptions = mutableListOf<Exception>()

    for (change in update) {
        val target = File(change.path)
        val updateFile = File("${change.path}.up")
        val backupFile = File("${change.path}.bak")

        try {
            if(backupFile(target, backupFile)) {
                backedUpFiles.add(backupFile)
            }
        } catch (e: IOException) {
            println("Failed to backup file: ${target.path}: $e")
            exceptions.add(e)
            continue
        }

        when (change.mode) {
            Mode.FILE -> {
                fileMode(target, updateFile, exceptions)
            }
            Mode.DELETE -> {
                println("Deleting file: ${target.path}")
                // nothing else to do, file already moved to backup file
            }
            Mode.REGEX -> {
                regexMode(target, backupFile, change, exceptions)
            }
            Mode.LINE -> {
                lineMode(target, backupFile, change, exceptions)
            }
        }
    }

    if(exceptions.isNotEmpty()) {
        return if(restore(backedUpFiles, exceptions)) {
            UpdaterStatus(Status.FAILURE, "One or more updates failed.", exceptions.map { it.stackTraceToString() })
        } else {
            UpdaterStatus(Status.FATAL, "One or more updates failed.", exceptions.map { it.stackTraceToString() })
        }
    }

    return if(deleteBackup(backedUpFiles, exceptions)) {
        UpdaterStatus(Status.SUCCESS)
    } else {
        UpdaterStatus(Status.WARNING, "Update successful, but failed to unneeded files.", exceptions.map { it.stackTraceToString() })
    }
}

@Throws(IOException::class)
fun backupFile(file: File, backupFile: File): Boolean {
    if(file.isFile) {
        var exception: Exception?
        do {
            try {
                println("Backing up file: ${file.path}")
                Files.move(file.toPath(), backupFile.toPath())
                exception = null
            } catch (e: AccessDeniedException) {
                println("Waiting for access to file: ${file.path}, trying again in 1 second")
                Thread.sleep(1000)
                exception = e
                continue
            } catch (e: IOException) {
                println("Failed to backup file: ${file.path}: $e")
                throw e
            }
        } while(exception != null)
        return true
    }
    return false
}

fun fileMode(file: File, updateFile: File, exceptions: MutableList<Exception>) {
    println("Updating file: ${file.path}")
    if(!updateFile.isFile) {
        println("Update file not found: ${updateFile.path}")
        exceptions.add(IOException("Update File not found: ${updateFile.path}"))
        return
    }
    try {
        Files.move(updateFile.toPath(), file.toPath())
    } catch (e: IOException) {
        println("Failed to update file: ${file.path}: $e")
        exceptions.add(e)
    }
}

fun regexMode(file: File, dataFile: File, change: UpdateChange, exceptions: MutableList<Exception>) {
    if(!dataFile.isFile) {
        println("File regex content not found: ${dataFile.path}")
        exceptions.add(IOException("Backup File not found: ${dataFile.path}"))
        return
    }
    try {
        var content = dataFile.readText()
        change.elements?.let { elements ->
            for (element in elements) {
                println(
                    "${
                        if (element.replace == true) {
                            "Replacing"
                        } else "Changing"
                    } regex ${element.pattern} match ${element.meta} to ${element.value}"
                )

                val regex = Regex(element.pattern ?: "")
                if(element.meta == null) {
                    content = regex.replace(content, element.value ?: "")
                } else {
                    val index = if(element.meta >= 0) {
                        element.meta
                    } else {
                        (regex.findAll(content).count() + element.meta)
                    }
                    var currentIndex = 0
                    content = regex.replace(content) {
                        if(currentIndex == index) {
                            currentIndex++
                            element.value ?: ""
                        } else {
                            currentIndex++
                            it.value
                        }
                    }
                }
            }
        }
        file.writeText(content)
    } catch (e: IOException) {
        println("Failed to update file: ${file.path}: $e")
        exceptions.add(e)
    }
}

fun lineMode(file: File, dataFile: File, change: UpdateChange, exceptions: MutableList<Exception>) {
    if(!dataFile.isFile) {
        println("File line content not found: ${dataFile.path}")
        exceptions.add(IOException("Backup File not found: ${dataFile.path}"))
        return
    }
    try {
        val lines = ArrayList<String>(dataFile.readLines())
        change.elements?.let {
            for (element in it) {
                println(
                    "${
                        if (element.replace == true) {
                            "Replacing"
                        } else "Changing"
                    } line ${element.meta} to ${element.value}"
                )
                val line = element.meta?.let {
                    if (element.meta < 0) {
                        (lines.size + element.meta + if(element.replace == true) 0 else 1)
                    } else {
                        element.meta
                    }
                } ?: 0
                if(line < 0) {
                    continue
                }
                while (line > lines.size) {
                    lines.add("")
                }
                if (element.replace == true) {
                    if (element.value == null) {
                        lines.removeAt(line)
                    } else {
                        if(line == lines.size) {
                            lines.add(element.value)
                        } else {
                            lines[line] = element.value
                        }
                    }
                } else {
                    lines.add(line, element.value ?: "")
                }
            }
            file.writeText(lines.joinToString("\n"))
        }
    } catch (e: IOException) {
        println("Failed to update file: ${file.path}: $e")
        exceptions.add(e)
    }
}

fun restore(backedUpFiles: List<File>, exceptions: MutableList<Exception>): Boolean {
    println("Update failed. Attempting to restore files")
    var restoreSuccess = true
    for(file in backedUpFiles) {
        try {
            Files.move(file.toPath(), Path(file.name.substring(0, file.name.length - 4)))
        } catch (e: IOException) {
            restoreSuccess = false
            println("Failed to restore file: ${file.path}: $e")
            exceptions.add(e)
        }
    }
    return restoreSuccess
}

fun deleteBackup(backedUpFiles: List<File>, exceptions: MutableList<Exception>): Boolean {
    var deleteSuccess = true
    println("Update successful. Deleting backup files.")
    for(file in backedUpFiles) {
        try {
            Files.delete(file.toPath())
        } catch (e: IOException) {
            deleteSuccess = false
            println("Failed to delete backup file: ${file.path}: $e")
            exceptions.add(e)
        }
    }
    try {
        Files.delete(File("update.json").toPath())
    } catch (e: IOException) {
        deleteSuccess = false
        println("Failed to delete update.json: $e")
        exceptions.add(e)
    }
    return deleteSuccess
}

fun restartApplication() {
    applicationPath?.let {
        println("Restarting application: $it")
        Runtime.getRuntime().exec(it)
    }
}