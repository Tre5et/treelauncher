package net.treset.treelauncher.backend.util.sort

import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.localization.strings

class InstanceDetailsLastPlayedComparator : Comparator<InstanceData> {
    override fun compare(o1: InstanceData, o2: InstanceData): Int {
        if (o1.instance.second.lastPlayed == o2.instance.second.lastPlayed) {
            return 0
        }
        if (o1.instance.second.lastPlayed == null) {
            return 1
        }
        return if (o2.instance.second.lastPlayed == null) {
            -1
        } else o2.instance.second.lastPlayed.compareTo(o1.instance.second.lastPlayed)
    }

    override fun toString(): String = strings().sorts.lastPlayed()
}
