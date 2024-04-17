package net.treset.treelauncher.components.mods

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.mods.ModData
import net.treset.mc_version_loader.mods.ModVersionData
import net.treset.treelauncher.backend.util.ModProviderStatus

data class ModDisplayData(
    val mod: LauncherMod,
    val downloading: Boolean,
    val image: Painter?,
    val enabled: Boolean,
    val selectLatest: Boolean,
    val versions: List<ModVersionData>?,
    val currentVersion: ModVersionData,
    val modrinthStatus: ModProviderStatus,
    val curseforgeStatus: ModProviderStatus,
    val modData: ModData?,

    val startDownload: (version: ModVersionData) -> Unit,
    val changeEnabled: () -> Unit,
    val deleteMod: () -> Unit
)

@Composable
fun ModDataProvider(
    element: ModDisplay,
    content: @Composable ModDisplayData.() -> Unit
) {
    var displayData by remember { mutableStateOf(element.recomposeData()) }
    LaunchedEffect(element) {
        element.onRecomposeData = { displayData = it }
    }

    displayData.content()
}