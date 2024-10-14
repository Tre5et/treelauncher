package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.localization.strings

class InstanceDetailsTimeComparator : Comparator<InstanceData> {
    override fun compare(o1: InstanceData, o2: InstanceData): Int {
        return (o2.instance.totalTime - o1.instance.totalTime).toInt()
    }

    override fun toString(): String = strings().sortBox.sort.time()
}
