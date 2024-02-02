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
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.launcher.LauncherModDownload
import net.treset.mc_version_loader.mods.MinecraftMods
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.TextBox
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import java.nio.file.StandardCopyOption

@Composable
fun ModsLocal(
    modContext: ModContext,
    close: (Boolean) -> Unit
) {
    var tfName by remember { mutableStateOf("") }
    var tfFile by remember { mutableStateOf("") }
    var fileError by remember { mutableStateOf(false) }
    var tfVersion by remember { mutableStateOf("") }
    var tfCurseforge by remember { mutableStateOf("") }
    var curseforgeError by remember { mutableStateOf(false) }
    var tfModrinth by remember { mutableStateOf("") }
    var modrinthError by remember { mutableStateOf(false) }

    var showFilePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { close(false) },
                tooltip = strings().manager.mods.search.back()
            ) {
                Icon(
                    imageVector = icons().back,
                    contentDescription = "Back",
                )
            }
        }

        TextBox(
            text = tfName,
            onChange = { tfName = it },
            placeholder = strings().manager.mods.local.name()
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
                placeholder = strings().manager.mods.local.file(),
                modifier = Modifier.weight(1f, false),
                isError = fileError
            )

            IconButton(
                onClick = {
                    showFilePicker = true
                },
                tooltip = strings().manager.mods.local.file()
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
            placeholder = strings().manager.mods.local.version()
        )

        TextBox(
            text = tfCurseforge,
            onChange = {
                tfCurseforge = it
                curseforgeError = false
            },
            placeholder = strings().manager.mods.local.curseforge(),
            isError = curseforgeError
        )

        TextBox(
            text = tfModrinth,
            onChange = {
                tfModrinth = it
                modrinthError = false
            },
            placeholder = strings().manager.mods.local.modrinth(),
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
                    if(!file.isFile || !file.name.endsWith(".jar")) {
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

                    file.copyTo(newFile, StandardCopyOption.REPLACE_EXISTING)

                    mods.add(mod)

                    close(true)
                }
            },
            enabled = tfFile.isNotBlank() && tfVersion.isNotBlank()
        ) {
            Text(strings().manager.mods.local.confirm())
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