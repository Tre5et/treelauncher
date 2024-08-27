package net.treset.treelauncher.backend.data.manifest

enum class LauncherManifestType {
    LAUNCHER,
    VERSIONS,
    VERSION_COMPONENT,
    RESOURCEPACKS,
    RESOURCEPACKS_COMPONENT,
    OPTIONS,
    OPTIONS_COMPONENT,
    JAVAS,
    JAVA_COMPONENT,
    INSTANCES,
    INSTANCE_COMPONENT,
    GAME,
    MODS,
    MODS_COMPONENT,
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



