package net.treset.treelauncher.backend.data.manifest

import com.google.gson.annotations.SerializedName
import net.treset.mcdl.format.FormatUtils
import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.SerializationException
import java.time.LocalDateTime

open class  Manifest(
    type: LauncherManifestType,
    @field:Transient var typeConversion: Map<String, LauncherManifestType>,
    @field:SerializedName("id") var _id: String?,
    @field:SerializedName("details") var _details: String?,
    @field:SerializedName("prefix") var _prefix: String?,
    @field:SerializedName("name") var _name: String?,
    @field:SerializedName("includedFiles") var _includedFiles: List<String>?,
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
        ): Manifest {
            val manifest = fromJson(json, Manifest::class.java)
            manifest.typeConversion = typeConversion
            return manifest
        }
    }
}
