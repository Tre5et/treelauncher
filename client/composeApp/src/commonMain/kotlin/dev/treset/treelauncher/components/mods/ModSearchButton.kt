package dev.treset.treelauncher.components.mods

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.mods.ModVersionData
import dev.treset.treelauncher.backend.util.string.openInBrowser
import dev.treset.treelauncher.generic.ComboBox
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.SelectorButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.DownloadingIcon
import dev.treset.treelauncher.style.icons

@Composable
fun ModDataDisplay.ModSearchButton(
    ctx: ModDisplayContext
) {
    var selectedVersion: ModVersionData? by remember(mod, currentVersion.value) { mutableStateOf(currentVersion.value) }

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
                    image.value ?: painterResource("img/default_mod.png"),
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
                    mod.name?: "",
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
                    if(!downloading.value && currentVersion.value?.versionNumber != selectedVersion?.versionNumber) {
                        IconButton(
                            onClick = {
                                selectedVersion?.let {
                                    download(it, ctx)
                                }
                            },
                            icon = icons().download,
                            tooltip = Strings.manager.mods.card.download(),
                        )
                    }
                    if(downloading.value) {
                        DownloadingIcon(
                            "Downloading",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    ComboBox(
                        items = versions.value ?: emptyList(),
                        onSelected = {
                            selectedVersion = it
                        },
                        selected = selectedVersion,
                        placeholder = Strings.manager.mods.card.versionPlaceholder(),
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
                            tooltip = Strings.manager.mods.card.openBrowser()
                        )
                    }

                    Icon(
                        icons().modrinth,
                        "Modrinth",
                        tint = icons().modrinthColor(modrinthStatus.value),
                        modifier = Modifier.size(32.dp)
                    )
                    Icon(
                        icons().curseforge,
                        "Curseforge",
                        tint = icons().curseforgeColor(curseforgeStatus.value),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

    }
}