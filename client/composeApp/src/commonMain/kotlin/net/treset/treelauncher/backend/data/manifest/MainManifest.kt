package net.treset.treelauncher.backend.data.manifest

import net.treset.mc_version_loader.json.SerializationException

class MainManifest(
    type: LauncherManifestType,
    typeConversion: Map<String, LauncherManifestType>,
    details: String
) : Manifest(
    type,
    typeConversion,
    null,
    details,
    null,
    null,
    null,
    null,
    LauncherManifestType.LAUNCHER
) {
    var details: String
        get() = _details ?: ""
        set(details) {
            _details = details
        }

    companion object {
        @JvmOverloads
        @Throws(SerializationException::class)
        fun fromJson(
            json: String,
            typeConversion: Map<String, LauncherManifestType> = LauncherManifestType.defaultConversion
        ): MainManifest {
            val mainManifest = fromJson(json, MainManifest::class.java)
            mainManifest.typeConversion = typeConversion
            return mainManifest
        }
    }
}