import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path

fun main() {
    println("Hello World!")

    try {
        readUpdate()
    } catch (e: IOException) {
        println(e)
        File("updater.json").writeText(Gson().toJson(UpdaterStatus(Status.ERROR, "Failed to read update file.", listOf(e))))
        return
    }

    try {
        File("updater.json").writeText(Gson().toJson(UpdaterStatus(Status.UPDATING)))
        File("updater.json").writeText(Gson().toJson(executeUpdate()))
    } catch (e: IOException) {
        println(e)
    }
}

fun executeUpdate() : UpdaterStatus {
    val backedUpFiles = mutableListOf<File>()
    val exceptions = mutableListOf<Exception>()

    for (change in update) {
        val target = File(change.path)
        val updateFile = File("${change.path}.up")
        val backupFile = File("${change.path}.bak")
        if(target.isFile) {
            var exception: Exception? = null
            do {
                try {
                    println("Backing up file: ${target.path}")
                    Files.move(target.toPath(), backupFile.toPath())
                    backedUpFiles.add(backupFile)
                } catch (e: AccessDeniedException) {
                    println("Failed to backup file: ${target.path}: access denied, trying again in 1 second")
                    Thread.sleep(1000)
                    exception = e
                    continue
                } catch (e: IOException) {
                    println("Failed to backup file: ${target.path}: $e")
                    exceptions.add(e)
                    continue
                }
            } while(exception != null)
        }
        when (change.mode) {
            Mode.FILE -> {
                println("Updating file: ${target.path}")
                if(!updateFile.isFile) {
                    println("Update file not found: ${updateFile.path}")
                    exceptions.add(IOException("Update File not found: ${updateFile.path}"))
                    continue
                }
                try {
                    Files.move(updateFile.toPath(), target.toPath())
                } catch (e: IOException) {
                    println("Failed to update file: ${target.path}: $e")
                    exceptions.add(e)
                    continue
                }
            }
            Mode.DELETE -> {
                println("Deleting file: ${target.path}")
            }
            Mode.REGEX -> {
                if(!backupFile.isFile) {
                    println("File regex content not found: ${backupFile.path}")
                    exceptions.add(IOException("Backup File not found: ${backupFile.path}"))
                    continue
                }
                try {
                    var content = backupFile.readText()
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
                    target.writeText(content)
                } catch (e: IOException) {
                    println("Failed to update file: ${target.path}: $e")
                    exceptions.add(e)
                    continue
                }
            }
            Mode.LINE -> {
                if(!backupFile.isFile) {
                    println("File line content not found: ${backupFile.path}")
                    exceptions.add(IOException("Backup File not found: ${backupFile.path}"))
                    continue
                }
                try {
                    val lines = ArrayList<String>(backupFile.readLines())
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
                        target.writeText(lines.joinToString("\n"))
                    }
                } catch (e: IOException) {
                    println("Failed to update file: ${target.path}: $e")
                    exceptions.add(e)
                    continue
                }
            }
        }
    }

    if(exceptions.isNotEmpty()) {
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
        return if(restoreSuccess) {
            UpdaterStatus(Status.ERROR, "Restored successfully.", exceptions)
        } else {
            UpdaterStatus(Status.ERROR, "Failed to restore.", exceptions)
        }
    }

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

    return if(deleteSuccess) {
        UpdaterStatus(Status.SUCCESS)
    } else {
        UpdaterStatus(Status.WARNING, "Update successful, but failed to unneeded files.", exceptions)
    }
}