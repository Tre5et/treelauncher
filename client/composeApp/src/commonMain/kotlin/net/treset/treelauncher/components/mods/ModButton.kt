package net.treset.treelauncher.components.mods

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.mods.*
import net.treset.treelauncher.backend.mods.ModDownloader
import net.treset.treelauncher.backend.util.ModProviderStatus
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.loadNetworkImage
import net.treset.treelauncher.backend.util.string.openInBrowser
import net.treset.treelauncher.generic.ComboBox
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.SelectorButton
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.DownloadingIcon
import net.treset.treelauncher.style.icons
import java.time.LocalDateTime
import java.util.*

@Composable
fun ModButton(
    mod: LauncherMod,
    modContext: ModContext,
    checkUpdates: Boolean,
    onEdit: () -> Unit
) {
    var downloading by rememberSaveable(mod) { mutableStateOf(false) }

    var image: Painter? by rememberSaveable(mod) { mutableStateOf(null) }

    var currentVersion: ModVersionData by rememberSaveable(mod) { mutableStateOf(
        object: GenericModVersion() {
            override fun getDatePublished(): LocalDateTime? = null
            override fun getDownloads(): Int = 0
            override fun getName(): String? = null
            override fun getVersionNumber(): String = mod.version
            override fun getDownloadUrl(): String? = null
            override fun getModLoaders(): MutableList<String> = mutableListOf()
            override fun getGameVersions(): MutableList<String> = mutableListOf()
            override fun getRequiredDependencies(p0: String?, p1: String?): MutableList<ModVersionData> = mutableListOf()
            override fun getParentMod(): ModData? = null
            override fun setParentMod(p0: ModData?) {}
            override fun getModProviders(): MutableList<ModProvider> = mutableListOf()
            override fun getModVersionType(): ModVersionType? = null
        }
    )}

    var versions: List<ModVersionData>? by rememberSaveable(mod, modContext.version) { mutableStateOf(null) }
    var selectedVersion: ModVersionData by rememberSaveable(mod, modContext.version) { mutableStateOf(currentVersion)}

    var enabled by rememberSaveable(mod) { mutableStateOf(mod.isEnabled) }

    var modData: Optional<ModData> = rememberSaveable(mod) { Optional.empty() }

    val modrinthStatus = rememberSaveable(mod) {
        if(mod.currentProvider == "modrinth") {
            ModProviderStatus.CURRENT
        } else if(mod.downloads.any { it.provider == "modrinth" }) {
            ModProviderStatus.AVAILABLE
        } else {
            ModProviderStatus.UNAVAILABLE
        }
    }
    val curseforgeStatus  = rememberSaveable(mod) {
        if(mod.currentProvider == "curseforge") {
            ModProviderStatus.CURRENT
        } else if(mod.downloads.any { it.provider == "curseforge" }) {
            ModProviderStatus.AVAILABLE
        } else {
            ModProviderStatus.UNAVAILABLE
        }
    }

    LaunchedEffect(mod) {
        image ?: Thread {
            mod.iconUrl?.let { url ->
                loadNetworkImage(url)?.let {
                    image = it
                }
            }
        }.start()
    }

    LaunchedEffect(mod, modContext.version) {
        versions ?: Thread {
            if(modData.isEmpty) {
                try {
                    modData = Optional.of(mod.modData)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if(modData.isPresent) {
                versions = modData.get().getVersions(modContext.version, "fabric")
                    .also { vs ->
                        vs
                            .firstOrNull { it.versionNumber == currentVersion.versionNumber }
                            ?.let {
                                currentVersion = it
                            }
                    }

            }
        }.start()
    }

    SelectorButton(
        selected = false,
        onClick = {}
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.Start)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LocalContentColor.current)
                        .padding(4.dp)
                        .size(72.dp)
                ) {
                    Image(
                        image ?: painterResource("img/default_mod.png"),
                        "Mod Icon",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        mod.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    mod.description?.let {
                        Text(
                            it,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if(!downloading && currentVersion.versionNumber != selectedVersion.versionNumber) {
                            IconButton(
                                onClick = {
                                    downloading = true
                                    modContext.registerChangingJob { currentMods ->
                                        ModDownloader(
                                            mod,
                                            modContext.directory,
                                            "fabric",
                                            modContext.version,
                                            currentMods,
                                            modContext.enableOnDownload
                                        ).download(
                                            selectedVersion
                                        )

                                        currentVersion = selectedVersion

                                        downloading = false
                                    }
                                },
                                tooltip = strings().manager.mods.card.download(),
                            ) {
                                Icon(
                                    icons().download,
                                    "Download"
                                )
                            }
                        }
                        if(downloading) {
                            DownloadingIcon(
                                "Downloading",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        ComboBox(
                            items = versions?: emptyList(),
                            onSelected = {
                                selectedVersion = it
                            },
                            defaultSelected = versions?.let { if(checkUpdates && it.isNotEmpty()) it[0] else null } ?: currentVersion,
                            loading = versions == null && checkUpdates,
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                onEdit()
                            },
                            tooltip = strings().manager.mods.card.edit(),
                        ) {
                            Icon(
                                icons().edit,
                                "Edit"
                            )
                        }

                        IconButton(
                            onClick = {
                                modContext.registerChangingJob {
                                    val modFile: LauncherFile = LauncherFile.of(
                                        modContext.directory,
                                        "${mod.fileName}${if (enabled) "" else ".disabled"}"
                                    )
                                    val newFile: LauncherFile = LauncherFile.of(
                                        modContext.directory,
                                        "${mod.fileName}${if (enabled) ".disabled" else ""}"
                                    )
                                    modFile.moveTo(newFile)

                                    mod.isEnabled = !enabled

                                    enabled = !enabled
                                }
                            },
                            tooltip = strings().manager.mods.card.changeUsed(enabled),
                        ) {
                            Icon(
                                icons().enabled(enabled),
                                if(enabled) "Disable" else "Enable"
                            )
                        }

                        IconButton(
                            onClick = {
                                modContext.registerChangingJob { mods ->
                                    val oldFile: LauncherFile = LauncherFile.of(
                                        modContext.directory,
                                        "${mod.fileName}${if (enabled) "" else ".disabled"}"
                                    )
                                    oldFile.remove()
                                    mods.remove(mod)
                                }
                            },
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = strings().manager.mods.card.delete(),
                        ) {
                            Icon(
                                icons().delete,
                                "Delete"
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mod.url?.let {
                            IconButton(
                                onClick = {
                                    it.openInBrowser()
                                },
                                tooltip = strings().manager.mods.card.openBrowser()
                            ) {
                                Icon(
                                    icons().browser,
                                    "Open in browser"
                                )
                            }
                        }

                        Icon(
                            icons().modrinth,
                            "Modrinth",
                            tint = icons().modrinthColor(modrinthStatus),
                            modifier = Modifier.size(32.dp)
                        )
                        Icon(
                            icons().curseforge,
                            "Curseforge",
                            tint = icons().curseforgeColor(curseforgeStatus),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

    }
}