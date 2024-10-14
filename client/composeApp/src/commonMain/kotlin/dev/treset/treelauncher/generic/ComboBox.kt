package dev.treset.treelauncher.generic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.style.disabledContainer
import dev.treset.treelauncher.style.disabledContent
import dev.treset.treelauncher.style.hovered
import dev.treset.treelauncher.style.icons

@Composable
fun <T> ComboBox(
    items: List<T>,
    onSelected: (T?) -> Unit = {},
    placeholder: String = "",
    loading: Boolean = false,
    loadingPlaceholder: String = strings().comboBox.loading(),
    selected: T? = null,
    allowUnselect: Boolean,
    allowSearch: Boolean = false,
    toDisplayString: T.() -> String = { toString() },
    decorated: Boolean = true,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember(enabled) { mutableStateOf(false) }
    val displayString = remember(loading, loadingPlaceholder, selected, placeholder) { if(loading) loadingPlaceholder else selected?.toDisplayString() ?: placeholder }

    var search by remember { mutableStateOf("") }
    var actualItems: List<T> by remember(items) { mutableStateOf(emptyList()) }

    LaunchedEffect(items, search) {
        actualItems = items.filter { it.toDisplayString().contains(search, ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
            .pointerHoverIcon(PointerIcon.Hand),
    ) {
        val borderColor by animateColorAsState(
            if(enabled && !loading) {
                if (expanded) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            } else {
                MaterialTheme.colorScheme.outline.disabledContainer()
            }
        )

        val textColor by animateColorAsState(
            if(enabled && !loading)
                LocalContentColor.current
            else
                LocalContentColor.current.disabledContent()
        )

        val borderWidth by animateDpAsState(
            if(expanded && enabled) 2.dp else 1.dp
        )

        val rowModifier = remember(loading, enabled, decorated, borderWidth, borderColor) {
            if (loading || !enabled) {
                Modifier
            } else {
                Modifier.clickable(onClick = { expanded = true })
            }.let {
                if (decorated) {
                    it
                        .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
                        .padding(start = 12.dp, bottom = 9.dp, top = 6.dp, end = 6.dp)
                } else {
                    it
                }
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
                    modifier = Modifier.offset(y = 2.dp),
                    tint = textColor
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            //TODO: make lazy
            if(allowSearch) {
                TextBox(
                    text = search,
                    onTextChanged = { search = it },
                    placeholder = strings().comboBox.search(),
                    leadingIcon = {
                        Icon(
                            icons().search,
                            "Search",
                        )
                    },
                )
            }
            if(allowUnselect) {
                ComboBoxItem(
                    text = placeholder,
                    onClick = {
                        onSelected(null)
                        expanded = false
                    }
                )
            }
            actualItems.forEach { i ->
                ComboBoxItem(
                    text = i.toDisplayString(),
                    onClick = {
                        onSelected(i)
                        expanded = false
                    }
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
    selected: T? = null,
    toDisplayString: T.() -> String = { toString() },
    allowSearch: Boolean = false,
    decorated: Boolean = true,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) = ComboBox(
    items,
    { it?.let { e -> onSelected(e) } },
    placeholder,
    loading,
    loadingPlaceholder,
    allowUnselect = false,
    allowSearch = allowSearch,
    selected = selected,
    toDisplayString = toDisplayString,
    decorated = decorated,
    enabled = enabled,
    modifier = modifier
)

@Composable
fun <T> TitledComboBox(
    title: String,
    items: List<T>,
    onSelected: (T?) -> Unit = {},
    loading: Boolean = false,
    loadingPlaceholder: String = strings().creator.version.loading(),
    placeholder: String = "",
    selected: T? = null,
    allowUnselect: Boolean,
    allowSearch: Boolean = false,
    toDisplayString: T.() -> String = { toString() },
    decorated: Boolean = true,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        modifier = modifier
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
            selected,
            allowUnselect,
            allowSearch,
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
    selected: T? = null,
    allowSearch: Boolean = false,
    toDisplayString: T.() -> String = { toString() },
    decorated: Boolean = true,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) = TitledComboBox(
    title = title,
    items = items,
    onSelected = { it?.let { e -> onSelected(e) } },
    loading = loading,
    loadingPlaceholder = loadingPlaceholder,
    placeholder = placeholder,
    selected = selected,
    allowUnselect = false,
    allowSearch = allowSearch,
    toDisplayString = toDisplayString,
    decorated = decorated,
    enabled = enabled,
    modifier = modifier
)

@Composable
fun ComboBoxItem(
    text: String,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier
) {
    val hovered by interactionSource.collectIsHoveredAsState()

    val background = if (hovered)
            MaterialTheme.colorScheme.primary.hovered()
        else
            Color.Transparent

    val textColor = if (hovered)
            MaterialTheme.colorScheme.onPrimary
        else
            LocalContentColor.current

    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clip(RoundedCornerShape(4.dp))
            .background(background),
        colors = MenuDefaults.itemColors(
            textColor = textColor
        )
    )
}