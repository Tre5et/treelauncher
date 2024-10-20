package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.Strings

class ComponentManifestNameComparator : Comparator<Component> {
    override fun compare(o1: Component, o2: Component): Int {
        return o1.name.value.lowercase().compareTo(o2.name.value.lowercase())
    }

    override fun toString(): String = Strings.sortBox.sort.name()
}