package dev.treset.treelauncher.backend.config

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.mods.ModProvider
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.ColorData
import dev.treset.treelauncher.backend.util.serialization.DpData
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import dev.treset.treelauncher.backend.util.sort.*
import dev.treset.treelauncher.localization.Language
import dev.treset.treelauncher.localization.SystemLanguage
import dev.treset.treelauncher.style.*
import dev.treset.treelauncher.util.DetailsListDisplay
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames
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

@Serializable
data class Window(
    val x: DpData,
    val y: DpData,
    val width: DpData,
    val height: DpData,
    val isMaximized: Boolean
)


@Serializable
class Settings(@Transient var file: LauncherFile = LauncherFile.of("")) : GenericJsonParsable() {
    val language: MutableDataState<Language> = mutableStateOf(SystemLanguage)
    val theme: MutableDataState<Theme> = mutableStateOf(Theme.SYSTEM)
    val accentColor: MutableDataState<AccentColor> = mutableStateOf(AccentColor.GREEN)
    val customColor: MutableDataState<ColorData> = mutableStateOf(Color.White)
    val darkColors: MutableDataState<UserColors> = mutableStateOf(UserColors())
    val lightColors: MutableDataState<UserColors> = mutableStateOf(UserColors())
    val displayScale: MutableDataState<Int> = mutableStateOf(1000)
    val fontScale: MutableDataState<Int> = mutableStateOf(1000)
    val syncUrl: MutableDataState<String?> = mutableStateOf(null)
    val syncPort: MutableDataState<String?> = mutableStateOf(null)
    val syncKey: MutableDataState<String?> = mutableStateOf(null)
    val instanceSortType: MutableDataState<InstanceDataSortType> = mutableStateOf(InstanceDataSortType.NAME)
    val isInstanceSortReverse: MutableDataState<Boolean> = mutableStateOf(false)
    val savesComponentSortType: MutableDataState<ComponentManifestSortType> = mutableStateOf(ComponentManifestSortType.NAME)
    val isSavesComponentSortReverse: MutableDataState<Boolean> = mutableStateOf(false)
    val savesDetailsListDisplay: MutableDataState<DetailsListDisplay> = mutableStateOf(DetailsListDisplay.FULL)
    val resourcepacksDetailsListDisplay: MutableDataState<DetailsListDisplay> = mutableStateOf(DetailsListDisplay.FULL)
    val resourcepacksComponentSortType: MutableDataState<ComponentManifestSortType> = mutableStateOf(ComponentManifestSortType.NAME)
    val isResourcepacksComponentSortReverse: MutableDataState<Boolean> = mutableStateOf(false)
    val optionsComponentSortType: MutableDataState<ComponentManifestSortType> = mutableStateOf(ComponentManifestSortType.NAME)
    val isOptionsComponentSortReverse: MutableDataState<Boolean> = mutableStateOf(false)
    val modDetailsListDisplay: MutableDataState<DetailsListDisplay> = mutableStateOf(DetailsListDisplay.FULL)
    val modComponentSortType: MutableDataState<ComponentManifestSortType> = mutableStateOf(ComponentManifestSortType.NAME)
    val isModComponentSortReverse: MutableDataState<Boolean> = mutableStateOf(false)
    val modSortType: MutableDataState<LauncherModSortType> = mutableStateOf(LauncherModSortType.NAME)
    val isModSortReverse: MutableDataState<Boolean> = mutableStateOf(false)
    val isModsUpdate: MutableDataState<Boolean> = mutableStateOf(true)
    val isModsEnable: MutableDataState<Boolean> = mutableStateOf(false)
    val isModsDisable: MutableDataState<Boolean> = mutableStateOf(false)
    val modrinthStatus: MutableDataState<Pair<Int, Boolean>> = mutableStateOf(0 to true)
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("curseforge_satus")
    val curseforgeStatus: MutableDataState<Pair<Int, Boolean>> = mutableStateOf(1 to true)
    var modProviders: List<Pair<ModProvider, Boolean>>
        get() {
            val result = mutableListOf<Pair<ModProvider, Boolean>>()
            result.add(ModProvider.MODRINTH to modrinthStatus.value.second)
            result.add(curseforgeStatus.value.first, ModProvider.CURSEFORGE to curseforgeStatus.value.second)
            return result
        }
        set(value) = value.forEachIndexed { i, provider ->
            when(provider.first) {
                ModProvider.MODRINTH -> modrinthStatus.value = i to provider.second
                ModProvider.CURSEFORGE -> curseforgeStatus.value = i to provider.second
            }
        }
    val acknowledgedNews: MutableDataStateList<String> = mutableStateListOf()
    val updateUrl: MutableDataState<String> = mutableStateOf(appConfig().updateUrl ?: "https://update.treelauncher.net")
    val discordIntegration: MutableDataState<Boolean> = mutableStateOf(false)
    val discordShowModLoader: MutableDataState<Boolean> = mutableStateOf(true)
    val discordShowTime: MutableDataState<Boolean> = mutableStateOf(true)
    val discordShowWatermark: MutableDataState<Boolean> = mutableStateOf(true)
    val discordShowVersion: MutableDataState<Boolean> = mutableStateOf(true)
    val discordShowInstance: MutableDataState<Boolean> = mutableStateOf(true)
    val window: MutableDataState<Window?> = mutableStateOf(null)
    val dataVersion: MutableDataState<String> = mutableStateOf("0.0.1")
    val clientId: MutableDataState<String> = mutableStateOf(UUID.randomUUID().toString())

    val isDebug: MutableDataState<Boolean> = mutableStateOf(System.getenv("debug") == "true")

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
        @Throws(IOException::class)
        fun load(file: LauncherFile): Settings {
            LOGGER.debug { "Loading settings from: $file..." }
            val result: Settings = file.readData()
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