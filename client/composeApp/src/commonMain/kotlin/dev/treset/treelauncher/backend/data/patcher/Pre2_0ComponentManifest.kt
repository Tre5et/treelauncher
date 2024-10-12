package dev.treset.treelauncher.backend.data.patcher

import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.data.manifest.*
import dev.treset.treelauncher.backend.util.file.LauncherFile

class Pre2_0ComponentManifest(
    type: LauncherManifestType,
    typeConversion: Map<String, LauncherManifestType>,
    id: String,
    name: String,
    includedFiles: List<String>,
    details: String? = null,
    expectedType: LauncherManifestType? = null
) : Pre2_0Manifest(
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
        ): Pre2_0ComponentManifest {
            val componentManifest = fromJson(json, Pre2_0ComponentManifest::class.java)
            componentManifest.typeConversion = typeConversion
            return componentManifest
        }
    }
}

fun Pre2_0ComponentManifest.toSavesComponent(): SavesComponent {
    return SavesComponent(
        id,
        name,
        LauncherFile.of(directory, appConfig().manifestFileName),
        includedFiles = includedFiles.toTypedArray(),
        lastUsed = lastUsed ?: ""
    )
}

fun Pre2_0ComponentManifest.toResourcepackComponent(): ResourcepackComponent {
    return ResourcepackComponent(
        id,
        name,
        LauncherFile.of(directory, appConfig().manifestFileName),
        includedFiles = includedFiles.toTypedArray(),
        lastUsed = lastUsed ?: ""
    )
}

fun Pre2_0ComponentManifest.toOptionsComponent(): OptionsComponent {
    return OptionsComponent(
        id,
        name,
        LauncherFile.of(directory, appConfig().manifestFileName),
        includedFiles = includedFiles.toTypedArray(),
        lastUsed = lastUsed ?: ""
    )
}

fun Pre2_0ComponentManifest.toJavaComponent(): JavaComponent {
    return JavaComponent(
        id,
        name,
        LauncherFile.of(directory, appConfig().manifestFileName),
        includedFiles = includedFiles.toTypedArray(),
        lastUsed = lastUsed ?: ""
    )
}