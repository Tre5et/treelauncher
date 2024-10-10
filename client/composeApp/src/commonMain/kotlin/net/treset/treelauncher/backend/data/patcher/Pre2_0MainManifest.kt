package net.treset.treelauncher.backend.data.patcher

import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType

class Pre2_0MainManifest(
    type: LauncherManifestType,
    typeConversion: Map<String, LauncherManifestType>,
    details: String
) : Pre2_0Manifest(
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
        ): Pre2_0MainManifest {
            val mainManifest = fromJson(json, Pre2_0MainManifest::class.java)
            mainManifest.typeConversion = typeConversion
            return mainManifest
        }
    }
}