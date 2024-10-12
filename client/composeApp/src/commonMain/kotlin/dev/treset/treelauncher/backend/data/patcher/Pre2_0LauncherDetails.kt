package dev.treset.treelauncher.backend.data.patcher

import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.manifest.LauncherManifestType
import dev.treset.treelauncher.backend.data.manifest.MainManifest
import dev.treset.treelauncher.backend.util.file.LauncherFile

class Pre2_0LauncherDetails(
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
        fun fromJson(json: String?): Pre2_0LauncherDetails {
            return fromJson(json, Pre2_0LauncherDetails::class.java)
        }
    }
}

fun Pre2_0LauncherDetails.toMainManifest(): MainManifest {
    return MainManifest(
        activeInstance = activeInstance,
        assetsDir = assetsDir,
        librariesDir = librariesDir,
        gameDataDir = gamedataDir,
        instancesDir = instancesDir,
        savesDir = savesDir,
        resourcepacksDir = resourcepacksDir,
        optionsDir = optionsDir,
        modsDir = modsDir,
        versionDir = versionDir,
        javasDir = javasDir,
        file = LauncherFile.ofData(appConfig().manifestFileName)
    )
}