package dev.treset.treelauncher.components.mods

import dev.treset.mcdl.mods.ModProvider
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableStateList
import dev.treset.treelauncher.generic.VersionType

class ModDisplayContext(
    val versions: List<String>,
    val types: List<VersionType>,
    val providers: List<ModProvider>,
    val directory: LauncherFile,
    val registerJob: ((MutableStateList<LauncherMod>) -> Unit) -> Unit
)