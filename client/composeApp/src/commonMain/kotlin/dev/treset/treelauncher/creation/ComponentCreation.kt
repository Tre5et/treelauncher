package dev.treset.treelauncher.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.creation.ComponentCreator
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import java.io.IOException

open class CreationContent<T: Component>(
    val mode: CreationMode,
    val newName: String?,
    val inheritName: String?,
    val inheritComponent: T?,
    val useComponent: T?
) {
    open fun isValid(): Boolean {
        return when(mode) {
            CreationMode.NEW -> !newName.isNullOrBlank()
            CreationMode.INHERIT -> !inheritName.isNullOrBlank() && inheritComponent != null
            CreationMode.USE -> useComponent != null
        }
    }

    fun isNotValid(): Boolean {
        return !isValid()
    }
}

@Composable
fun <T: Component, C: ComponentCreator<T, *>> ComponentCreator(
    components: MutableList<T>,
    allowUse: Boolean = true,
    showCreate: Boolean = true,
    getCreator: (content: CreationContent<T>, onStatus: (Status) -> Unit) -> C,
    defaultMode: CreationMode = CreationMode.NEW,
    defaultNewName: String = "",
    defaultInheritName: String = "",
    defaultInheritComponent: T? = null,
    defaultUseComponent: T? = null,
    setExecute: (((onStatus: (Status) -> Unit) -> T)?) -> Unit = {},
    setContent: (CreationContent<T>) -> Unit = {},
    onDone: (T) -> Unit = {}
) {
    @Suppress("NAME_SHADOWING")
    val components = components.toList()

    var mode by remember(defaultMode) { mutableStateOf(defaultMode) }
    var newName by remember(defaultNewName) {
        mutableStateOf(defaultNewName)
    }

    var inheritName by remember(defaultInheritName) { mutableStateOf(defaultInheritName) }
    var inheritSelected: T? by remember(components, defaultInheritComponent) { mutableStateOf(defaultInheritComponent) }

    var useSelected: T? by remember(components, defaultUseComponent) { mutableStateOf(defaultUseComponent) }

    var creationStatus: Status? by remember { mutableStateOf(null) }

    val creationContent: CreationContent<T> = remember(mode, newName, inheritName, inheritSelected, useSelected) {
        CreationContent(
            mode = mode,
            newName = newName,
            inheritName = inheritName,
            inheritComponent = inheritSelected,
            useComponent = useSelected
        ).also {
            setContent(it)
        }
    }

    val execute: (onStatus: (Status) -> Unit) -> T = remember(creationContent) {
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
                throw IOException("Unable to create component", e)
            }
        }
    }

    val valid = remember(creationContent) {
        creationContent.isValid().also {
            setExecute(
                if(it) execute else null
            )
        }
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
            items = components,
            selected = inheritSelected,
            onSelected = {
                inheritSelected = it
            },
            placeholder = Strings.creator.component(),
            toDisplayString = { name },
            enabled = mode == CreationMode.INHERIT
        )

        if(allowUse) {
            TitledRadioButton(
                text = Strings.creator.radioUse(),
                selected = mode == CreationMode.USE,
                onClick = { mode = CreationMode.USE }
            )
            ComboBox(
                items = components,
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

enum class CreationMode {
    NEW,
    INHERIT,
    USE
}