package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.localization.Strings

class InstanceComponentTimePlayedComparator : Comparator<InstanceComponent> {
    override fun compare(o1: InstanceComponent, o2: InstanceComponent): Int {
        return (o2.totalTime.value - o1.totalTime.value).toInt()
    }

    override fun toString(): String = Strings.sortBox.sort.time()
}