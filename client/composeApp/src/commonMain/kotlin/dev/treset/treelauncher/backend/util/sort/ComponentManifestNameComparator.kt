package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.Strings

class ComponentManifestNameComparator<T: Component>  : Comparator<T> {
    override fun compare(o1: T, o2: T): Int {
        return o1.name.value.lowercase().compareTo(o2.name.value.lowercase())
    }

    override fun toString(): String = Strings.sortBox.sort.name()
}