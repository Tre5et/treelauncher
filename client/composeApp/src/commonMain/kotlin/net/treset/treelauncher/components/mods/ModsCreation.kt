package net.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import net.treset.mcdl.minecraft.MinecraftVersion
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.ModsCreationData
import net.treset.treelauncher.backend.creation.ModsCreator
import net.treset.treelauncher.backend.data.manifest.ModsComponent
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.creation.CreationPopup
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import java.io.IOException

@Composable
fun ModsCreation(
    existing: List<ModsComponent>,
    showCreate: Boolean = true,
    showUse: Boolean = true,
    defaultVersion: MinecraftVersion? = null,
    defaultType: VersionType? = null,
    defaultAlternate: Boolean = true,
    onDone: (ModsComponent) -> Unit = { _->}
) {
    var mode by remember(existing) { mutableStateOf(CreationMode.NEW) }

    var newName by remember(existing) { mutableStateOf("") }
    var newVersion: MinecraftVersion? by remember(existing, defaultVersion) { mutableStateOf(defaultVersion) }
    var newType: VersionType? by remember(existing, defaultType) { mutableStateOf(defaultType) }
    var alternateLoader by remember(existing, defaultAlternate) { mutableStateOf(defaultAlternate) }

    var inheritName by remember(existing) { mutableStateOf("") }
    var inheritSelected: ModsComponent? by remember(existing) { mutableStateOf(null) }

    var useSelected: ModsComponent? by remember(existing) { mutableStateOf(null) }

    var showSnapshots by remember(existing) { mutableStateOf(false) }
    var versions: List<MinecraftVersion> by remember(showSnapshots) { mutableStateOf(emptyList()) }

    var creationStatus: Status? by remember(existing) { mutableStateOf(null) }

    val valid = remember(mode, newName, newVersion, newType, alternateLoader, inheritName, inheritSelected, useSelected) {
        when(mode) {
            CreationMode.NEW -> newName.isNotBlank() && newVersion != null && newType != null
            CreationMode.INHERIT -> inheritName.isNotBlank() && inheritSelected != null
            CreationMode.USE -> useSelected != null
        }
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
            toDisplayString = { name },
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
                toDisplayString = { name },
                enabled = mode == CreationMode.USE
            )
        }

        if(showCreate) {
            Button(
                enabled = valid,
                onClick = {
                    if(valid) {
                        val creator = ModsCreator(
                            AppContext.files.modsManifest
                        ) { creationStatus = it }

                        val additionalLoader = if(newType == VersionType.QUILT && alternateLoader) VersionType.FABRIC else null
                        val data = ModsCreationData(
                            name = when(mode) {
                                CreationMode.NEW -> newName
                                CreationMode.INHERIT -> inheritName
                                CreationMode.USE -> ""
                            },
                            types = newType?.let { additionalLoader?.let { add -> listOf(it.id, add.id) }?: listOf(it.id) } ?: emptyList(),
                            versions = newVersion?.let { listOf(it.id) } ?: emptyList()
                        )

                        Thread {
                            try {
                                val component = when (mode) {
                                    CreationMode.NEW -> creator.new(data)
                                    CreationMode.INHERIT -> creator.inherit(inheritSelected!!, data)
                                    CreationMode.USE -> creator.use(useSelected!!)
                                }
                                onDone(component)
                            } catch (e: IOException) {
                                AppContext.error(e)
                            }
                        }.start()
                    }
                }
            ) {
                Text(strings().creator.buttonCreate())
            }
        }

        creationStatus?.let {
            CreationPopup(it)
        }
    }
}