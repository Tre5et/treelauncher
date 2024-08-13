package net.treset.treelauncher.backend.data

import net.treset.mc_version_loader.json.GenericJsonParsable
import net.treset.mc_version_loader.json.SerializationException
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType

class LauncherDetails(
    var activeInstance: String?,
    var assetsDir: String,
    var gamedataDir: String,
    var gamedataType: String,
    var instanceComponentType: String,
    var instancesDir: String,
    var instancesType: String,
    var javaComponentType: String,
    var javasDir: String,
    var javasType: String,
    var librariesDir: String,
    var modsComponentType: String,
    modsDir: String,
    var modsType: String,
    var optionsComponentType: String,
    var optionsDir: String,
    var optionsType: String,
    var resourcepacksComponentType: String,
    var resourcepacksDir: String,
    var resourcepacksType: String,
    var savesComponentType: String,
    savesDir: String,
    var savesType: String,
    var versionComponentType: String,
    var versionDir: String,
    var versionType: String
) : GenericJsonParsable() {
    val modsDir: String = modsDir
        get() {
            return field ?: "mods_data"
        }

    val savesDir: String = savesDir
        get() {
            return field ?: "saves_data"
        }

    val typeConversion: Map<String, LauncherManifestType>
        get() {
            val typeConversion: MutableMap<String, LauncherManifestType> = HashMap()
            typeConversion["launcher"] = LauncherManifestType.LAUNCHER
            typeConversion[gamedataType] = LauncherManifestType.GAME
            typeConversion[instanceComponentType] = LauncherManifestType.INSTANCE_COMPONENT
            typeConversion[instancesType] = LauncherManifestType.INSTANCES
            typeConversion[javaComponentType] = LauncherManifestType.JAVA_COMPONENT
            typeConversion[javasType] = LauncherManifestType.JAVAS
            typeConversion[modsComponentType] = LauncherManifestType.MODS_COMPONENT
            typeConversion[modsType] = LauncherManifestType.MODS
            typeConversion[optionsComponentType] = LauncherManifestType.OPTIONS_COMPONENT
            typeConversion[optionsType] = LauncherManifestType.OPTIONS
            typeConversion[resourcepacksComponentType] = LauncherManifestType.RESOURCEPACKS_COMPONENT
            typeConversion[resourcepacksType] = LauncherManifestType.RESOURCEPACKS
            typeConversion[savesComponentType] = LauncherManifestType.SAVES_COMPONENT
            typeConversion[savesType] = LauncherManifestType.SAVES
            typeConversion[versionComponentType] = LauncherManifestType.VERSION_COMPONENT
            typeConversion[versionType] = LauncherManifestType.VERSIONS
            return typeConversion
        }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): LauncherDetails {
            return fromJson(json, LauncherDetails::class.java)
        }
    }
}
