package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.strings

class ComponentManifestNameComparator : Comparator<Component> {
    override fun compare(o1: Component, o2: Component): Int {
        return o1.name.lowercase().compareTo(o2.name.lowercase())
    }

    override fun toString(): String = strings().sortBox.sort.name()
}