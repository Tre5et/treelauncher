package dev.treset.treelauncher.backend.data

import dev.treset.mcdl.format.FormatUtils
import dev.treset.mcdl.util.OsUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class LauncherLaunchArgument(
    var argument: String,
    var features: Map<String, Boolean>? = null,
    var osName: String? = null,
    var osVersion: String? = null,
    var osArch: String? = null
) {
    @Transient
    var parsedArgument = argument

    @Transient
    var replacementValues: MutableList<String> = FormatUtils.findMatches(argument, "\\$\\{([a-zA-z_\\-\\d]*)\\}").toMutableList()

    fun replace(replacements: Map<String, String>): Boolean {
        var allReplaced = true
        val toRemove = ArrayList<String>()
        for (r in replacementValues) {
            if (replacements[r] != null) {
                parsedArgument = parsedArgument.replace("\${$r}", replacements[r]!!)
                toRemove.add(r)
            } else {
                allReplaced = false
            }
        }
        replacementValues.removeAll(toRemove)

        return allReplaced
    }

    fun isActive(activeFeatures: List<String>): Boolean {
        features?.let { feat ->
            if(feat.any { e -> activeFeatures.contains(e.key) != e.value }) {
                return false
            }
        }
        if (osName?.isNotBlank() == true) {
            if (!OsUtil.isOsName(osName)) {
                return false
            }
        }
        if (osArch?.isNotBlank() == true) {
            if (!OsUtil.isOsArch(osArch)) {
                return false
            }
        }
        if (osVersion?.isNotBlank() == true) {
            return OsUtil.isOsVersion(osVersion)
        }
        return true
    }

    val isFinished: Boolean
        get() = replacementValues.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LauncherLaunchArgument

        if (argument != other.argument) return false
        if (features != other.features) return false
        if (osName != other.osName) return false
        if (osVersion != other.osVersion) return false
        if (osArch != other.osArch) return false

        return true
    }

    override fun toString(): String {
        return "Argument(argument='$argument', features=$features, osName=$osName, osVersion=$osVersion, osArch=$osArch)"
    }

    override fun hashCode(): Int {
        var result = argument.hashCode()
        result = 31 * result + (features?.hashCode() ?: 0)
        result = 31 * result + (osName?.hashCode() ?: 0)
        result = 31 * result + (osVersion?.hashCode() ?: 0)
        result = 31 * result + (osArch?.hashCode() ?: 0)
        return result
    }
}
