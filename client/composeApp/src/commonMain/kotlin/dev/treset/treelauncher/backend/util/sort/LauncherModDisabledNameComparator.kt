package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.localization.Strings

class LauncherModDisabledNameComparator : Comparator<LauncherMod> {
    override fun compare(o1: LauncherMod, o2: LauncherMod): Int {
        if (o1.enabled.value == o2.enabled.value) {
            return o1.name.value.lowercase().compareTo(o2.name.value.lowercase())
        }
        return if (o1.enabled.value) {
            -1
        } else 1
    }

    override fun toString(): String = Strings.sortBox.sort.enabledName()
}
