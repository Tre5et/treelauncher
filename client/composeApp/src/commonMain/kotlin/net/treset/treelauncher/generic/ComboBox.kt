package net.treset.treelauncher.generic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.style.icons

@Composable
fun <T> ComboBox(
    items: () -> List<T>,
    onSelected: (T) -> Unit = {},
    placeholder: String = "",
    loadingPlaceholder: String = "",
    defaultSelected: T? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem: T? by remember { mutableStateOf(defaultSelected) }
    var actualItems: List<T>? by remember { mutableStateOf(null) }
    val displayString = actualItems?.let{ selectedItem?.toString() ?: placeholder } ?: loadingPlaceholder

    LaunchedEffect(Unit) {
        Thread {
            actualItems = items()
        }.start()
    }


    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart),
    ) {

        val rowModifier = actualItems?.let {
            Modifier.clickable(onClick = { expanded = true })
        } ?: Modifier

        val borderColor by animateColorAsState(
            if(expanded) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            }
        )

        val borderWidth by animateDpAsState(
            if(expanded) { 2.dp } else { 1.dp }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = rowModifier
                .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
                .padding(start = 12.dp, bottom = 9.dp, top = 6.dp, end = 6.dp)
        ) {
            Text(
                displayString
            )
            Icon(
                icons().ArrowDropDown,
                "Open",
                modifier = Modifier.offset(y = 2.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            actualItems?.forEach { i ->
                DropdownMenuItem(
                    text = {
                        Text(
                            i.toString()
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
    defaultSelected: T? = null
) = ComboBox(
    {items},
    onSelected,
    placeholder,
    defaultSelected = defaultSelected
)

@Composable
fun <T> ComboBox(
    items: List<T>,
    onSelected: (T) -> Unit = {},
    placeholder: String = "",
    defaultSelected: Int? = null
) = ComboBox(
    items,
    onSelected,
    placeholder,
    defaultSelected?.let{if(it >= 0 && it < items.size) items[it] else null }
)

@Composable
fun <T> TitledComboBox(
    title: String,
    items: List<T>,
    onSelected: (T) -> Unit = {},
    placeholder: String = "",
    defaultSelected: T? = null
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
            defaultSelected
        )
    }

}

@Composable
fun <T> TitledComboBox(
    title: String,
    items: List<T>,
    onSelected: (T) -> Unit = {},
    placeholder: String = "",
    defaultSelected: Int? = null
) = TitledComboBox(
    title,
    items,
    onSelected,
    placeholder,
    defaultSelected?.let{if(it >= 0 && it < items.size) items[it] else null }
)