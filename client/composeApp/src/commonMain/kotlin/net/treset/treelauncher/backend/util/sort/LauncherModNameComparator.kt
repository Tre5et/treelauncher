package net.treset.treelauncher.backend.util.sort

import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.treelauncher.localization.strings

class LauncherModNameComparator : Comparator<LauncherMod> {
    override fun compare(o1: LauncherMod, o2: LauncherMod): Int {
        return o1.name.compareTo(o2.name)
    }

    override fun toString(): String = strings().sortBox.sort.name()
}
