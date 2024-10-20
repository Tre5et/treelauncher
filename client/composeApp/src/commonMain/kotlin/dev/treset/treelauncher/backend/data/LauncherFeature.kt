package dev.treset.treelauncher.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class LauncherFeature(var feature: String, var value: String)
