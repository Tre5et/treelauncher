package dev.treset.treelauncher.backend.util.sort

import dev.treset.treelauncher.backend.data.manifest.LauncherMod
import dev.treset.treelauncher.localization.Strings

object LauncherModNameComparator : SortProvider<LauncherMod>() {
    override fun compare(o1: LauncherMod, o2: LauncherMod): Int {
        return o1.name.value.lowercase().compareTo(o2.name.value.lowercase())
    }

    override val name: String
        get() = Strings.sortBox.sort.name()

    override val id = "MOD_NAME"
}
