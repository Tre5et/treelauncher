package dev.treset.treelauncher.components.mods

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.mods.ModVersionData
import dev.treset.treelauncher.backend.data.manifest.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.mods.modVersionFromString
import dev.treset.treelauncher.backend.util.string.openInBrowser
import dev.treset.treelauncher.generic.ComboBox
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.SelectorButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.DownloadingIcon
import dev.treset.treelauncher.style.disabledContent
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.ListDisplay
import org.jetbrains.compose.resources.painterResource
import treelauncher.composeapp.generated.resources.Res
import treelauncher.composeapp.generated.resources.alert_mod
import treelauncher.composeapp.generated.resources.default_mod

@Composable
fun LauncherMod.ModButton(
    component: ModsComponent,
    display: ListDisplay,
    onEdit: () -> Unit
) {
    var selectedVersion: ModVersionData? by rememberSaveable(currentVersion.value) { mutableStateOf(currentVersion.value)}

    DisposableEffect(Unit) {
        visible.value = true

        onDispose {
            visible.value = false
        }
    }

    LaunchedEffect(selectLatest.value, versions.value) {
        versions.value?.let {
            if(selectLatest.value > 0 && it.isNotEmpty()) {
                selectedVersion = it[0]
            }
        }
    }

    val image = image.value ?: if(hasMetaData.value) painterResource(Res.drawable.default_mod) else painterResource(Res.drawable.alert_mod)

    val versionRow = @Composable {
        if(!downloading.value && selectedVersion != null && currentVersion.value?.versionNumber != selectedVersion?.versionNumber) {
            IconButton(
                onClick = {
                    selectedVersion?.let {
                        downloadVersion(it, component)
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
            selected = selectedVersion ?: modVersionFromString(Strings.manager.mods.noVersion()),
            loading = versions.value == null && selectLatest.value > 0,
        )
    }

    when(display) {
        ListDisplay.FULL -> SelectorButton(
            selected = false,
            onClick = {},
            enabled = enabled.value,
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
                        image,
                        "Mod Icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if(enabled.value) 1f else 0.38f)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        name.value,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                    )
                    description.value?.let {
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
                        versionRow()
                    }

                    Row {
                        IconButton(
                            onClick = {
                                onEdit()
                            },
                            icon = icons().edit,
                            tooltip = Strings.manager.mods.card.edit(),
                        )

                        IconButton(
                            onClick = {
                                changeEnabled(component)
                            },
                            icon = icons().enabled(enabled.value),
                            tooltip = Strings.manager.mods.card.changeUsed(enabled.value),
                        )

                        IconButton(
                            onClick = {
                                delete(component)
                            },
                            icon = icons().delete,
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = Strings.manager.mods.card.delete(),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        url.value?.let {
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
                            tint = icons().modrinthColor(modrinthStatus.value).let {
                                if(enabled.value) it else it.disabledContent()
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Icon(
                            icons().curseforge,
                            "Curseforge",
                            tint = icons().curseforgeColor(curseforgeStatus.value).let {
                                if(enabled.value) it else it.disabledContent()
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        ListDisplay.COMPACT -> SelectorButton(
            selected = false,
            onClick = {},
            enabled = enabled.value
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(LocalContentColor.current)
                        .size(64.dp)
                        .padding(3.dp)
                ) {
                    Image(
                        image,
                        "Icon",
                        modifier = Modifier.size(58.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        name.value,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    description.value?.let {
                        Text(
                            it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        versionRow()
                    }

                    Row {
                        url.value?.let {
                            IconButton(
                                onClick = {
                                    it.openInBrowser()
                                },
                                icon = icons().browser,
                                tooltip = Strings.manager.mods.card.openBrowser()
                            )
                        }

                        IconButton(
                            onClick = {
                                onEdit()
                            },
                            icon = icons().edit,
                            tooltip = Strings.manager.mods.card.edit(),
                        )

                        IconButton(
                            onClick = {
                                changeEnabled(component)
                            },
                            icon = icons().enabled(enabled.value),
                            tooltip = Strings.manager.mods.card.changeUsed(enabled.value),
                        )

                        IconButton(
                            onClick = {
                                delete(component)
                            },
                            icon = icons().delete,
                            interactionTint = MaterialTheme.colorScheme.error,
                            tooltip = Strings.manager.mods.card.delete(),
                        )

                    }
                }
            }
        }

        ListDisplay.MINIMAL -> SelectorButton(
            selected = false,
            onClick = {},
            enabled = enabled.value
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    name.value,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )

                versionRow()

                url.value?.let {
                    IconButton(
                        onClick = {
                            it.openInBrowser()
                        },
                        icon = icons().browser,
                        tooltip = Strings.manager.mods.card.openBrowser()
                    )
                }

                IconButton(
                    onClick = {
                        onEdit()
                    },
                    icon = icons().edit,
                    tooltip = Strings.manager.mods.card.edit(),
                )

                IconButton(
                    onClick = {
                        changeEnabled(component)
                    },
                    icon = icons().enabled(enabled.value),
                    tooltip = Strings.manager.mods.card.changeUsed(enabled.value),
                )

                IconButton(
                    onClick = {
                        delete(component)
                    },
                    icon = icons().delete,
                    interactionTint = MaterialTheme.colorScheme.error,
                    tooltip = Strings.manager.mods.card.delete(),
                )
            }
        }

    }
}