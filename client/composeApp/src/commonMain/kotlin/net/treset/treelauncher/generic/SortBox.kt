package net.treset.treelauncher.generic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

@Composable
fun <T> SortBox(
    sorts: List<T>,
    reversed: Boolean,
    selected: T,
    onSelected: (T) -> Unit,
    onReversed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation: Float by animateFloatAsState(if(reversed) 180f else 0f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium
        ) {
            ComboBox(
                items = sorts,
                onSelected = onSelected,
                selected = selected,
                decorated = false
            )
            IconButton(
                onClick = onReversed,
                icon = icons().sort,
                modifier = Modifier.rotate(rotation),
                tooltip = strings().sortBox.reverse()
            )
        }
    }
}