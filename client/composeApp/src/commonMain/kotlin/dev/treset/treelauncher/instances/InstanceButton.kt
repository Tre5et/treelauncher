package dev.treset.treelauncher.instances

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.generic.SelectorButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons

@Composable
fun InstanceButton(
    instance: InstanceData,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    SelectorButton(
        selected = selected,
        onClick = onClick,
        enabled = enabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                instance.instance.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(instance.versionComponents[0].name)
        }

        val interactionSource = remember { MutableInteractionSource() }
        val hovered by interactionSource.collectIsHoveredAsState()
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .hoverable(interactionSource),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                if(hovered) {
                    Strings.units.accurateTime(instance.instance.totalTime)
                } else {
                    Strings.units.approxTime(instance.instance.totalTime)
                }
            )
            Icon(
                icons().time,
                "Time Played"
            )
        }
    }
}