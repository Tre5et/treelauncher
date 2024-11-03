package dev.treset.treelauncher.components.instances

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
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.generic.SelectorButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons

@Composable
fun InstanceButton(
    component: InstanceComponent,
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
                component.name.value,
                style = MaterialTheme.typography.titleMedium
            )
            Text(component.versionComponents.value[0].name.value)
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
                    Strings.units.accurateTime(component.totalTime.value)
                } else {
                    Strings.units.approxTime(component.totalTime.value)
                }
            )
            Icon(
                icons().time,
                "Time Played"
            )
        }
    }
}