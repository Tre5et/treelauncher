package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.Strings

class ComponentManifestLastUsedComparator : Comparator<Component> {
    override fun compare(o1: Component, o2: Component): Int {
        if (o1.lastUsed.value == o2.lastUsed.value) {
            return 0
        }
        return o2.lastUsed.value.compareTo(o1.lastUsed.value)
    }

    override fun toString(): String = Strings.sortBox.sort.lastUsed()
}
