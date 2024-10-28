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
    } else if(canMoveUp(data)) {
        moveUp(data)
    }
}

fun ModProviderList.contains(element: ModProvider): Boolean {
    return any { it.provider == element }
}

fun ModProviderList.containsAll(elements: Collection<ModProvider>): Boolean {
    return elements.all { contains(it) }
}

fun ModProviderList.getEnabled(): List<ModProvider> {
    return filter { it.enabled.value }.map { it.provider }
}

fun ModProviderList.deepCopy(): ModProviderList {
    val new = ModProviderList()
    new.addAll(map { ModProviderData(it.provider, it.enabled.value) })
    return new
}

fun ModProviderList.copyOrder(): ModProviderList {
    val new = ModProviderList()
    new.addAll(map { ModProviderData(it.provider, true) })
    return new
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