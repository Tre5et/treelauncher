package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.localization.Strings

object InstanceComponentTimePlayedComparator : SortProvider<Component>() {
    override fun compare(o1: Component, o2: Component): Int {
        if (o1 !is InstanceComponent || o2 !is InstanceComponent) {
            return 0
        }
        return (o2.totalTime.value - o1.totalTime.value).toInt()
    }

    override val name: String
        get() = Strings.sortBox.sort.time()

    override val id = "TIME_PLAYED"
}