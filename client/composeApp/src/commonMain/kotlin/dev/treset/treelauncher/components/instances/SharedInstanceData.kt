package dev.treset.treelauncher.components.instances

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.components.SharedComponentData

class SharedInstanceData(
    component: InstanceComponent,
    reload: () -> Unit,
    val headerContent: MutableState<@Composable RowScope.() -> Unit> = mutableStateOf({})
) : SharedComponentData<InstanceComponent>(
    component,
    reload
) {
    companion object {
        fun of(component: InstanceComponent, reload: () -> Unit) = SharedInstanceData(component, reload)
    }
}