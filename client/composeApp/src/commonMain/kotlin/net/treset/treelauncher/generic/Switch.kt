package net.treset.treelauncher.generic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.style.disabledContent

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = androidx.compose.material3.Switch(
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier
        .requiredHeight(24.dp)
        .requiredWidth(40.dp)
        .scale(0.7f)
        .pointerHoverIcon(PointerIcon.Hand),
    thumbContent = thumbContent,
    enabled = enabled,
    colors = colors,
    interactionSource = interactionSource
)

@Composable
fun TitledSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit) = {_ ->},
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier
            .padding(start = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            }
    ) {
        Text(
            title,
            style = style,
            color = LocalContentColor.current.let { if(enabled) it else it.disabledContent() },
            modifier = Modifier.padding(end = 4.dp),
            textAlign = TextAlign.Start
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.offset(y = (-2).dp),
            thumbContent = thumbContent,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
        )
    }
}