package dev.treset.treelauncher.backend.util.sort

import androidx.compose.runtime.mutableStateOf
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import kotlinx.serialization.Serializable

@Serializable
class Sort<T>(
    val provider: MutableDataState<SortProvider<T>>,
    val reverse: MutableDataState<Boolean>
) {
    constructor(
        type: SortProvider<T>,
        reverse: Boolean
    ): this(
        mutableStateOf(type),
        mutableStateOf(reverse)
    )

    fun <E: T> sort(list: List<E>): List<E> {
        val new = list.sortedWith(provider.value).let {
            if (reverse.value) {
                it.reversed()
            } else {
                it
            }
        }
        return new
    }
}

fun <T> List<T>.sorted(sort: Sort<in T>): List<T> {
    return sort.sort(this)
}

val sortObjects: Set<SortProvider<*>> = setOf(
    ComponentNameComparator,
    ComponentLastUsedComparator,
    InstanceComponentTimePlayedComparator,
    LauncherModNameComparator,
    LauncherModEnabledNameComparator
)

val sortProviders: Map<String, SortProvider<*>> = sortObjects.associateBy { it.id }


val ComponentSortProviders: List<SortProvider<Component>> = listOf(
    ComponentNameComparator,
    ComponentLastUsedComparator,
)

val InstanceSortProviders: List<SortProvider<Component>> = listOf(
    ComponentNameComparator,
    ComponentLastUsedComparator,
    InstanceComponentTimePlayedComparator,
)

val LauncherModSortProviders: List<SortProvider<LauncherMod>> = listOf(
    LauncherModNameComparator,
    LauncherModEnabledNameComparator
)