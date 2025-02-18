package dev.treset.treelauncher.backend.config

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import dev.treset.mcdl.json.GenericJsonParsable
import dev.treset.mcdl.mods.ModProvider
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.ColorData
import dev.treset.treelauncher.backend.util.serialization.DpData
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import dev.treset.treelauncher.backend.util.sort.*
import dev.treset.treelauncher.backend.data.ModProviderData
import dev.treset.treelauncher.backend.data.ModProviderList
import dev.treset.treelauncher.localization.Language
import dev.treset.treelauncher.localization.SystemLanguage
import dev.treset.treelauncher.style.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames
import java.io.IOException
import java.util.*

@Serializable
data class Window(
    val x: DpData,
    val y: DpData,
    val width: DpData,
    val height: DpData,
    val isMaximized: Boolean
)


@Serializable
class Settings(
    @Transient var file: LauncherFile = LauncherFile.of(""),
    val dataVersion: MutableDataState<String> = mutableStateOf("0.0.1")
) : GenericJsonParsable() {
    val language: MutableDataState<Language> = mutableStateOf(SystemLanguage)
    val theme: MutableDataState<Theme> = mutableStateOf(Theme.SYSTEM)
    val accentColor: MutableDataState<AccentColor> = mutableStateOf(AccentColor.GREEN)
    val customColor: MutableDataState<ColorData> = mutableStateOf(Color.White)
    val darkColors: MutableDataState<UserColors> = mutableStateOf(UserColors())
    val lightColors: MutableDataState<UserColors> = mutableStateOf(UserColors())
    val displayScale: MutableDataState<Int> = mutableStateOf(1000)
    val fontScale: MutableDataState<Int> = mutableStateOf(1000)
    val minimizeWhileRunning: MutableDataState<Boolean> = mutableStateOf(false)
    val syncUrl: MutableDataState<String?> = mutableStateOf(null)
    val syncPort: MutableDataState<String?> = mutableStateOf(null)
    val syncKey: MutableDataState<String?> = mutableStateOf(null)
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("isModsAutoUpdate")
    val modsDefaultAutoUpdate: MutableDataState<Boolean> = mutableStateOf(false)
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("isModsEnable")
    val modsDefaultEnableOnUpdate: MutableDataState<Boolean> = mutableStateOf(false)
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("isModsDisable")
    val modsDefaultDisableOnNoVersion: MutableDataState<Boolean> = mutableStateOf(false)
    val modsDefaultProviders: ModProviderList = mutableStateListOf(
        ModProviderData(ModProvider.MODRINTH, true),
        ModProviderData(ModProvider.CURSEFORGE, true),
    )
    val acknowledgedNews: MutableDataStateList<String> = mutableStateListOf()
    val updateUrl: MutableDataState<String> = mutableStateOf(appConfig().updateUrl ?: "https://update.treelauncher.net")
    val discordIntegration: MutableDataState<Boolean> = mutableStateOf(false)
    val discordShowModLoader: MutableDataState<Boolean> = mutableStateOf(true)
    val discordShowTime: MutableDataState<Boolean> = mutableStateOf(true)
    val discordShowWatermark: MutableDataState<Boolean> = mutableStateOf(true)
    val discordShowVersion: MutableDataState<Boolean> = mutableStateOf(true)
    val discordShowInstance: MutableDataState<Boolean> = mutableStateOf(true)
    val window: MutableDataState<Window?> = mutableStateOf(null)
    val clientId: MutableDataState<String> = mutableStateOf(UUID.randomUUID().toString())

    val isDebug: MutableDataState<Boolean> = mutableStateOf(System.getenv("debug") == "true")

    fun hasSyncData(): Boolean {
        return syncUrl.value != null && syncPort.value != null && syncKey.value != null
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
            LOGGER.debug { "Creating new settings with data version: ${appConfig().dataVersion} at: $file..." }
            val result = Settings(file, mutableStateOf(appConfig().dataVersion.toString()))
            AppSettings = result
            result.save()
            LOGGER.debug { "Created new settings" }
            return result
        }
    }
}

private val LOGGER = KotlinLogging.logger {}

lateinit var AppSettings: Settings