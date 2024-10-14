package net.treset.treelauncher.backend.util.sort

import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.localization.strings

class ComponentManifestNameComparator : Comparator<ComponentManifest> {
    override fun compare(o1: ComponentManifest, o2: ComponentManifest): Int {
        return o1.name.lowercase().compareTo(o2.name.lowercase())
    }

    override fun toString(): String = strings().sortBox.sort.name()
}