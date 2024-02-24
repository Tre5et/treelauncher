package net.treset.treelauncher.backend.util.sort

import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.localization.strings

class LauncherManifestNameComparator : Comparator<LauncherManifest> {
    override fun compare(o1: LauncherManifest, o2: LauncherManifest): Int {
        return o1.name.lowercase().compareTo(o2.name.lowercase())
    }

    override fun toString(): String = strings().sortBox.sort.name()
}