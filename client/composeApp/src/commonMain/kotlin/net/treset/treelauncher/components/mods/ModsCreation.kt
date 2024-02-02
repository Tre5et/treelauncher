package net.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.creation.CreationState
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings

class ModsCreationState(
    mode: CreationMode,
    name: String?,
    existing: Pair<LauncherManifest, LauncherModsDetails>?,
    val version: String?
) : CreationState<Pair<LauncherManifest, LauncherModsDetails>>(
    mode,
    name,
    existing
) {
    override fun isValid(): Boolean = when(mode) {
        CreationMode.NEW -> !name.isNullOrBlank() && !version.isNullOrBlank()
        CreationMode.INHERIT -> !name.isNullOrBlank() && existing != null
        CreationMode.USE -> existing != null
    }

    companion object {
        fun of(
            mode: CreationMode,
            newName: String?,
            newVersion: String?,
            inheritName: String?,
            inheritSelected: Pair<LauncherManifest, LauncherModsDetails>?,
            useSelected: Pair<LauncherManifest, LauncherModsDetails>?
        ): ModsCreationState = ModsCreationState(
            mode,
            when(mode) {
                CreationMode.NEW -> newName?.ifBlank { null }
                CreationMode.INHERIT -> inheritName?.ifBlank { null }
                CreationMode.USE -> null
            },
            when(mode) {
                CreationMode.NEW -> null
                CreationMode.INHERIT -> inheritSelected
                CreationMode.USE -> useSelected
            },
            if(mode == CreationMode.NEW) newVersion else null
        )
    }
}

@Composable
fun ModsCreation(
    existing: List<Pair<LauncherManifest, LauncherModsDetails>>,
    onCreate: (ModsCreationState) -> Unit = { _->}
) {
    var mode by remember(existing) { mutableStateOf(CreationMode.NEW) }

    var newName by remember(existing) { mutableStateOf("") }
    var newVersion by remember(existing) { mutableStateOf("") }

    var inheritName by remember(existing) { mutableStateOf("") }
    var inheritSelected: Pair<LauncherManifest, LauncherModsDetails>? by remember(existing) { mutableStateOf(null) }

    var showSnapshots by remember(existing) { mutableStateOf(false) }
    var versions: List<MinecraftVersion> by remember(showSnapshots) { mutableStateOf(emptyList()) }

    LaunchedEffect(showSnapshots) {
        versions = if(showSnapshots) {
            MinecraftGame.getVersions()
        } else {
            MinecraftGame.getReleases()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TitledRadioButton(
            text = strings().creator.radioCreate(),
            selected = mode == CreationMode.NEW,
            onClick = { mode = CreationMode.NEW }
        )
        TextBox(
            text = newName,
            onChange = {
                newName = it
            },
            placeholder = strings().creator.name(),
            enabled = mode == CreationMode.NEW
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ComboBox(
                items = versions,
                onSelected = {
                    newVersion = it.id
                },
                placeholder = strings().creator.mods.version(),
                loading = versions.isEmpty(),
                toDisplayString = { id },
                enabled = mode == CreationMode.NEW
            )

            TitledCheckBox(
                title = strings().creator.version.showSnapshots(),
                checked = showSnapshots,
                onCheckedChange = {
                    showSnapshots = it
                },
                enabled = mode == CreationMode.NEW
            )
        }

        TitledRadioButton(
            text = strings().creator.radioInherit(),
            selected = mode == CreationMode.INHERIT,
            onClick = { mode = CreationMode.INHERIT }
        )
        TextBox(
            text = inheritName,
            onChange = {
                inheritName = it
            },
            placeholder = strings().creator.name(),
            enabled = mode == CreationMode.INHERIT
        )
        ComboBox(
            items = existing,
            defaultSelected = inheritSelected,
            onSelected = {
                inheritSelected = it
            },
            placeholder = strings().creator.component(),
            toDisplayString = { first.name },
            enabled = mode == CreationMode.INHERIT
        )

        Button(
            enabled = when(mode) {
                CreationMode.NEW -> newName.isNotBlank()
                CreationMode.INHERIT -> inheritName.isNotBlank() && inheritSelected != null
                else -> false
            },
            onClick = {
                ModsCreationState.of(
                    mode,
                    newName,
                    newVersion,
                    inheritName,
                    inheritSelected,
                    null
                ).let {
                    if(it.isValid()) onCreate(it)
                }
            }
        ) {
            Text(strings().creator.buttonCreate())
        }
    }
}