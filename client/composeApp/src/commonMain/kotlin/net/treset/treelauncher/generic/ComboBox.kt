package net.treset.treelauncher.generic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun <T> ComboBox(
    items: List<T>,
    onSelected: (T?) -> Unit = {},
    placeholder: String = "",
    loading: Boolean = false,
    loadingPlaceholder: String = strings().creator.version.loading(),
    defaultSelected: T? = null,
    allowUnselect: Boolean,
    toDisplayString: T.() -> String = { toString() },
    decorated: Boolean = true,
    enabled: Boolean = true
) {
    var expanded by remember(enabled) { mutableStateOf(false) }
    var selectedItem: T? by remember { mutableStateOf(defaultSelected) }
    val displayString = if(loading) loadingPlaceholder else selectedItem?.toDisplayString() ?: placeholder

    LaunchedEffect(defaultSelected) {
        selectedItem = defaultSelected
        onSelected(defaultSelected)
    }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .pointerHoverIcon(PointerIcon.Hand),
    ) {
        val borderColor by animateColorAsState(
            if(enabled) {
                if (expanded) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            } else {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
            }
        )

        val textColor by animateColorAsState(
            if(enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
        )

        val borderWidth by animateDpAsState(
            if(expanded && enabled) 2.dp else 1.dp
        )

        val rowModifier = if(loading || !enabled) {
            Modifier
        } else {
            Modifier.clickable(onClick = { expanded = true })
        }.let {
            if(decorated) {
                it
                    .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
                    .padding(start = 12.dp, bottom = 9.dp, top = 6.dp, end = 6.dp)
            } else {
                it
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = rowModifier
        ) {
            Text(
                displayString,
                color = textColor
            )
            if(decorated) {
                Icon(
                    icons().comboBox,
                    "Open",
                    modifier = Modifier.offset(y = 2.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if(allowUnselect) {
                DropdownMenuItem(
                    text = {
                        Text(
                            placeholder
                        )
                    },
                    onClick = {
                        selectedItem = null
                        onSelected(null)
                        expanded = false
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }
            items.forEach { i ->
                DropdownMenuItem(
                    text = {
                        Text(
                            i.toDisplayString()
                        )
                    },
                    onClick = {
                        selectedItem = i
                        onSelected(i)
                        expanded = false
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }
        }
    }
}

@Composable
fun <T> ComboBox(
    items: List<T>,
    onSelected: (T) -> Unit = {},
    placeholder: String = "",
    loading: Boolean = false,
    loadingPlaceholder: String = strings().creator.version.loading(),
    defaultSelected: T? = null,
    toDisplayString: T.() -> String = { toString() },
    decorated: Boolean = true,
    enabled: Boolean = true
) = ComboBox(
    items,
    { it?.let { e -> onSelected(e) } },
    placeholder,
    loading,
    loadingPlaceholder,
    allowUnselect = false,
    defaultSelected = defaultSelected,
    toDisplayString = toDisplayString,
    decorated = decorated,
    enabled = enabled
)

@Composable
fun <T> TitledComboBox(
    title: String,
    items: List<T>,
    onSelected: (T?) -> Unit = {},
    loading: Boolean = false,
    loadingPlaceholder: String = strings().creator.version.loading(),
    placeholder: String = "",
    defaultSelected: T? = null,
    allowUnselect: Boolean,
    toDisplayString: T.() -> String = { toString() },
    decorated: Boolean = true,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium
        )

        ComboBox(
            items,
            onSelected,
            placeholder,
            loading,
            loadingPlaceholder,
            defaultSelected,
            allowUnselect,
            toDisplayString,
            decorated,
            enabled
        )
    }
}

@Composable
fun <T> TitledComboBox(
    title: String,
    items: List<T>,
    onSelected: (T) -> Unit = {},
    loading: Boolean = false,
    loadingPlaceholder: String = strings().creator.version.loading(),
    placeholder: String = "",
    defaultSelected: T? = null,
    toDisplayString: T.() -> String = { toString() },
    decorated: Boolean = true,
    enabled: Boolean = true
) = TitledComboBox(
    title,
    items,
    { onSelected(it!!) },
    loading,
    loadingPlaceholder,
    placeholder,
    defaultSelected,
    false,
    toDisplayString,
    decorated,
    enabled
)