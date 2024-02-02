package net.treset.treelauncher.backend.util.sort

import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.localization.strings

class InstanceDetailsNameComparator : Comparator<InstanceData> {
    override fun compare(e1: InstanceData, e2: InstanceData): Int {
        return e1.instance.first.name.lowercase().compareTo(e2.instance.first.name.lowercase())
    }

    override fun toString(): String = strings().sortBox.sort.name()
}
