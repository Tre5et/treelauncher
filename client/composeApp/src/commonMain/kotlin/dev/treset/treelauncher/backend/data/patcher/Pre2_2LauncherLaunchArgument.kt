package dev.treset.treelauncher.backend.data.patcher

import dev.treset.mcdl.format.FormatUtils
import dev.treset.mcdl.util.OsUtil
import dev.treset.treelauncher.backend.data.LauncherFeature
import dev.treset.treelauncher.backend.data.LauncherLaunchArgument
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Pre2_2LauncherLaunchArgument(
    var argument: String,
    var feature: String? = null,
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

    fun isActive(features: List<LauncherFeature>): Boolean {
        if (feature?.isNotBlank() == true) {
            if (features.none { f: LauncherFeature -> f.feature == feature }) {
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
        return argument == (other as? Pre2_2LauncherLaunchArgument)?.argument
                && feature == (other as? Pre2_2LauncherLaunchArgument)?.feature
                && osName == (other as? Pre2_2LauncherLaunchArgument)?.osName
                && osVersion == (other as? Pre2_2LauncherLaunchArgument)?.osVersion
                && osArch == (other as? Pre2_2LauncherLaunchArgument)?.osArch
    }

    override fun hashCode(): Int {
        var result = argument.hashCode()
        result = 31 * result + (feature?.hashCode() ?: 0)
        result = 31 * result + (osName?.hashCode() ?: 0)
        result = 31 * result + (osVersion?.hashCode() ?: 0)
        result = 31 * result + (osArch?.hashCode() ?: 0)
        return result
    }

    fun toLauncherLaunchArgument(): LauncherLaunchArgument {
        return LauncherLaunchArgument(
            argument = argument,
            features = if(feature == null) mapOf() else if(feature!! == "resolution_x" || feature!! == "resolution_y") mapOf(Pair("has_custom_resolution", true)) else mapOf(Pair(feature!!, true)),
            osName = osName,
            osVersion = osVersion,
            osArch = osArch
        )
    }
}
