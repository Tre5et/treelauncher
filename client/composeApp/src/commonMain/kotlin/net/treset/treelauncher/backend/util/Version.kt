package net.treset.treelauncher.backend.util

import dev.treset.mcdl.json.SerializationException

class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
) {
    operator fun compareTo(other: Version): Int {
        if(major != other.major) {
            return major - other.major
        }
        if(minor != other.minor) {
            return minor - other.minor
        }
        return patch - other.patch
    }

    override fun equals(other: Any?): Boolean {
        if(other is Version) {
            return major == other.major && minor == other.minor && patch == other.patch
        }
        return false
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        return result
    }

    override fun toString(): String {
        return "${major}.${minor}.${patch}"
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromString(version: String): Version {
            val parts = version.split(".")
            if(parts.size != 3) {
                throw SerializationException("Invalid version string: $version")
            }
            try {
                return Version(
                    parts[0].toInt(),
                    parts[1].toInt(),
                    parts[2].toInt(),
                )
            } catch (e: NumberFormatException) {
                throw SerializationException("Invalid version string: $version", e)
            }
        }
    }
}