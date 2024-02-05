package net.treset.treelauncher.backend.util.sort

import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.treelauncher.localization.strings

class LauncherManifestLastPlayedComparator : Comparator<LauncherManifest> {
    override fun compare(o1: LauncherManifest, o2: LauncherManifest): Int {
        if (o1.lastUsed == o2.lastUsed) {
            return 0
        }
        if (o1.lastUsed == null) {
            return 1
        }
        return if (o2.lastUsed == null) {
            -1
        } else o2.lastUsed.compareTo(o1.lastUsed)
    }

    override fun toString(): String = strings().sortBox.sort.lastPlayed()
}
