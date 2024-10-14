package dev.treset.treelauncher.components.mods.display

import androidx.compose.ui.graphics.painter.Painter
import dev.treset.mcdl.mods.ModData
import dev.treset.mcdl.mods.ModVersionData
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.util.ModProviderStatus

data class ModDisplayData(
    val mod: LauncherMod?,
    val downloading: Boolean,
    val image: Painter?,
    val enabled: Boolean,
    val selectLatest: Boolean,
    val versions: List<ModVersionData>?,
    val currentVersion: ModVersionData?,
    val modrinthStatus: ModProviderStatus,
    val curseforgeStatus: ModProviderStatus,
    val modData: ModData?,

    val startDownload: (version: ModVersionData) -> Unit,
    val changeEnabled: () -> Unit,
    val deleteMod: () -> Unit,
    val setVisible: (Boolean) -> Unit,
)