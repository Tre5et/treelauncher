package net.treset.treelauncher.backend.util.sort

import net.treset.treelauncher.backend.data.LauncherMod
import net.treset.treelauncher.localization.strings

class LauncherModNameComparator : Comparator<LauncherMod> {
    override fun compare(o1: LauncherMod, o2: LauncherMod): Int {
        return o1.name.lowercase().compareTo(o2.name.lowercase())
    }

    override fun toString(): String = strings().sortBox.sort.name()
}
