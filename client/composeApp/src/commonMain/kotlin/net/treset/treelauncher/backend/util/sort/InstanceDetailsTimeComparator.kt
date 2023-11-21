package net.treset.treelauncher.backend.util.sort

import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.localization.strings

class InstanceDetailsTimeComparator : Comparator<InstanceData> {
    override fun compare(o1: InstanceData, o2: InstanceData): Int {
        return (o2.instance.second.totalTime - o1.instance.second.totalTime).toInt()
    }

    override fun toString(): String = strings().sorts.time()
}
