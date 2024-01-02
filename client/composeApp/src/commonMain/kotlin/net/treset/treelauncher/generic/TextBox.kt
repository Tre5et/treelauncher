package net.treset.treelauncher.generic

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.style.icons

@Composable
fun TextBox(
    text: String = "",
    onChange: (String) -> Unit,
    placeholder: String = "",
    enabled: Boolean = true,
    showClear: Boolean = true,
    singleLine: Boolean = true,
    prefix: @Composable () -> Unit = {},
    suffix: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var displayText by remember { mutableStateOf(text) }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = text,
        onValueChange = {
            displayText = it
            onChange(it)
        },
        placeholder = { Text(placeholder) },
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        trailingIcon = {
            if (showClear && isFocused && displayText.isNotEmpty()) {
                IconButton(onClick = {
                    displayText = ""
                    onChange("")
                }) {
                    Icon(
                        imageVector = icons().clear,
                        contentDescription = "Clear"
                    )
                }
            }
        },
        singleLine = singleLine,
        interactionSource = interactionSource,
        prefix = prefix,
        suffix = suffix
    )
}