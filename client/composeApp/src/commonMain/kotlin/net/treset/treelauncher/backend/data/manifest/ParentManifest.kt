package net.treset.treelauncher.backend.data.manifest

import net.treset.mcdl.json.SerializationException

class ParentManifest(
    type: LauncherManifestType,
    typeConversion: Map<String, LauncherManifestType>,
    details: String,
    prefix: String,
    components: MutableList<String>,
    expectedType: LauncherManifestType? = null
) : Manifest(
    type,
    typeConversion,
    null,
    details,
    prefix,
    null,
    null,
    components,
    expectedType
) {
    var details: String
        get() = _details ?: ""
        set(details) {
            _details = details
        }

    var prefix: String
        get() = _prefix ?: ""
        set(prefix) {
            _prefix = prefix
        }

    var components: MutableList<String>
        get() = _components ?: mutableListOf()
        set(components) {
            _components = components
        }

    companion object {
        @JvmOverloads
        @Throws(SerializationException::class)
        fun fromJson(
            json: String?,
            typeConversion: Map<String, LauncherManifestType> = LauncherManifestType.defaultConversion
        ): ParentManifest {
            val parentManifest = fromJson(json, ParentManifest::class.java)
            parentManifest.typeConversion = typeConversion
            return parentManifest
        }
    }
}