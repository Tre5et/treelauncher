package net.treset.treelauncher.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.strings


open class CreationState<T> (
    val mode: CreationMode,
    val name: String?,
    val existing: T?
) {
    open fun isValid(): Boolean = when(mode) {
        CreationMode.NEW -> !name.isNullOrBlank()
        CreationMode.INHERIT -> !name.isNullOrBlank() && existing != null
        CreationMode.USE -> existing != null
    }

    companion object {
        fun <T> of(
            mode: CreationMode,
            newName: String?,
            inheritName: String?,
            inheritSelected: T?,
            useSelected: T?
        ): CreationState<T> = CreationState(
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
            }
        )
    }
}

@Composable
fun <T> ComponentCreator(
    existing: List<T>,
    allowUse: Boolean = true,
    showCreate: Boolean = true,
    toDisplayString: T.() -> String = { toString() },
    onCreate: (CreationState<T>) -> Unit = { _->},
    setCurrentState: (CreationState<T>) -> Unit = {_->}
) {
    var mode by remember(existing) { mutableStateOf(CreationMode.NEW) }

    var newName by remember(existing) { mutableStateOf("") }

    var inheritName by remember(existing) { mutableStateOf("") }
    var inheritSelected: T? by remember(existing) { mutableStateOf(null) }

    var useSelected: T? by remember(existing) { mutableStateOf(null) }

    setCurrentState(
        CreationState.of(
            mode,
            newName,
            inheritName,
            inheritSelected,
            useSelected
        )
    )

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
            selected = inheritSelected,
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
                selected = useSelected,
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
                    CreationState.of(
                        mode,
                        newName,
                        inheritName,
                        inheritSelected,
                        useSelected
                    ).let {
                        if(it.isValid()) onCreate(it)
                    }
                }
            ) {
                Text(strings().creator.buttonCreate())
            }
        }
    }
}

enum class CreationMode {
    NEW,
    INHERIT,
    USE
}