package dev.treset.treelauncher.backend.data.manifest

import dev.treset.mcdl.mods.*
import java.time.LocalDateTime

fun ModVersionFromVersion(v: String): GenericModVersion {
    return object : GenericModVersion() {
        override fun getDatePublished(): LocalDateTime {
            return LocalDateTime.MIN
        }

        override fun getDownloads(): Int {
            return -1
        }

        override fun getName(): String {
            return ""
        }

        override fun getVersionNumber(): String {
            return v
        }

        override fun getDownloadUrl(): String {
            return ""
        }

        override fun getModLoaders(): MutableList<String> {
            return mutableListOf()
        }

        override fun getGameVersions(): MutableList<String> {
            return mutableListOf()
        }

        override fun updateRequiredDependencies(): MutableList<ModVersionData> {
            return mutableListOf()
        }

        override fun getParentMod(): ModData? {
            return null
        }

        override fun setParentMod(parent: ModData?) {
        }

        override fun getModProviders(): MutableList<ModProvider> {
            return mutableListOf()
        }

        override fun getModVersionType(): ModVersionType {
            return ModVersionType.NONE
        }

    }
}