package dev.treset.treelauncher.components.mods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.creation.*
import dev.treset.treelauncher.backend.data.manifest.ModsComponent
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.creation.CreationContent
import dev.treset.treelauncher.creation.CreationMode
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import java.io.IOException

class ModsCreationContent(
    mode: CreationMode,
    newName: String?,
    val newTypes: List<String>,
    val newVersions: List<String>,
    inheritName: String?,
    inheritComponent: ModsComponent?,
    useComponent: ModsComponent?
) : CreationContent<ModsComponent>(
    mode = mode,
    newName = newName,
    inheritName = inheritName,
    inheritComponent = inheritComponent,
    useComponent = useComponent
) {
    override fun isValid(): Boolean {
        return when(mode) {
            CreationMode.NEW -> super.isValid() && newTypes.isNotEmpty() && newVersions.isNotEmpty()
            else -> super.isValid()
        }
    }
}

@Composable
fun ModsCreation(
    existing: List<ModsComponent>,
    showCreate: Boolean = true,
    showUse: Boolean = true,
    defaultMode: CreationMode = CreationMode.NEW,
    defaultNewName: String = "",
    defaultVersion: String? = null,
    defaultType: VersionType? = null,
    defaultAlternateLoader: Boolean = true,
    defaultInheritName: String = "",
    defaultInheritComponent: ModsComponent? = null,
    defaultUseComponent: ModsComponent? = null,
    getCreator: (content: ModsCreationContent, onStatus: (Status) -> Unit) -> ComponentCreator<ModsComponent, *> = ModsCreator::get,
    setExecute: (((onStatus: (Status) -> Unit) -> ModsComponent)?) -> Unit = {},
    setContent: (ModsCreationContent) -> Unit = {},
    onDone: (ModsComponent) -> Unit = { _->}
) {
    var mode by remember(existing, defaultMode) { mutableStateOf(defaultMode) }

    var newName by remember(existing, defaultNewName) { mutableStateOf(defaultNewName) }

    var newVersion: MinecraftVersion? by remember(existing) { mutableStateOf(null) }

    var newType: VersionType? by remember(existing, defaultType) { mutableStateOf(defaultType) }
    var alternateLoader by remember(existing, defaultAlternateLoader) { mutableStateOf(defaultAlternateLoader) }

    var inheritName by remember(existing, defaultInheritName) { mutableStateOf(defaultInheritName) }
    var inheritSelected: ModsComponent? by remember(existing, defaultInheritComponent) { mutableStateOf(defaultInheritComponent) }

    var useSelected: ModsComponent? by remember(existing, defaultUseComponent) { mutableStateOf(defaultUseComponent) }

    var showSnapshots by remember(existing) { mutableStateOf(false) }
    var versions: List<MinecraftVersion> by remember(showSnapshots) { mutableStateOf(emptyList()) }

    var creationStatus: Status? by remember(existing) { mutableStateOf(null) }

    val creationContent: ModsCreationContent = remember(existing, mode, newName, newVersion, newType, alternateLoader, inheritName, inheritSelected, useSelected) {
        val additionalLoader = if (newType == VersionType.QUILT && alternateLoader) VersionType.FABRIC else null

        ModsCreationContent(
            mode = mode,
            newName = newName,
            newTypes = newType?.let { additionalLoader?.let { add -> listOf(it.id, add.id) } ?: listOf(it.id) }
                ?: emptyList(),
            newVersions = newVersion?.let { listOf(it.id) } ?: emptyList(),
            inheritName = inheritName,
            inheritComponent = inheritSelected,
            useComponent = useSelected
        ).also {
            setContent(it)
        }
    }

    val execute: (onStatus: (Status) -> Unit) -> ModsComponent = remember(creationContent) {
        { onStatus ->
            if(!creationContent.isValid()) {
                throw IOException("Invalid version creation content")
            }

            val creator = getCreator(
                creationContent,
                onStatus
            )

            try {
                val component = creator.create()
                onDone(component)
                component
            } catch (e: IOException) {
                throw IOException("Unable to create mods component", e)
            }
        }
    }

    val valid = remember(mode, newName, newVersion, newType, alternateLoader, inheritName, inheritSelected, useSelected) {
        when(mode) {
            CreationMode.NEW -> newName.isNotBlank() && newVersion != null && newType != null
            CreationMode.INHERIT -> inheritName.isNotBlank() && inheritSelected != null
            CreationMode.USE -> useSelected != null
        }.also { setExecute(if(it) execute else null) }
    }

    LaunchedEffect(showSnapshots, defaultVersion) {
        try {
            versions = (if(versions.isEmpty()) {
                if (showSnapshots) {
                    MinecraftVersion.getAll()
                } else {
                    MinecraftVersion.getAll().filter { it.isRelease }
                }
            } else {
                versions
            }).also {
                if(defaultVersion != null) {
                    it.firstOrNull { it.id == defaultVersion }?.let {
                        newVersion = it
                    }
                }
            }
        } catch(_: IOException) {}
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TitledRadioButton(
            text = Strings.creator.radioCreate(),
            selected = mode == CreationMode.NEW,
            onClick = { mode = CreationMode.NEW }
        )
        TextBox(
            text = newName,
            onTextChanged = {
                newName = it
            },
            placeholder = Strings.creator.name(),
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
                placeholder = Strings.creator.mods.version(),
                loading = versions.isEmpty(),
                toDisplayString = { id },
                enabled = mode == CreationMode.NEW
            )

            TitledCheckBox(
                title = Strings.creator.version.showSnapshots(),
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
                placeholder = Strings.creator.mods.type(),
                enabled = mode == CreationMode.NEW
            )
            if(newType == VersionType.QUILT) {
                TitledCheckBox(
                    title = Strings.creator.mods.quiltIncludeFabric(),
                    checked = alternateLoader,
                    onCheckedChange = {
                        alternateLoader = it
                    },
                )
            }
        }

        TitledRadioButton(
            text = Strings.creator.radioInherit(),
            selected = mode == CreationMode.INHERIT,
            onClick = { mode = CreationMode.INHERIT }
        )
        TextBox(
            text = inheritName,
            onTextChanged = {
                inheritName = it
            },
            placeholder = Strings.creator.name(),
            enabled = mode == CreationMode.INHERIT
        )
        ComboBox(
            items = existing,
            selected = inheritSelected,
            onSelected = {
                inheritSelected = it
            },
            placeholder = Strings.creator.component(),
            toDisplayString = { name },
            enabled = mode == CreationMode.INHERIT
        )

        if(showUse) {
            TitledRadioButton(
                text = Strings.creator.radioUse(),
                selected = mode == CreationMode.USE,
                onClick = { mode = CreationMode.USE }
            )
            ComboBox(
                items = existing,
                selected = useSelected,
                onSelected = {
                    useSelected = it
                },
                placeholder = Strings.creator.component(),
                toDisplayString = { name },
                enabled = mode == CreationMode.USE
            )
        }

        if(showCreate) {
            Button(
                enabled = valid,
                onClick = {
                    if(valid) {
                        Thread {
                            try {
                                execute {
                                    creationStatus = it
                                }
                            } catch (e: IOException) {
                                AppContext.error(e)
                            }
                        }.start()
                    }
                }
            ) {
                Text(Strings.creator.buttonCreate())
            }
        }

        creationStatus?.let {
            StatusPopup(it)
        }
    }
}

fun ModsCreator.get(content: ModsCreationContent, onStatus: (Status) -> Unit): ComponentCreator<ModsComponent, out CreationData> {
    return when(content.mode) {
        CreationMode.NEW -> new(
            NewModsCreationData(
                name = content.newName!!,
                types = content.newTypes,
                versions = content.newVersions,
                parent = AppContext.files.modsManifest
            ),
            onStatus
        )
        CreationMode.INHERIT -> inherit(
            InheritModsCreationData(
                name = content.inheritName!!,
                component = content.inheritComponent!!,
                parent = AppContext.files.modsManifest
            ),
            onStatus
        )
        CreationMode.USE -> use(
            UseModsCreationData(
                component = content.useComponent!!,
                parent = AppContext.files.modsManifest
            ),
            onStatus
        )
    }
}