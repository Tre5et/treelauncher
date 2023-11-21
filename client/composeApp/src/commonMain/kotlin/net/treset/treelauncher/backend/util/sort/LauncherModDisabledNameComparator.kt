package net.treset.treelauncher.backend.util.sort

import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.treelauncher.localization.strings

class LauncherModDisabledNameComparator : Comparator<LauncherMod> {
    override fun compare(o1: LauncherMod, o2: LauncherMod): Int {
        if (o1.isEnabled == o2.isEnabled) {
            return o1.name.compareTo(o2.name)
        }
        return if (o1.isEnabled) {
            -1
        } else 1
    }

    override fun toString(): String = strings().sorts.enabledName()
}
