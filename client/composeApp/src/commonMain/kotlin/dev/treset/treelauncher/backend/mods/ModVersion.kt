package dev.treset.treelauncher.backend.mods

import dev.treset.mcdl.mods.*
import java.time.LocalDateTime

fun modVersionFromString(version: String): ModVersionData {
    return object : GenericModVersion() {
        override fun getDatePublished(): LocalDateTime? = null
        override fun getDownloads(): Int = 0
        override fun getName(): String? = null
        override fun getVersionNumber(): String = version
        override fun getDownloadUrl(): String? = null
        override fun getModLoaders(): MutableList<String> = mutableListOf()
        override fun getGameVersions(): MutableList<String> = mutableListOf()
        override fun updateRequiredDependencies(): MutableList<ModVersionData> = mutableListOf()
        override fun getParentMod(): ModData? = null
        override fun setParentMod(p0: ModData?) {}
        override fun getModProviders(): MutableList<ModProvider> = mutableListOf()
        override fun getModVersionType(): ModVersionType? = null
    }
}