package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.Strings

object ComponentNameComparator : SortProvider<Component>() {
    override fun compare(o1: Component, o2: Component): Int {
        return o1.name.value.lowercase().compareTo(o2.name.value.lowercase())
    }

    override val name: String
        get() = Strings.sortBox.sort.name()

    override val id = "NAME"
}