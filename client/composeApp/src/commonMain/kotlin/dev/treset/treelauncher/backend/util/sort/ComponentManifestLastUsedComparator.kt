package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.Strings

class ComponentManifestLastUsedComparator<T: Component> : Comparator<T> {
    override fun compare(o1: T, o2: T): Int {
        if (o1.lastUsed.value == o2.lastUsed.value) {
            return 0
        }
        return o2.lastUsed.value.compareTo(o1.lastUsed.value)
    }

    override fun toString(): String = Strings.sortBox.sort.lastUsed()
}
