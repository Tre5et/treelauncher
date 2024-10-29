package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.localization.Strings

object ComponentLastUsedComparator : SortProvider<Component>() {
    override fun compare(o1: Component, o2: Component): Int {
        if (o1.lastUsed.value == o2.lastUsed.value) {
            return 0
        }
        return o2.lastUsed.value.compareTo(o1.lastUsed.value)
    }

    override val name: String
        get() = Strings.sortBox.sort.lastUsed()

    override val id = "LAST_USED"
}
