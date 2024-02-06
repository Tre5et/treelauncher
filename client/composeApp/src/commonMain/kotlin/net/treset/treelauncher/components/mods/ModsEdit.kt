package net.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.launcher.LauncherModDownload
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.TextBox
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import java.io.IOException
import java.nio.file.StandardCopyOption

@Composable
fun ModsEdit(
    modContext: ModContext,
    currentMod: LauncherMod? = null,
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextBox(
            text = tfName,
            onChange = { tfName = it },
            placeholder = strings().manager.mods.edit.name()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextBox(
                text = tfFile,
                onChange = {
                    tfFile = it
                    fileError = false
                },
                placeholder = strings().manager.mods.edit.file(),
                modifier = Modifier.weight(1f, false),
                isError = fileError
            )

            IconButton(
                onClick = {
                    showFilePicker = true
                },
                tooltip = strings().manager.mods.edit.file()
            ) {
                Icon(
                    icons().selectFile,
                    "Open File Picker"
                )
            }

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
            onChange = { tfVersion = it },
            placeholder = strings().manager.mods.edit.version()
        )

        TextBox(
            text = tfCurseforge,
            onChange = {
                if(it.matches("[0-9]*".toRegex())) {
                    tfCurseforge = it
                    curseforgeError = false
                }
            },
            placeholder = strings().manager.mods.edit.curseforge(),
            isError = curseforgeError
        )

        TextBox(
            text = tfModrinth,
            onChange = {
                tfModrinth = it
                modrinthError = false
            },
            placeholder = strings().manager.mods.edit.modrinth(),
            isError = modrinthError
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
                                it.fileName
                            )

                            val backupFile = LauncherFile.of(
                                modContext.directory,
                                "${it.fileName}.old"
                            )
                            try {
                                LOGGER.debug { "Backing up old file: ${oldFile.path} -> ${backupFile.path}"}
                                oldFile.moveTo(backupFile)
                            } catch (e: IOException) {
                                app().error(e)
                                return@registerChangingJob
                            }

                            try {
                                val newFile = LauncherFile.of(
                                    modContext.directory,
                                    file.name
                                )

                                LOGGER.debug { "Copying new file: ${file.path} -> ${newFile.path}"}

                                file.copyTo(newFile, StandardCopyOption.REPLACE_EXISTING)
                            } catch (e: IOException) {
                                LOGGER.warn { "Failed to copy new file: ${file.path} -> ${oldFile.path}, restoring backup"}
                                try {
                                    backupFile.moveTo(oldFile)
                                } catch (e: IOException) {
                                    app().error(e)
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

                        val newFile = LauncherFile.of(
                            modContext.directory,
                            file.name
                        )

                        try {
                            LOGGER.debug { "Copying new file: ${file.path} -> ${newFile.path}" }
                            file.copyTo(newFile, StandardCopyOption.REPLACE_EXISTING)
                        } catch(e: IOException) {
                            app().error(e)
                            return@registerChangingJob
                        }

                        mods.add(mod)

                        LOGGER.debug { "Add completed" }
                    }

                    close()
                }
            },
            enabled = tfFile.isNotBlank() && tfVersion.isNotBlank()
                    && currentMod?.let { it.name != tfName || it.version != tfVersion || currentFile?.let { it.path == tfName } ?: true || (currentCurseforge ?: "") != tfCurseforge || (currentModrinth ?: "") != tfModrinth } ?: true
        ) {
            Text(strings().manager.mods.edit.confirm(currentMod))
        }
    }
}

private fun checkCurseforge(id: String): Boolean {
    try {
        if (id.isNotBlank() && (!id.matches("[0-9]+".toRegex()) || !MinecraftMods.checkCurseforgeValid(id.toLong()))) {
            return false
        }
    } catch (e: Exception) {
        return false
    }
    return true
}

private fun checkModrinth(id: String): Boolean {
    try {
        if (id.isNotBlank() && !MinecraftMods.checkModrinthValid(id)) {
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
            MinecraftMods.getModrinthMod(modrinthId).let {
                return Pair(it.description, it.iconUrl)
            }
        } catch (ignored: FileDownloadException) {
        }
    }
    if (curseforgeId.isNotEmpty()) {
        try {
            MinecraftMods.getCurseforgeMod(curseforgeId.toLong()).let {
                return Pair(it.description, it.iconUrl)
            }
        } catch (ignored: FileDownloadException) {
        }
    }
    return Pair(null, null)
}

private val LOGGER = KotlinLogging.logger {}