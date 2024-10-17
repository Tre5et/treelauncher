package dev.treset.treelauncher.backend.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    var language = mutableStateOf(language().systemLanguage)
    var theme = mutableStateOf(Theme.SYSTEM)
    var accentColor = mutableStateOf(AccentColor.GREEN)
    var customColor = mutableStateOf(Color.White)
    var darkColors = mutableStateOf(UserColors())
    var lightColors = mutableStateOf(UserColors())
    var displayScale = mutableStateOf(1000)
    var fontScale = mutableStateOf(1000)
    var syncUrl = mutableStateOf<String?>(null)
    var syncPort = mutableStateOf<String?>(null)
    var syncKey = mutableStateOf<String?>(null)
    var instanceSortType = mutableStateOf(InstanceDataSortType.NAME)
    var isInstanceSortReverse = mutableStateOf(false)
    var savesComponentSortType = mutableStateOf(ComponentManifestSortType.NAME)
    var isSavesComponentSortReverse = mutableStateOf(false)
    var savesDetailsListDisplay = mutableStateOf(DetailsListDisplay.FULL)
    var resourcepacksDetailsListDisplay = mutableStateOf(DetailsListDisplay.FULL)
    var resourcepacksComponentSortType = mutableStateOf(ComponentManifestSortType.NAME)
    var isResourcepacksComponentSortReverse = mutableStateOf(false)
    var optionsComponentSortType = mutableStateOf(ComponentManifestSortType.NAME)
    var isOptionsComponentSortReverse = mutableStateOf(false)
    var modDetailsListDisplay = mutableStateOf(DetailsListDisplay.FULL)
    var modComponentSortType = mutableStateOf(ComponentManifestSortType.NAME)
    var isModComponentSortReverse = mutableStateOf(false)
    var modSortType = mutableStateOf(LauncherModSortType.NAME)
    var isModSortReverse = mutableStateOf(false)
    var isModsUpdate = mutableStateOf(true)
    var isModsEnable = mutableStateOf(false)
    var isModsDisable = mutableStateOf(false)
    var modrinthStatus = mutableStateOf(0 to true)
    var curseforgeSatus = mutableStateOf(1 to true)
    var modProviders: List<Pair<ModProvider, Boolean>>
        get() {
            val result = mutableListOf<Pair<ModProvider, Boolean>>()
            result.add(ModProvider.MODRINTH to modrinthStatus.value.second)
            result.add(curseforgeSatus.value.first, ModProvider.CURSEFORGE to curseforgeSatus.value.second)
            return result
        }
        set(value) = value.forEachIndexed { i, provider ->
            when(provider.first) {
                ModProvider.MODRINTH -> modrinthStatus.value = i to provider.second
                ModProvider.CURSEFORGE -> curseforgeSatus.value = i to provider.second
            }
        }
    var acknowledgedNews = mutableStateListOf<String>()
    var updateUrl = mutableStateOf(appConfig().updateUrl ?: "https://update.treelauncher.net")
    var discordIntegration = mutableStateOf(false)
    var discordShowModLoader = mutableStateOf(true)
    var discordShowTime = mutableStateOf(true)
    var discordShowWatermark = mutableStateOf(true)
    var discordShowVersion = mutableStateOf(true)
    var discordShowInstance = mutableStateOf(true)
    var window = mutableStateOf<Window?>(null)
    var dataVersion = mutableStateOf("0.0.1")
    val clientId = mutableStateOf(UUID.randomUUID().toString())

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
            AppSettings = result
            LOGGER.debug { "Loaded settings" }
            return result
        }

        @Throws(IOException::class)
        fun new(file: LauncherFile): Settings {
            LOGGER.debug { "Creating new settings at: $file..." }
            val result = Settings(file)
            AppSettings = result
            result.save()
            LOGGER.debug { "Created new settings" }
            return result
        }
    }
}

private val LOGGER = KotlinLogging.logger {}

lateinit var AppSettings: Settings