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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.mods.ModVersionData
import net.treset.treelauncher.backend.util.string.openInBrowser
import net.treset.treelauncher.components.mods.display.ModDisplayData
import net.treset.treelauncher.generic.ComboBox
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.SelectorButton
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.DownloadingIcon
import net.treset.treelauncher.style.icons

@Composable
fun ModDisplayData.ModButton(
    onEdit: () -> Unit
) {
    var selectedVersion: ModVersionData by rememberSaveable(currentVersion!!) { mutableStateOf(currentVersion)}

    LaunchedEffect(selectLatest, versions) {
        versions?.let {
            if(selectLatest && it.isNotEmpty()) {
                selectedVersion = it[0]
            }
        }
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
                        mod?.name?: "",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                    )
                    mod?.description?.let {
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
                        if(!downloading && currentVersion?.versionNumber != selectedVersion.versionNumber) {
                            IconButton(
                                onClick = {
                                    startDownload(selectedVersion)
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
                            loading = versions == null && selectLatest,
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                onEdit()
                            },
                            icon = icons().edit,
                            tooltip = strings().manager.mods.card.edit(),
                        )

                        IconButton(
                            onClick = {
                                changeEnabled()
                            },
                            icon = icons().enabled(enabled),
                            tooltip = strings().manager.mods.card.changeUsed(enabled),
                        )

                        IconButton(
                            onClick = {
                                deleteMod()
                            },
                            icon = icons().delete,
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = strings().manager.mods.card.delete(),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mod?.url?.let {
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