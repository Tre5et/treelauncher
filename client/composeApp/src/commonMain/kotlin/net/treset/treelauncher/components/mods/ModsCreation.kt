package net.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import net.treset.mcdl.minecraft.MinecraftVersion
import net.treset.treelauncher.backend.data.LauncherModsDetails
import net.treset.treelauncher.backend.data.manifest.Component
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.creation.CreationState
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings

class ModsCreationState(
    mode: CreationMode,
    name: String?,
    existing: Pair<Component, LauncherModsDetails>?,
    val version: MinecraftVersion?,
    val type: VersionType?,
    val alternateLoader: Boolean?
) : CreationState<Pair<Component, LauncherModsDetails>>(
    mode,
    name,
    existing
) {
    override fun isValid(): Boolean = when(mode) {
        CreationMode.NEW -> !name.isNullOrBlank() && version != null && type != null && alternateLoader != null
        CreationMode.INHERIT -> !name.isNullOrBlank() && existing != null
        CreationMode.USE -> existing != null
    }

    companion object {
        fun of(
            mode: CreationMode,
            newName: String?,
            newVersion: MinecraftVersion?,
            newType: VersionType?,
            alternateLoader: Boolean?,
            inheritName: String?,
            inheritSelected: Pair<Component, LauncherModsDetails>?,
            useSelected: Pair<Component, LauncherModsDetails>?
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
            if(mode == CreationMode.NEW) newVersion else null,
            if(mode == CreationMode.NEW) newType else null,
            if(mode == CreationMode.NEW) alternateLoader else null
        )
    }
}

@Composable
fun ModsCreation(
    existing: List<Pair<Component, LauncherModsDetails>>,
    showCreate: Boolean = true,
    showUse: Boolean = true,
    setCurrentState: (ModsCreationState) -> Unit = {},
    defaultVersion: MinecraftVersion? = null,
    defaultType: VersionType? = null,
    defaultAlternate: Boolean = true,
    onCreate: (ModsCreationState) -> Unit = { _->}
) {
    var mode by remember(existing) { mutableStateOf(CreationMode.NEW) }

    var newName by remember(existing) { mutableStateOf("") }
    var newVersion: MinecraftVersion? by remember(existing, defaultVersion) { mutableStateOf(defaultVersion) }
    var newType: VersionType? by remember(existing, defaultType) { mutableStateOf(defaultType) }
    var alternateLoader by remember(existing, defaultAlternate) { mutableStateOf(defaultAlternate) }

    var inheritName by remember(existing) { mutableStateOf("") }
    var inheritSelected: Pair<Component, LauncherModsDetails>? by remember(existing) { mutableStateOf(null) }

    var useSelected: Pair<Component, LauncherModsDetails>? by remember(existing) { mutableStateOf(null) }

    var showSnapshots by remember(existing) { mutableStateOf(false) }
    var versions: List<MinecraftVersion> by remember(showSnapshots) { mutableStateOf(emptyList()) }

    val currentState = remember(mode, newName, newVersion, newType, alternateLoader, inheritName, inheritSelected, useSelected) {
        ModsCreationState.of(
            mode,
            newName,
            newVersion,
            newType,
            alternateLoader,
            inheritName,
            inheritSelected,
            useSelected
        ).also(setCurrentState)
    }

    LaunchedEffect(showSnapshots) {
        versions = if(showSnapshots) {
            MinecraftVersion.getAll()
        } else {
            MinecraftVersion.getAll().filter { it.isRelease }
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
            onTextChanged = {
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
                selected = newVersion,
                onSelected = {
                    newVersion = it
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

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ComboBox(
                items = VersionType.entries.filter { it != VersionType.VANILLA },
                selected = newType,
                onSelected = {
                    newType = it
                },
                placeholder = strings().creator.mods.type(),
                enabled = mode == CreationMode.NEW
            )
            if(newType == VersionType.QUILT) {
                TitledCheckBox(
                    title = strings().creator.mods.quiltIncludeFabric(),
                    checked = alternateLoader,
                    onCheckedChange = {
                        alternateLoader = it
                    },
                )
            }
        }

        TitledRadioButton(
            text = strings().creator.radioInherit(),
            selected = mode == CreationMode.INHERIT,
            onClick = { mode = CreationMode.INHERIT }
        )
        TextBox(
            text = inheritName,
            onTextChanged = {
                inheritName = it
            },
            placeholder = strings().creator.name(),
            enabled = mode == CreationMode.INHERIT
        )
        ComboBox(
            items = existing,
            selected = inheritSelected,
            onSelected = {
                inheritSelected = it
            },
            placeholder = strings().creator.component(),
            toDisplayString = { first.name },
            enabled = mode == CreationMode.INHERIT
        )

        if(showUse) {
            TitledRadioButton(
                text = strings().creator.radioUse(),
                selected = mode == CreationMode.USE,
                onClick = { mode = CreationMode.USE }
            )
            ComboBox(
                items = existing,
                selected = useSelected,
                onSelected = {
                    useSelected = it
                },
                placeholder = strings().creator.component(),
                toDisplayString = { first.name },
                enabled = mode == CreationMode.USE
            )
        }

        if(showCreate) {
            Button(
                enabled = currentState.isValid(),
                onClick = { if (currentState.isValid()) onCreate(currentState) }
            ) {
                Text(strings().creator.buttonCreate())
            }
        }
    }
}