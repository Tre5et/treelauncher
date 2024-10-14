package dev.treset.treelauncher.backend.data.manifest

import com.google.gson.annotations.SerializedName

enum class LauncherManifestType {
    LAUNCHER,
    @SerializedName("VERSIONS", alternate = ["versions"])
    VERSIONS,
    VERSION_COMPONENT,
    @SerializedName("RESOURCEPACKS", alternate = ["resourcepacks"])
    RESOURCEPACKS,
    RESOURCEPACKS_COMPONENT,
    @SerializedName("OPTIONS", alternate = ["options"])
    OPTIONS,
    OPTIONS_COMPONENT,
    @SerializedName("JAVAS", alternate = ["javas"])
    JAVAS,
    JAVA_COMPONENT,
    @SerializedName("INSTANCES", alternate = ["instances"])
    INSTANCES,
    INSTANCE_COMPONENT,
    GAME,
    @SerializedName("MODS", alternate = ["mods"])
    MODS,
    MODS_COMPONENT,
    @SerializedName("SAVES", alternate = ["saves"])
    SAVES,
    SAVES_COMPONENT,
    UNKNOWN;

    fun asString(typeConversion: Map<String, LauncherManifestType>): String {
        for ((key, value) in typeConversion) {
            if (value == this) {
                return key
            }
        }
        return this.name.lowercase()
    }

    companion object {
        fun getLauncherManifestType(
            type: String,
            conversion: Map<String, LauncherManifestType>
        ): LauncherManifestType {
            return conversion[type]?: UNKNOWN
        }

        fun getLauncherManifestType(type: String): LauncherManifestType {
            return getLauncherManifestType(type, defaultConversion)
        }

        val defaultConversion: Map<String, LauncherManifestType>
            get() = mapOf(
                "launcher" to LAUNCHER
            )
    }
}



