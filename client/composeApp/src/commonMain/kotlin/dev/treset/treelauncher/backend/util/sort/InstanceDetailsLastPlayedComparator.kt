package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.localization.Strings

class InstanceDetailsLastPlayedComparator : Comparator<InstanceData> {
    override fun compare(o1: InstanceData, o2: InstanceData): Int {
        val firstLastPlayed = o1.instance.lastUsed.value
        val secondLastPlayed = o2.instance.lastUsed.value

        if (firstLastPlayed == secondLastPlayed) {
            return 0
        }
        return secondLastPlayed.compareTo(firstLastPlayed)
    }

    override fun toString(): String = Strings.sortBox.sort.lastPlayed()
}
