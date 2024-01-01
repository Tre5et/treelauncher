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
import net.treset.treelauncher.style.icons

@Composable
fun <T> ComboBox(
    items: () -> List<T>,
    onSelected: (T) -> Unit = {},
    placeholder: String = "",
    loadingPlaceholder: String = "",
    defaultSelected: T? = null,
    toDisplayString: T.() -> String = { toString() },
    decorated: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem: T? by remember { mutableStateOf(defaultSelected) }
    var actualItems: List<T>? by remember { mutableStateOf(null) }
    val displayString = actualItems?.let{ selectedItem?.toDisplayString() ?: placeholder } ?: loadingPlaceholder

    LaunchedEffect(items) {
        Thread {
            actualItems = items()
        }.start()

        selectedItem = defaultSelected
    }

    LaunchedEffect(defaultSelected) {
        selectedItem = defaultSelected
    }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .pointerHoverIcon(PointerIcon.Hand),
    ) {
        val borderColor by animateColorAsState(
            if(expanded) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            }
        )

        val borderWidth by animateDpAsState(
            if(expanded) 2.dp else 1.dp
        )

        val rowModifier = (actualItems?.let {
            Modifier.clickable(onClick = { expanded = true })
        } ?: Modifier).let {
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
                displayString
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
            actualItems?.forEach { i ->
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
    defaultSelected: T? = null,
    displayTransformer: T.() -> String = { toString() },
    decorated: Boolean = true
) = ComboBox(
    {items},
    onSelected,
    placeholder,
    defaultSelected = defaultSelected,
    toDisplayString = displayTransformer,
    decorated = decorated
)

@Composable
fun <T> ComboBox(
    items: List<T>,
    onSelected: (T) -> Unit = {},
    placeholder: String = "",
    defaultSelected: Int? = null,
    displayTransformer: T.() -> String = { toString() },
    decorated: Boolean = true
) = ComboBox(
    items,
    onSelected,
    placeholder,
    defaultSelected?.let{if(it >= 0 && it < items.size) items[it] else null },
    displayTransformer,
    decorated
)

@Composable
fun <T> TitledComboBox(
    title: String,
    items: List<T>,
    onSelected: (T) -> Unit = {},
    placeholder: String = "",
    defaultSelected: T? = null,
    displayTransformer: T.() -> String = { toString() },
    decorated: Boolean = true
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
            defaultSelected,
            displayTransformer,
            decorated
        )
    }

}