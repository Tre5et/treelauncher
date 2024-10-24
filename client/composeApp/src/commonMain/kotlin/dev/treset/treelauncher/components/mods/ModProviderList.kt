package dev.treset.treelauncher.components.mods

import androidx.compose.runtime.mutableStateOf
import dev.treset.mcdl.mods.ModProvider
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import kotlinx.serialization.Serializable

typealias ModProviderList = MutableDataStateList<ModProviderData>

fun ModProviderList.canMoveUp(data: ModProviderData): Boolean {
    return indexOf(data) > 0
}

fun ModProviderList.canMoveDown(data: ModProviderData): Boolean {
    return indexOf(data) < size - 1
}

fun ModProviderList.moveUp(data: ModProviderData) {
    val index = indexOf(data)
    if (index > 0) {
        remove(data)
        add(index - 1, data)
    }
}

fun ModProviderList.moveDown(data: ModProviderData) {
    val index = indexOf(data)
    if (index < size - 1) {
        remove(data)
        add(index + 1, data)
    }
}

fun ModProviderList.moveApplicableDirection(data: ModProviderData) {
    if(canMoveDown(data)) {
        moveDown(data)
    } else if(canMoveDown(data)) {
        moveUp(data)
    }
}

fun ModProviderList.getEnabledCount(): Int {
    return count { it.enabled.value }
}

@Serializable
class ModProviderData(
    val provider: ModProvider,
    val enabled: MutableDataState<Boolean>
) {
    constructor(
        provider: ModProvider,
        enabled: Boolean
    ): this(provider, mutableStateOf(enabled))
}