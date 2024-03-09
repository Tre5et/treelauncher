package net.treset.treelauncher.components.mods

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.mods.*
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.mods.ModDownloader
import net.treset.treelauncher.backend.util.ModProviderStatus
import net.treset.treelauncher.backend.util.isSame
import net.treset.treelauncher.backend.util.loadNetworkImage
import net.treset.treelauncher.backend.util.string.openInBrowser
import net.treset.treelauncher.generic.ComboBox
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.SelectorButton
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.DownloadingIcon
import net.treset.treelauncher.style.icons
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Composable
fun ModSearchButton(
    mod: ModData,
    searchContext: SearchContext
) {
    var downloading by rememberSaveable(mod) { mutableStateOf(false) }

    var image: Painter? by rememberSaveable(mod) { mutableStateOf(null) }

    var launcherMod: Optional<LauncherMod>? by rememberSaveable(mod) { mutableStateOf(null) }

    var versions: List<ModVersionData>? by rememberSaveable(mod) { mutableStateOf(null) }
    var currentVersion: ModVersionData? by rememberSaveable(mod, launcherMod, versions) { mutableStateOf(
        versions?.firstOrNull { it.versionNumber == launcherMod?.getOrNull()?.version }
    ) }
    var selectedVersion: ModVersionData? by rememberSaveable(mod, currentVersion) { mutableStateOf(currentVersion) }

    val modrinthStatus = rememberSaveable(mod, launcherMod) {
        launcherMod?.getOrNull()?.let {
            if(it.currentProvider == "modrinth") {
                ModProviderStatus.CURRENT
            } else null
        } ?: run {
            if(mod.modProviders.contains(ModProvider.MODRINTH)) {
                ModProviderStatus.AVAILABLE
            } else {
                ModProviderStatus.UNAVAILABLE
            }
        }
    }
    val curseforgeStatus  = rememberSaveable(mod, launcherMod) {
        launcherMod?.getOrNull()?.let {
            if(it.currentProvider == "curseforge") {
                ModProviderStatus.CURRENT
            } else null
        } ?: run {
            if(mod.modProviders.contains(ModProvider.CURSEFORGE)) {
                ModProviderStatus.AVAILABLE
            } else {
                ModProviderStatus.UNAVAILABLE
            }
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

    LaunchedEffect(mod, searchContext.recheck) {
        searchContext.registerChangingJob { mods ->
            launcherMod = mods.firstOrNull {
                    it.isSame(mod)
                }?.let {
                    Optional.of(it)
                } ?: Optional.empty()
        }
    }

    LaunchedEffect(mod) {
        versions ?: Thread {
            try {
                versions = mod.getVersions(searchContext.versions, searchContext.types.map { it.id })
            } catch (e: FileDownloadException) {
                app().error(e)
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
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start
                )
                mod.description?.let {
                    Text(
                        it,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        maxLines = 2
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
                    if(!downloading && currentVersion?.versionNumber != selectedVersion?.versionNumber) {
                        IconButton(
                            onClick = {
                                downloading = true
                                selectedVersion?.let {
                                    searchContext.registerChangingJob { currentMods ->
                                        try {
                                            ModDownloader(
                                                launcherMod?.getOrNull(),
                                                searchContext.directory,
                                                searchContext.types,
                                                searchContext.versions,
                                                currentMods,
                                                searchContext.enableOnDownload
                                            ).download(
                                                it
                                            )

                                            currentVersion = selectedVersion
                                        } catch(e: Exception) {
                                            app().error(e)
                                        }

                                        downloading = false

                                        searchContext.requestRecheck()
                                    }
                                }
                            },
                            icon = icons().download,
                            tooltip = strings().manager.mods.card.download(),
                        )
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
                        selected = selectedVersion,
                        placeholder = strings().manager.mods.card.versionPlaceholder(),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mod.url?.let {
                        IconButton(
                            onClick = {
                                it.openInBrowser()
                            },
                            icon = icons().browser,
                            tooltip = strings().manager.mods.card.openBrowser()
                        )
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