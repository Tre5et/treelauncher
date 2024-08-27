package net.treset.treelauncher.backend.data.manifest

import net.treset.mc_version_loader.json.SerializationException

class ComponentManifest(
    type: LauncherManifestType,
    typeConversion: Map<String, LauncherManifestType>,
    id: String,
    name: String,
    includedFiles: List<String>,
    details: String? = null,
    expectedType: LauncherManifestType? = null
) : Manifest(
    type,
    typeConversion,
    id,
    details,
    null,
    name,
    includedFiles,
    null,
    expectedType
) {
    var id: String
        get() = _id ?: ""
        set(id) {
            _id = id
        }

    var name: String
        get() = _name ?: ""
        set(name) {
            _name = name
        }

    var details: String
        get() = _details ?: ""
        set(details) {
            _details = details
        }

    var includedFiles: List<String>
        get() = _includedFiles ?: emptyList()
        set(includedFiles) {
            _includedFiles = includedFiles.toMutableList()
        }

    companion object {
        @JvmOverloads
        @Throws(SerializationException::class)
        fun fromJson(
            json: String?,
            typeConversion: Map<String, LauncherManifestType> = LauncherManifestType.defaultConversion
        ): ComponentManifest {
            val componentManifest = fromJson(json, ComponentManifest::class.java)
            componentManifest.typeConversion = typeConversion
            return componentManifest
        }
    }
}