package net.treset.treelauncher.generic

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextBox(
    text: String = "",
    onTextChanged: (String) -> Unit,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    prefix: @Composable () -> Unit = {},
    suffix: @Composable () -> Unit = {},
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    inputAcceptable: (String) -> Boolean = { true },
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    OutlinedTextField(
        value = text,
        onValueChange = {
            if(inputAcceptable(it)) {
                onTextChanged(it)
            }
        },
        placeholder = { Text(placeholder) },
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        interactionSource = interactionSource,
        prefix = prefix,
        suffix = suffix,
        isError = isError
    )
}