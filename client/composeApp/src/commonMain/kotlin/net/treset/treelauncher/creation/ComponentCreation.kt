package net.treset.treelauncher.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.ComponentCreator
import net.treset.treelauncher.backend.creation.CreationData
import net.treset.treelauncher.backend.data.manifest.Component
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings
import java.io.IOException

@Composable
fun <T: Component, C: ComponentCreator<T, CreationData>> ComponentCreator(
    existing: List<T>,
    getCreator: (onStatus: (Status) -> Unit) -> C,
    allowUse: Boolean = true,
    showCreate: Boolean = true,
    onDone: (T) -> Unit
) = ComponentCreator(
    existing = existing,
    allowUse = allowUse,
    showCreate = showCreate,
    getCreator = getCreator,
    getData = { CreationData(it ?: "") },
    onDone = onDone
)

@Composable
fun <T: Component, C: ComponentCreator<T, D>, D: CreationData> ComponentCreator(
    existing: List<T>,
    allowUse: Boolean = true,
    showCreate: Boolean = true,
    getCreator: (onStatus: (Status) -> Unit) -> C,
    getData: (name: String?) -> D,
    onDone: (T) -> Unit
) {
    var mode by remember(existing) { mutableStateOf(CreationMode.NEW) }

    var newName by remember(existing) { mutableStateOf("") }

    var inheritName by remember(existing) { mutableStateOf("") }
    var inheritSelected: T? by remember(existing) { mutableStateOf(null) }

    var useSelected: T? by remember(existing) { mutableStateOf(null) }

    var creationStatus: Status? by remember { mutableStateOf(null) }

    val valid = remember(existing, mode, newName, inheritName, inheritSelected, useSelected) {
        when(mode) {
            CreationMode.NEW -> newName.isNotBlank()
            CreationMode.INHERIT -> inheritName.isNotBlank() && inheritSelected != null
            CreationMode.USE -> useSelected != null
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

        if(allowUse) {
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
                        val creator = getCreator {
                            creationStatus = it
                        }
                        val data = getData(
                            when(mode) {
                                CreationMode.NEW -> newName
                                CreationMode.INHERIT -> inheritName
                                CreationMode.USE -> null
                            }
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

enum class CreationMode {
    NEW,
    INHERIT,
    USE
}