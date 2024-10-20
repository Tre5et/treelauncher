package dev.treset.treelauncher.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class LauncherModDownload(var provider: String, var id: String)
