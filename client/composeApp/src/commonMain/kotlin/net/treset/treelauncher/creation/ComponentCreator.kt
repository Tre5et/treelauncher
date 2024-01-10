package net.treset.treelauncher.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.generic.ComboBox
import net.treset.treelauncher.generic.TextBox
import net.treset.treelauncher.generic.TitledRadioButton
import net.treset.treelauncher.localization.strings

@Composable
fun <T> ComponentCreator(
    existing: List<T>,
    allowUse: Boolean = true,
    showCreate: Boolean = true,
    toDisplayString: T.() -> String = { toString() },
    onCreate: (CreationMode, String?, T?) -> Unit = { _, _, _->}
): () -> Triple<CreationMode, String?, T?> {
    var mode by remember(existing) { mutableStateOf(CreationMode.NEW) }

    var newName by remember(existing) { mutableStateOf("") }

    var inheritName by remember(existing) { mutableStateOf("") }
    var inheritSelected: T? by remember(existing) { mutableStateOf(null) }

    var useSelected: T? by remember(existing) { mutableStateOf(null) }

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
            toDisplayString = toDisplayString,
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
                defaultSelected = useSelected,
                onSelected = {
                    useSelected = it
                },
                placeholder = strings().creator.component(),
                toDisplayString = toDisplayString,
                enabled = mode == CreationMode.USE
            )
        }

        if(showCreate) {
            Button(
                enabled = when(mode) {
                    CreationMode.NEW -> newName.isNotBlank()
                    CreationMode.INHERIT -> inheritName.isNotBlank() && inheritSelected != null
                    CreationMode.USE -> useSelected != null
                },
                onClick = {
                    toTriple(mode, newName, inheritName, inheritSelected, useSelected).let {
                        onCreate(it.first, it.second, it.third)
                    }
                }
            ) {
                Text(strings().creator.buttonCreate())
            }
        }
    }

    return { toTriple(mode, newName, inheritName, inheritSelected, useSelected) }
}

private fun <T> toTriple(
    mode: CreationMode,
    newName: String,
    inheritName: String,
    inheritSelected: T?,
    useSelected: T?
): Triple<CreationMode, String?, T?> = Triple(
    mode,
    when(mode) {
        CreationMode.NEW -> newName.ifBlank { null }
        CreationMode.INHERIT -> inheritName.ifBlank { null }
        CreationMode.USE -> null
    },
    when(mode) {
        CreationMode.NEW -> null
        CreationMode.INHERIT -> inheritSelected
        CreationMode.USE -> useSelected
    }
)

enum class CreationMode {
    NEW,
    INHERIT,
    USE
}