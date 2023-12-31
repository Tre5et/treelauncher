package net.treset.treelauncher.backend.config

import net.treset.mc_version_loader.json.GenericJsonParsable
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.backend.util.sort.*
import net.treset.treelauncher.localization.Language
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.style.Theme
import java.io.IOException

enum class InstanceDataSortType(val comparator: Comparator<InstanceData>) {
    NAME(InstanceDetailsNameComparator()),
    TIME(InstanceDetailsTimeComparator()),
    LAST_PLAYED(InstanceDetailsLastPlayedComparator());

    override fun toString(): String {
        return comparator.toString()
    }
}

enum class LauncherModSortType(val comparator: Comparator<LauncherMod>) {
    NAME(LauncherModNameComparator()),
    DISABLED_NAME(LauncherModDisabledNameComparator());

    override fun toString(): String {
        return comparator.toString()
    }
}


class Settings(@Transient var file: LauncherFile) : GenericJsonParsable() {

    var language: Language = language().systemLanguage
    var theme: Theme = Theme.SYSTEM
    var syncUrl: String? = null
    var syncPort: String? = null
    var syncKey: String? = null
    var instanceSortType: InstanceDataSortType = InstanceDataSortType.NAME
    var isInstanceSortReverse = false
    var modSortType: LauncherModSortType = LauncherModSortType.NAME
    var isModSortReverse = false
    var isModsUpdate = true
    var isModsEnable = false
    var isModsDisable = false
    var acknowledgedNews = listOf<String>()

    private constructor() : this(LauncherFile("")) //constructor only for gson

    fun hasSyncData(): Boolean {
        return syncUrl != null && syncPort != null && syncKey != null
    }

    @Throws(IOException::class)
    fun save() {
        file.write(this)
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): Settings {
            return fromJson(json, Settings::class.java)
        }

        @Throws(IOException::class)
        fun load(file: LauncherFile): Settings {
            val result: Settings = try {
                fromJson(file.readString())
            } catch (e: SerializationException) {
                throw IOException("Failed to load settings", e)
            }
            result.file = file
            setAppSettings(result)
            return appSettings()
        }

        @Throws(IOException::class)
        fun new(file: LauncherFile): Settings {
            val result = Settings(file)
            setAppSettings(result)
            result.save()
            return appSettings()
        }
    }
}

private lateinit var settings: Settings
fun appSettings(): Settings = settings
fun setAppSettings(_settings: Settings) {
    settings = _settings
}
