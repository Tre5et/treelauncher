package dev.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.exception.FileDownloadException
import dev.treset.mcdl.mods.ModsDL
import dev.treset.mcdl.mods.curseforge.CurseforgeMod
import dev.treset.mcdl.mods.modrinth.ModrinthMod
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.LauncherModDownload
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.Button
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TextBox
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.io.IOException
import java.nio.file.StandardCopyOption

@Composable
fun ModsEdit(
    modContext: ModContext,
    currentMod: LauncherMod? = null,
    onNewMod: ((LauncherMod, LauncherFile) -> Unit)? = null,
    droppedFile: LauncherFile? = null,
    close: () -> Unit
) {
    val currentFile: LauncherFile? = remember(currentMod) { currentMod?.fileName?.let { LauncherFile.of(modContext.directory, it) } }
    val currentCurseforge: String? = remember(currentMod) { currentMod?.downloads?.firstOrNull { it.provider == "curseforge" }?.id }
    val currentModrinth: String? = remember(currentMod) { currentMod?.downloads?.firstOrNull { it.provider == "modrinth" }?.id }

    var tfName by remember(currentMod) { mutableStateOf(currentMod?.name ?: "") }
    var tfFile by remember(currentMod) { mutableStateOf(currentFile?.path ?: "") }
    var fileError by remember(currentMod) { mutableStateOf(false) }
    var tfVersion by remember(currentMod) { mutableStateOf(currentMod?.version ?: "") }
    var tfCurseforge by remember(currentMod) { mutableStateOf(currentCurseforge ?: "") }
    var curseforgeError by remember(currentMod) { mutableStateOf(false) }
    var tfModrinth by remember(currentMod) { mutableStateOf(currentModrinth ?: "") }
    var modrinthError by remember(currentMod) { mutableStateOf(false) }

    var showFilePicker by remember(currentMod) { mutableStateOf(false) }

    LaunchedEffect(droppedFile) {
        droppedFile?.let {
            tfFile = it.absolutePath
        }
    }

    LaunchedEffect(tfFile) {
        if(tfFile != currentFile?.path && tfFile.endsWith(".jar")) {
            val filePart = tfFile.split("[/\\\\]".toRegex()).last()
            val firstNum = filePart.indexOfFirst { it.isDigit() }
            if(firstNum >= -1) {
                for(i in firstNum downTo 0) {
                    if(filePart[i] == '-' || filePart[i] == '_' || filePart[i] == ' ') {
                        tfVersion = filePart.substring(i + 1, filePart.length - 4)
                        break
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextBox(
            text = tfName,
            onTextChanged = { tfName = it },
            placeholder = Strings.manager.mods.edit.name(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextBox(
                text = tfFile,
                onTextChanged = {
                    tfFile = it
                    fileError = false
                },
                placeholder = Strings.manager.mods.edit.file(),
                modifier = Modifier.weight(1f, false),
                isError = fileError
            )

            IconButton(
                onClick = {
                    showFilePicker = true
                },
                icon = icons().selectFile,
                tooltip = Strings.manager.mods.edit.file()
            )

            FilePicker(
                show = showFilePicker,
                fileExtensions = listOf("jar"),
                onFileSelected = {
                    it?.let {
                        tfFile = it.path
                    }
                    showFilePicker = false
                },
            )
        }

        TextBox(
            text = tfVersion,
            onTextChanged = { tfVersion = it },
            placeholder = Strings.manager.mods.edit.version(),
            modifier = Modifier.fillMaxWidth()
        )

        TextBox(
            text = tfCurseforge,
            onTextChanged = {
                if(it.matches("[0-9]*".toRegex())) {
                    tfCurseforge = it
                    curseforgeError = false
                }
            },
            placeholder = Strings.manager.mods.edit.curseforge(),
            isError = curseforgeError,
            modifier = Modifier.fillMaxWidth()
        )

        TextBox(
            text = tfModrinth,
            onTextChanged = {
                tfModrinth = it
                modrinthError = false
            },
            placeholder = Strings.manager.mods.edit.modrinth(),
            isError = modrinthError,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                modContext.registerChangingJob { mods ->
                    if(!checkCurseforge(tfCurseforge)) {
                        curseforgeError = true
                        return@registerChangingJob
                    }
                    if(!checkModrinth(tfModrinth)) {
                        modrinthError = true
                        return@registerChangingJob
                    }

                    val file = LauncherFile.of(tfFile)
                    if(!file.isFile || !file.name.endsWith(".jar") || (currentFile?.let {file.path != it.path} != false && file.absolutePath.startsWith(appConfig().baseDir.absolutePath))) {
                        fileError = true
                        return@registerChangingJob
                    }
                    val name = tfName.ifBlank { file.name.substring(0, file.name.length-4) }
                    val downloads = getDownloads(tfCurseforge, tfModrinth)
                    val url = if(downloads.isEmpty())
                        null
                    else if(downloads[0].provider == "modrinth")
                        "https://modrinth.com/project/${downloads[0].id}"
                    else
                        "https://www.curseforge.com/projects/${downloads[0].id}"
                    val (description, iconUrl) = getDescriptionIconUrl(tfCurseforge, tfModrinth)

                    currentMod?.let {
                        LOGGER.debug { "Editing existing mod: ${it.name}:v${it.version} -> $name:v$tfVersion" }
                        it.name = name
                        it.description = description
                        it.iconUrl = iconUrl
                        it.version = tfVersion
                        it.downloads = downloads
                        it.url = url

                        if(currentFile?.path != file.path) {
                            LOGGER.debug { "Changing file: ${currentFile?.path} -> ${file.path}" }

                            val oldFile = LauncherFile.of(
                                modContext.directory,
                                it.fileName.let { name -> if(it.enabled) name else "$name.disabled" }
                            )

                            val backupFile = LauncherFile.of(
                                modContext.directory,
                                "${it.fileName}.old"
                            )
                            try {
                                LOGGER.debug { "Backing up old file: ${oldFile.path} -> ${backupFile.path}"}
                                oldFile.moveTo(backupFile)
                            } catch (e: IOException) {
                                AppContext.error(e)
                                return@registerChangingJob
                            }

                            try {
                                val newFile = LauncherFile.of(
                                    modContext.directory,
                                    file.name.let { name -> if(it.enabled) name else "$name.disabled" }
                                )

                                LOGGER.debug { "Copying new file: ${file.path} -> ${newFile.path}"}

                                file.copyTo(newFile, StandardCopyOption.REPLACE_EXISTING)
                            } catch (e: IOException) {
                                LOGGER.warn { "Failed to copy new file: ${file.path} -> ${oldFile.path}, restoring backup"}
                                try {
                                    backupFile.moveTo(oldFile)
                                } catch (e: IOException) {
                                    AppContext.error(e)
                                }
                                return@registerChangingJob
                            }

                            try {
                                LOGGER.debug { "Removing backup file: ${backupFile.path}" }
                                backupFile.remove()
                            } catch (_: IOException) {
                                LOGGER.warn { "Failed to remove backup file: ${backupFile.path}" }
                            }

                            it.currentProvider = null
                            it.fileName = file.name
                        }
                        LOGGER.debug { "Edit completed" }
                    } ?: run {

                        LOGGER.debug { "Adding new mod: $name:v$tfVersion" }
                        val mod = LauncherMod(
                            null,
                            description,
                            true,
                            url,
                            iconUrl,
                            name,
                            file.name,
                            tfVersion,
                            downloads,
                        )

                        onNewMod?.let {
                            LOGGER.debug { "Delegating mod handling to onNewMod function" }
                            tfVersion = ""
                            tfCurseforge = ""
                            tfModrinth = ""
                            tfFile = ""
                            tfName = ""
                            it(mod, file)
                        } ?: run {

                            val newFile = LauncherFile.of(
                                modContext.directory,
                                file.name
                            )

                            try {
                                LOGGER.debug { "Copying new file: ${file.path} -> ${newFile.path}" }
                                file.copyTo(newFile, StandardCopyOption.REPLACE_EXISTING)
                            } catch (e: IOException) {
                                AppContext.error(e)
                                return@registerChangingJob
                            }

                            mods.add(mod)

                            LOGGER.debug { "Add completed" }
                        }
                    }

                    close()
                }
            },
            enabled = tfFile.isNotBlank() && tfVersion.isNotBlank()
                    && currentMod?.let { it.name != tfName || it.version != tfVersion || currentFile?.let { it.path != tfFile } ?: true || (currentCurseforge ?: "") != tfCurseforge || (currentModrinth ?: "") != tfModrinth } ?: true
        ) {
            Text(Strings.manager.mods.edit.confirm(currentMod))
        }
    }
}

private fun checkCurseforge(id: String): Boolean {
    try {
        if (id.isNotBlank() && (!id.matches("[0-9]+".toRegex()) || !ModsDL.checkCurseforgeValid(id.toLong()))) {
            return false
        }
    } catch (e: Exception) {
        return false
    }
    return true
}

private fun checkModrinth(id: String): Boolean {
    try {
        if (id.isNotBlank() && !ModsDL.checkModrinthValid(id)) {
            return false
        }
    } catch (e: Exception) {
        return false
    }
    return true
}

private fun getDownloads(
    curseforgeId: String,
    modrinthId: String,
): List<LauncherModDownload> {
    val downloads = mutableListOf<LauncherModDownload>()
    if (modrinthId.isNotEmpty()) {
        downloads.add(
            LauncherModDownload(
                "modrinth",
                modrinthId
            )
        )
    }
    if (curseforgeId.isNotEmpty()) {
        downloads.add(
            LauncherModDownload(
                "curseforge",
                curseforgeId
            )
        )
    }
    return downloads
}

private fun getDescriptionIconUrl(
    curseforgeId: String,
    modrinthId: String,
): Pair<String?, String?> {
    if (modrinthId.isNotEmpty()) {
        try {
            ModrinthMod.get(modrinthId).let {
                return Pair(it.description, it.iconUrl)
            }
        } catch (ignored: FileDownloadException) {
        }
    }
    if (curseforgeId.isNotEmpty()) {
        try {
            CurseforgeMod.get(curseforgeId.toLong()).let {
                return Pair(it.description, it.iconUrl)
            }
        } catch (ignored: FileDownloadException) {
        }
    }
    return Pair(null, null)
}

private val LOGGER = KotlinLogging.logger {}