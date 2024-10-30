package dev.treset.treelauncher.components

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.treset.treelauncher.backend.data.manifest.Component

open class SharedComponentData<T: Component>(
    val component: T,
    var reload: () -> Unit,
    val settingsOpen: MutableState<Boolean> = mutableStateOf(false),
) {
    companion object {
        fun <T: Component> of(component: T, reload: () -> Unit): SharedComponentData<T> = SharedComponentData(component, reload)
    }
}