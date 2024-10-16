package dev.treset.treelauncher.backend.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.gson.annotations.SerializedName
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.json.SerializationException
import dev.treset.mcdl.mods.ModProvider
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.sort.*
import dev.treset.treelauncher.localization.Language
import dev.treset.treelauncher.localization.language
import dev.treset.treelauncher.style.*
import dev.treset.treelauncher.util.DetailsListDisplay
import java.io.IOException
import java.util.*

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

enum class ComponentManifestSortType(val comparator: Comparator<Component>) {
    NAME(ComponentManifestNameComparator()),
    LAST_USED(ComponentManifestLastUsedComparator());

    override fun toString(): String {
        return comparator.toString()
    }
}

data class Window(
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp,
    val isMaximized: Boolean
)


class Settings(@Transient var file: LauncherFile) : GenericJsonParsable() {
    var language: Language = language().systemLanguage
    var theme: Theme = Theme.SYSTEM
    var accentColor: AccentColor = AccentColor.GREEN
    var customColor: Color = Color.White
    var darkColors: UserColors = UserColors()
    var lightColors: UserColors = UserColors()
    var displayScale: Int = 1000
        set(value) {
            field = value
            setDisplayScale(value)
        }
    var fontScale: Int = 1000
        set(value) {
            field = value
            setFontScale(value)
        }
    var syncUrl: String? = null
    var syncPort: String? = null
    var syncKey: String? = null
    var instanceSortType: InstanceDataSortType = InstanceDataSortType.NAME
    var isInstanceSortReverse = false
    var savesComponentSortType: ComponentManifestSortType = ComponentManifestSortType.NAME
    var isSavesComponentSortReverse = false
    var savesDetailsListDisplay: DetailsListDisplay = DetailsListDisplay.FULL
    var resourcepacksDetailsListDisplay: DetailsListDisplay = DetailsListDisplay.FULL
    var resourcepacksComponentSortType: ComponentManifestSortType = ComponentManifestSortType.NAME
    var isResourcepacksComponentSortReverse = false
    var optionsComponentSortType: ComponentManifestSortType = ComponentManifestSortType.NAME
    var isOptionsComponentSortReverse = false
    var modDetailsListDisplay: DetailsListDisplay = DetailsListDisplay.FULL
    var modComponentSortType: ComponentManifestSortType = ComponentManifestSortType.NAME
    var isModComponentSortReverse = false
    var modSortType: LauncherModSortType = LauncherModSortType.NAME
    var isModSortReverse = false
    var isModsUpdate = true
    var isModsEnable = false
    var isModsDisable = false
    var modrinthStatus = 0 to true
    var curseforgeSatus = 1 to true
    var modProviders: List<Pair<ModProvider, Boolean>>
        get() {
            val result = mutableListOf<Pair<ModProvider, Boolean>>()
            result.add(ModProvider.MODRINTH to modrinthStatus.second)
            result.add(curseforgeSatus.first, ModProvider.CURSEFORGE to curseforgeSatus.second)
            return result
        }
        set(value) = value.forEachIndexed { i, provider ->
            when(provider.first) {
                ModProvider.MODRINTH -> modrinthStatus = i to provider.second
                ModProvider.CURSEFORGE -> curseforgeSatus = i to provider.second
            }
        }
    var acknowledgedNews = mutableListOf<String>()
    var updateUrl: String = appConfig().updateUrl ?: "https://update.treelauncher.net"
    var discordIntegration: Boolean = false
    var discordShowModLoader: Boolean = true
    var discordShowTime: Boolean = true
    var discordShowWatermark: Boolean = true
    var discordShowVersion: Boolean = true
    var discordShowInstance: Boolean = true
    var window: Window? = null
    var dataVersion: String = "0.0.1"
    val clientId: String = UUID.randomUUID().toString()

    @SerializedName("is_debug")
    private var _isDebug: Boolean? = if(System.getenv("debug") == "true") true else null
    var isDebug: Boolean
        get() = _isDebug ?: false
        set(value) {
            _isDebug = if(value) true else null
        }
    //constructor only for gson
    private constructor() : this(LauncherFile(""))

    fun hasSyncData(): Boolean {
        return syncUrl != null && syncPort != null && syncKey != null
    }

    @Throws(IOException::class)
    fun save() {
        LOGGER.debug { "Saving settings to: $file..." }
        file.write(this)
        LOGGER.debug { "Saved settings" }
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): Settings {
            return fromJson(json, Settings::class.java)
        }

        @Throws(IOException::class)
        fun load(file: LauncherFile): Settings {
            LOGGER.debug { "Loading settings from: $file..." }
            val result: Settings = try {
                fromJson(file.readString())
            } catch (e: SerializationException) {
                throw IOException("Failed to load settings", e)
            }
            result.file = file
            setAppSettings(result)
            LOGGER.debug { "Loaded settings" }
            return appSettings()
        }

        @Throws(IOException::class)
        fun new(file: LauncherFile): Settings {
            LOGGER.debug { "Creating new settings at: $file..." }
            val result = Settings(file)
            setAppSettings(result)
            result.save()
            LOGGER.debug { "Created new settings" }
            return appSettings()
        }
    }
}

private val LOGGER = KotlinLogging.logger {}

private lateinit var settings: Settings
fun appSettings(): Settings = settings
fun setAppSettings(newSettings: Settings) {
    settings = newSettings
}