package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.localization.strings

class InstanceDetailsNameComparator : Comparator<InstanceData> {
    override fun compare(e1: InstanceData, e2: InstanceData): Int {
        return e1.instance.name.lowercase().compareTo(e2.instance.name.lowercase())
    }

    override fun toString(): String = strings().sortBox.sort.name()
}
