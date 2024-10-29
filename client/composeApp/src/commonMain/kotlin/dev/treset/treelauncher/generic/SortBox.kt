package dev.treset.treelauncher.generic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import dev.treset.treelauncher.backend.util.sort.Sort
import dev.treset.treelauncher.backend.util.sort.SortProvider
import dev.treset.treelauncher.backend.util.toggle
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons

@Composable
fun <T> SortBox(
    sorts: List<SortProvider<T>>,
    sort: Sort<T>,
    onSelected: (SortProvider<T>) -> Unit = {},
    onReversed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val rotation: Float by animateFloatAsState(if(sort.reverse.value) 180f else 0f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium
        ) {
            ComboBox(
                items = sorts,
                onSelected = {
                    sort.provider.value = it
                    onSelected(it)
                },
                selected = sort.provider.value,
                decorated = false
            )
            IconButton(
                onClick = {
                    sort.reverse.toggle()
                    onReversed()
                },
                icon = icons().sort,
                modifier = Modifier.rotate(rotation),
                tooltip = Strings.sortBox.reverse()
            )
        }
    }
}