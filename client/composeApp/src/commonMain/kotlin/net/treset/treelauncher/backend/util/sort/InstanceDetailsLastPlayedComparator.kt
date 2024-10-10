package net.treset.treelauncher.backend.util.sort

import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.localization.strings

class InstanceDetailsLastPlayedComparator : Comparator<InstanceData> {
    override fun compare(o1: InstanceData, o2: InstanceData): Int {
        val firstLastPlayed = o1.instance.lastUsed
        val secondLastPlayed = o2.instance.lastUsed

        if (firstLastPlayed == secondLastPlayed) {
            return 0
        }
        return secondLastPlayed.compareTo(firstLastPlayed)
    }

    override fun toString(): String = strings().sortBox.sort.lastPlayed()
}
