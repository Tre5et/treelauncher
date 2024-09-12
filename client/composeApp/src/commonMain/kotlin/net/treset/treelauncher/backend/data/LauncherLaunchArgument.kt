package net.treset.treelauncher.backend.data

import net.treset.mcdl.format.FormatUtils
import net.treset.mcdl.util.OsUtil

class LauncherLaunchArgument(
    var argument: String,
    var feature: String? = null,
    var osName: String? = null,
    var osVersion: String? = null,
    var osArch: String? = null
) {
    @Transient
    var parsedArgument = argument
        get() {
            if (field == null) {
                field = argument
            }
            return field
        }

    @Transient
    var replacementValues = FormatUtils.findMatches(argument, "\\$\\{([a-zA-z_\\-\\d]*)\\}")
        get() {
            if(field == null) {
                field = FormatUtils.findMatches(argument, "\\$\\{([a-zA-z_\\-\\d]*)\\}")
            }
            return field
        }

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

    fun isActive(features: List<LauncherFeature>): Boolean {
        if (feature?.isNotBlank() == true) {
            if (features.stream().noneMatch { f: LauncherFeature -> f.feature == feature }) {
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
        return argument == (other as? LauncherLaunchArgument)?.argument
                && feature == (other as? LauncherLaunchArgument)?.feature
                && osName == (other as? LauncherLaunchArgument)?.osName
                && osVersion == (other as? LauncherLaunchArgument)?.osVersion
                && osArch == (other as? LauncherLaunchArgument)?.osArch
    }

    override fun hashCode(): Int {
        var result = argument.hashCode()
        result = 31 * result + (feature?.hashCode() ?: 0)
        result = 31 * result + (osName?.hashCode() ?: 0)
        result = 31 * result + (osVersion?.hashCode() ?: 0)
        result = 31 * result + (osArch?.hashCode() ?: 0)
        return result
    }
}
