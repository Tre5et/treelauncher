package dev.treset.treelauncher.backend.data.patcher

import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.LauncherModDownload

class Pre2_0LauncherMod(
    val currentProvider: String? = null,
    val description: String? = null,
    val isEnabled: Boolean = true,
    val url: String? = null,
    val iconUrl: String? = null,
    val name: String = "",
    val fileName: String = "",
    val version: String = "",
    val downloads: List<LauncherModDownload> = listOf()
) {
    fun toLauncherMod(): LauncherMod {
        return LauncherMod(
            currentProvider,
            description,
            isEnabled,
            url,
            iconUrl,
            name,
            fileName,
            version,
            downloads
        )
    }
}