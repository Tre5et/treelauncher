package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.Strings

class ComponentManifestLastUsedComparator : Comparator<Component> {
    override fun compare(o1: Component, o2: Component): Int {
        if (o1.lastUsed == o2.lastUsed) {
            return 0
        }
        o1.lastUsed.let {  o1Used ->
            o2.lastUsed.let { o2Used ->
                return o2Used.compareTo(o1Used)
            }
        }
    }

    override fun toString(): String = Strings.sortBox.sort.lastUsed()
}
