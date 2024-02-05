package net.treset.treelauncher.backend.util.sort

import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.localization.strings

class InstanceDetailsLastPlayedComparator : Comparator<InstanceData> {
    override fun compare(o1: InstanceData, o2: InstanceData): Int {
        val firstLastPlayed = o1.instance.first.lastUsed ?: o1.instance.second.lastPlayed
        val secondLastPlayed = o2.instance.first.lastUsed ?: o2.instance.second.lastPlayed

        if (firstLastPlayed == secondLastPlayed) {
            return 0
        }
        if (firstLastPlayed == null) {
            return 1
        }
        return if (secondLastPlayed == null) {
            -1
        } else secondLastPlayed.compareTo(firstLastPlayed)
    }

    override fun toString(): String = strings().sortBox.sort.lastPlayed()
}
