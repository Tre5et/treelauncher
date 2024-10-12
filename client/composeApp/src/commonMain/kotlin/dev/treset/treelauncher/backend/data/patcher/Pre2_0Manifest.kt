package dev.treset.treelauncher.backend.data.patcher

import com.google.gson.annotations.SerializedName
import dev.treset.mcdl.format.FormatUtils
import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import dev.treset.treelauncher.backend.data.manifest.LauncherManifestType
import java.time.LocalDateTime

open class Pre2_0Manifest(
    type: LauncherManifestType,
    @field:Transient var typeConversion: Map<String, LauncherManifestType>,
    @field:SerializedName("id") var _id: String?,
    @field:SerializedName("details") var _details: String?,
    @field:SerializedName("prefix") var _prefix: String?,
    @field:SerializedName("name") var _name: String?,
    @field:SerializedName("included_files") var _includedFiles: List<String>?,
    @field:SerializedName("components") var _components: MutableList<String>?,
    @field:Transient val expectedType: LauncherManifestType? = null
) : GenericJsonParsable() {
    var lastUsed: String? = null

    @Transient
    private var _directory: String? = null

    var directory: String
        get() = _directory ?: _name ?: ""
        set(directory) {
            _directory = directory
        }

    var type: LauncherManifestType
        get() = LauncherManifestType.getLauncherManifestType(_type, typeConversion)
        set(newType) {
            _type = newType.asString(typeConversion)
        }

    @SerializedName("type")
    private var _type: String = type.asString(typeConversion)

    var lastUsedTime: LocalDateTime
        get() = FormatUtils.parseLocalDateTime(lastUsed?: "0")
        set(lastPlayed) {
            lastUsed = FormatUtils.formatLocalDateTime(lastPlayed)
        }

    fun isExpectedType(): Boolean {
        return type == expectedType
    }

    companion object {
        @JvmOverloads
        @Throws(SerializationException::class)
        fun fromJson(
            json: String?,
            typeConversion: Map<String, LauncherManifestType> = LauncherManifestType.defaultConversion
        ): Pre2_0Manifest {
            val manifest = fromJson(json, Pre2_0Manifest::class.java)
            manifest.typeConversion = typeConversion
            return manifest
        }
    }
}