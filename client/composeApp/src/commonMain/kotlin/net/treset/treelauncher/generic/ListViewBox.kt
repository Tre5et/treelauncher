package net.treset.treelauncher.generic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.style.icons

@Composable
fun <T> ListViewBox(
    displays: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium
        ) {
            ComboBox(
                items = displays,
                onSelected = onSelected,
                selected = selected,
                decorated = false
            )
            Icon(
                icons().list,
                selected.toString()
            )
        }
    }
}