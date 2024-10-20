package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.localization.Strings

class InstanceDetailsNameComparator : Comparator<InstanceData> {
    override fun compare(e1: InstanceData, e2: InstanceData): Int {
        return e1.instance.name.value.lowercase().compareTo(e2.instance.name.value.lowercase())
    }

    override fun toString(): String = Strings.sortBox.sort.name()
}
