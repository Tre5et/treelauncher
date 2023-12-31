package net.treset.treelauncher.instances

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.util.string.TimeString
import net.treset.treelauncher.generic.SelectorButton
import net.treset.treelauncher.style.icons

@Composable
fun InstanceButton(
    instance: InstanceData,
    selected: Boolean,
    onClick: () -> Unit
) {
    SelectorButton(
        selected = selected,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                instance.instance.first.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(instance.versionComponents[0].second.versionId)
        }

        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                TimeString(instance.instance.second.totalTime).get()
            )
            Icon(
                icons().time,
                "Time Played"
            )
        }
    }
}