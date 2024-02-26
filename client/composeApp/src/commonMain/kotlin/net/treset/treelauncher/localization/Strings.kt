package net.treset.treelauncher.localization

import com.multiplatform.webview.web.WebViewState
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherMod
import net.treset.mc_version_loader.saves.Save
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.instances.InstanceDetails
import java.io.IOException
import java.util.*

enum class Language(val locale: Locale, val strings: Strings, val displayName: () -> String) {
    ENGLISH(Locale.ENGLISH, EnStrings(), { strings().language.english(language().systemLanguage == ENGLISH) }),
    GERMAN(Locale.GERMAN, DeStrings(), { strings().language.german(language().systemLanguage == GERMAN) });

    override fun toString(): String {
        return this.displayName()
    }
}
class LanguageInfo {
    val systemLanguage: Language = when (Locale.getDefault(Locale.Category.DISPLAY).language) {
        "de" -> Language.GERMAN
        else -> Language.ENGLISH
    }

    var appLanguage: Language = systemLanguage
        set(value) {
            field = value
            appSettings().language = value
            strings = when(value) {
                Language.ENGLISH -> EnStrings()
                Language.GERMAN -> DeStrings()
            }
        }
}

private val languageInfo = LanguageInfo()
fun language() = languageInfo

private var strings: Strings = when(language().appLanguage) {
    Language.ENGLISH -> EnStrings()
    Language.GERMAN -> DeStrings()
}
fun strings() = strings

open class Strings(
    val components: Components,
    val comboBox: ComboBox,
    val creator: Creator,
    val changer: Changer,
    val error: Error,
    val fixFiles: FixFiles,
    val game: Game,
    val language: Language,
    val launcher: Launcher,
    val login: Login,
    val manager: Manager,
    val nav: Nav,
    val news: News,
    val selector: Selector,
    val settings: Settings,
    val sortBox: SortBox,
    val sync: Sync,
    val textBox: TextBox,
    val theme: Theme,
    val units: Units,
    val updater: Updater,
    val version: Version
) {
    data class Components(
        val create: () -> String,
        val details: Details
    ) {
        data class Details(
            val title: () -> String
        )
    }

    data class ComboBox(
        val loading: () -> String,
        val search: () -> String
    )

    data class Creator(
        val buttonCreate: () -> String,
        val component: () -> String,
        val errorName: () -> String,
        val errorSelect: () -> String,
        val instance: Instance,
        val mods: Mods,
        val name: () -> String,
        val radioCreate: () -> String,
        val radioUse: () -> String,
        val radioInherit: () -> String,
        val status: Status,
        val version: Version
    ) {
        data class Instance(
            val instance: () -> String,
            val mods: () -> String,
            val popup: Popup,
            val resourcepacks: () -> String,
            val saves: () -> String,
            val title: () -> String,
            val options: () -> String,
            val version: () -> String
        ) {
            data class Popup(
                val back: () -> String,
                val backToInstances: () -> String,
                val creating: () -> String,
                val failure: () -> String,
                val success: () -> String,
                val undefined: () -> String
            )
        }

        data class Status(
            val starting: () -> String,
            val mods: () -> String,
            val options: () -> String,
            val resourcepacks: () -> String,
            val saves: () -> String,
            val version: Version,
            val java: () -> String,
            val finishing: () -> String
        ) {
            data class Version(
                val assets: () -> String,
                val fabric: () -> String,
                val fabricFile: () -> String,
                val fabricLibraries: () -> String,
                val file: () -> String,
                val forge: () -> String,
                val forgeFile: () -> String,
                val forgeLibraries: () -> String,
                val libraries: () -> String,
                val value: () -> String,
                val vanilla: () -> String
            )
        }

        data class Version(
            val errorVersion: () -> String,
            val errorLoader: () -> String,
            val errorType: () -> String,
            val fabric: () -> String,
            val forge: () -> String,
            val showSnapshots: () -> String,
            val loading: () -> String,
            val type: () -> String,
            val version: () -> String,
        )

        data class Mods(
            val version: () -> String,
            val type: () -> String,
        )
    }

    data class Changer(
        val apply: () -> String
    )

    data class Error(
        val close: () -> String,
        val message: (error: Exception) -> String,
        val title: () -> String,
        val severeClose: () -> String,
        val severeMessage: (error: Exception) -> String,
        val severeTitle: () -> String,
        val unknown: () -> String,
    )

    data class FixFiles(
        val cancel: () -> String,
        val close: () -> String,
        val confirm: () -> String,
        val failureMessage: () -> String,
        val failureTitle: () -> String,
        val message: () -> String,
        val runningMessage: () -> String,
        val runningTitle: () -> String,
        val successMessage: () -> String,
        val successTitle: () -> String,
        val title: () -> String
    )

    data class Game(
        val versionName: (instance: InstanceData) -> String = { instance -> "${strings().launcher.slug()}:${strings().launcher.version()}:${instance.instance.first.id.substring(0,3)}...${instance.instance.first.id.substring(instance.instance.first.id.length - 2)}"},
        val versionType: (instance: InstanceData) -> String = { instance -> instance.instance.first.name }
    )

    data class Language(
        val systemLanguage: () -> String,
        val english: (default: Boolean) -> String = { default ->  "English${if (default) " (${strings().language.systemLanguage()})" else ""}" },
        val german: (default: Boolean) -> String = { default ->  "Deutsch${if (default) " (${strings().language.systemLanguage()})" else ""}" }
    )

    data class Launcher(
        val name: () -> String = { "TreeLauncher" },
        val slug: () -> String = { "treelauncher" },
        val status: Status,
        val version: () -> String = { "1.0.0" }
    ) {
        data class Status(
            val preparing: (Int) -> String,
            val restartRequired: () -> String
        )
    }

    data class Login(
        val browserTitle: (WebViewState) -> String,
        val button: () -> String,
        val label: Label,
        val logout: () -> String,
        val keepLoggedIn: () -> String
    ) {
        data class Label(
            val authenticating: () -> String,
            val failure: () -> String,
            val success: (name: String?) -> String
        )
    }

    data class Manager(
        val component: Component,
        val instance: Instance,
        val mods: Mods,
        val resourcepacks: Resourcepacks,
        val saves: Saves
    ) {
        data class Component(
            val addFile: () -> String,
            val back: () -> String,
            val deleteFile: () -> String,
            val file: () -> String,
            val fileName: () -> String,
            val folder: () -> String,
            val import: Import,
            val includedFiles: () -> String,
            val settings: () -> String,
        ) {
            data class Import(
                val back: () -> String,
                val tooltipExpand: (expanded: Boolean) -> String
            )

            data class ImportStrings(
                val delete: () -> String,
                val import: () -> String,
                val importComponent: () -> String,
                val importFile: () -> String,
                val importing: () -> String,
                val selectedFiles: () -> String,
                val tooltipAdd: () -> String,
                val tooltipFile: () -> String,
                val back: () -> String = { strings().manager.component.import.back() },
                val tooltipExpand: (expanded: Boolean) -> String = { strings().manager.component.import.tooltipExpand(it) },
            )
        }

        data class Instance(
            val change: Change,
            val details: Details,
            val settings: Settings
        ) {
            data class Change(
                val back: () -> String,
                val cancel: () -> String,
                val changing: () -> String,
                val confirm: () -> String,
                val failure: () -> String,
                val message: () -> String,
                val noComponent: () -> String,
                val success: () -> String,
                val title: () -> String,
                val activeTitle: (InstanceDetails, String?) -> String
            )

            data class Details(
                val version: () -> String,
                val saves: () -> String,
                val resourcepacks: () -> String,
                val options: () -> String,
                val mods: () -> String,
                val settings: () -> String
            )

            data class Settings(
                val addArgument: () -> String,
                val argumentPlaceholder: () -> String,
                val arguments: () -> String,
                val deleteArgument: () -> String,
                val memory: () -> String,
                val resolution: () -> String,
                val title: () -> String,
            )
        }

        data class Mods(
            val add: () -> String,
            val card: Card,
            val change: Change,
            val changeVersion: () -> String,
            val current: () -> String,
            val edit: Edit,
            val import: ImportStrings,
            val search: Search,
            val update: Update,
            val version: () -> String,
        ) {
            data class Card(
                val changeUsed: (Boolean) -> String,
                val delete: () -> String,
                val download: () -> String,
                val edit: () -> String,
                val openBrowser: () -> String,
                val versionPlaceholder: () -> String
            )

            data class Change(
                val title: () -> String,
                val message: () -> String,
                val confirm: () -> String,
                val cancel: () -> String
            )

            data class Edit(
                val cancel: () -> String,
                val confirm: (LauncherMod?) -> String,
                val curseforge: () -> String,
                val curseforgeError: () -> String,
                val file: () -> String,
                val fileError: () -> String,
                val modrinth: () -> String,
                val modrinthError: () -> String,
                val name: () -> String,
                val version: () -> String,
                val versionError: () -> String
            )

            data class ImportStrings(
                val delete: () -> String,
                val displayName: (LauncherMod) -> String,
                val import: () -> String,
                val importComponent: () -> String,
                val importFile: () -> String,
                val importing: () -> String,
                val selectedFiles: () -> String,
                val tooltipAdd: () -> String,
                val tooltipFile: () -> String,
                val back: () -> String = { strings().manager.component.import.back() },
                val tooltipExpand: (expanded: Boolean) -> String = { strings().manager.component.import.tooltipExpand(it) },
            )

            data class Search(
                val addLocal: () -> String,
                val back: () -> String,
                val search: () -> String,
                val searchTooltip: () -> String,
                val loading: () -> String,
                val noResults: () -> String,
            )

            data class Update(
                val auto: () -> String,
                val disable: () -> String,
                val enable: () -> String,
                val tooltip: () -> String
            )
        }

        data class Resourcepacks(
            val delete: () -> String,
            val deleteTitle: () -> String,
            val deleteMessage: () -> String,
            val deleteConfirm: () -> String,
            val deleteCancel: () -> String,
            val import: Component.ImportStrings,
            val tooltipAdd: () -> String
        )

        data class Saves(
            val delete: () -> String,
            val deleteTitle: (world: Save) -> String,
            val deleteMessage: (world: Save) -> String,
            val deleteConfirm: (world: Save) -> String,
            val deleteCancel: () -> String,
            val import: Component.ImportStrings,
            val tooltipAdd: () -> String
        )
    }

    data class Nav(
        val add: () -> String,
        val home: () -> String,
        val mods: () -> String,
        val options: () -> String,
        val resourcepacks: () -> String,
        val saves: () -> String,
        val settings: () -> String,
    )

    data class News(
        val close: () -> String,
        val important: () -> String,
        val loading: () -> String,
        val none: () -> String,
        val other: () -> String,
        val tooltip: () -> String,
        val title: () -> String,
    )

    data class Selector(
        val component: Component,
        val instance: Instance,
        val mods: Mods,
        val options: Options,
        val resourcepacks: Resourcepacks,
        val saves: Saves
    ) {
        data class Component(
            val delete: Delete,
            val rename: Rename,
            val openFolder: () -> String,
        ) {
            data class Delete(
                val cancel: () -> String,
                val confirm: () -> String,
                val message: () -> String,
                val title: () -> String,
                val tooltip: () -> String,
                val unableClose: () -> String,
                val unableMessage: (instance: LauncherManifest) -> String,
                val unableTitle: () -> String
            )
            data class Rename(
                val cancel: () -> String,
                val confirm: () -> String,
                val error: () -> String,
                val prompt: () -> String,
                val title: () -> String
            )
        }

        data class Instance(
            val delete: Delete,
            val game: Game,
            val mods: () -> String,
            val options: () -> String,
            val play: () -> String,
            val resourcepacks: () -> String,
            val saves: () -> String,
            val title: () -> String,
            val version: () -> String
        ) {
            data class Delete(
                val cancel: () -> String,
                val confirm: () -> String,
                val message: () -> String,
                val title: () -> String,
                val tooltip: () -> String
            )
            data class Game(
                val errorMessage: (message: String) -> String,
                val errorTitle: () -> String,
                val preparingMessage: () -> String,
                val preparingTitle: () -> String,
                val runningMessage: () -> String,
                val runningTitle: () -> String,
                val crashClose: () -> String,
                val crashMessage: (message: String) -> String,
                val crashReports: () -> String,
                val crashTitle: () -> String
            )

        }

        data class Mods(
            val content: Content,
            val title: () -> String
        ) {
            data class Content(
                val delete: () -> String,
                val disable: () -> String,
                val enable: () -> String,
                val install: () -> String,
                val open: () -> String
            )
        }

        data class Options(
            val title: () -> String
        )

        data class Resourcepacks(
            val title: () -> String
        )

        data class Saves(
            val play: Play,
            val servers: () -> String,
            val title: () -> String,
            val worlds: () -> String
        ) {
            data class Play(
                val button: () -> String,
                val multipleClose: () -> String,
                val multipleMessage: () -> String,
                val multiplePlay: () -> String,
                val multipleTitle: () -> String,
                val noClose: () -> String,
                val noMessage: () -> String,
                val noTitle: () -> String
            )
        }
    }

    data class Settings(
        val appearance: () -> String,
        val cleanup: Cleanup,
        val language: () -> String,
        val logout: () -> String,
        val path: Path,
        val restartRequired: () -> String,
        val source: () -> String,
        val sourceTooltip: () -> String,
        val sync: Sync,
        val title: () -> String,
        val theme: () -> String,
        val update: Update,
        val user: () -> String,
        val version: () -> String
    ) {
        data class Cleanup(
            val button: () -> String,
            val cancel: () -> String,
            val close: () -> String,
            val confirm: () -> String,
            val deleting: () -> String,
            val failureMessage: () -> String,
            val failureTitle: () -> String,
            val libraries: () -> String,
            val message: () -> String,
            val success: () -> String,
            val title: () -> String
        )

        data class Path(
            val apply: () -> String,
            val changing: () -> String,
            val close: () -> String,
            val errorTitle: () -> String,
            val errorMessage: (IOException) -> String,
            val invalid: () -> String,
            val remove: () -> String,
            val select: () -> String,
            val success: () -> String,
            val title: () -> String
        )

        data class Sync(
            val close: () -> String,
            val failure: () -> String,
            val title: () -> String,
            val key: () -> String,
            val keyPlaceholder: () -> String,
            val port: () -> String,
            val success: () -> String,
            val test: () -> String,
            val url: () -> String,
        )

        data class Update(
            val available: () -> String,
            val availableMessage: (new: String, message: String?) -> String,
            val availableTitle: () -> String,
            val cancel: () -> String,
            val checkingTitle: () -> String,
            val close: () -> String,
            val download: () -> String,
            val downloadingMessage: (file: String, current: Int, total: Int) -> String,
            val downloadingTitle: () -> String,
            val latestMessage: () -> String,
            val latestTitle: () -> String,
            val successMessage: () -> String,
            val successRestart: () -> String,
            val successTitle: () -> String,
            val tooltip: () -> String,
            val unavailableMessage: () -> String,
            val unavailableTitle: () -> String
        )
    }

    data class SortBox(
        val sort: Sort,
        val reverse: () -> String,
    ) {
        data class Sort(
            val enabledName: () -> String,
            val lastPlayed: () -> String,
            val lastUsed: () -> String,
            val name: () -> String,
            val time: () -> String
        )
    }

    data class Sync(
        val complete: () -> String,
        val completeClose: () -> String,
        val download: Download,
        val status: Status,
        val syncing: () -> String,
        val unknown: () -> String
    ) {
        data class Download(
            val cancel: () -> String,
            val confirm: () -> String,
            val message: () -> String,
            val noneClose: () -> String,
            val noneTitle: () -> String,
            val title: () -> String,
        )

        data class Status(
            val collecting: () -> String,
            val creating: () -> String,
            val downloading: () -> String,
            val finished: () -> String,
            val starting: () -> String,
            val uploading: () -> String
        )
    }

    data class TextBox(
        val clear: () -> String,
    )

    data class Theme(
        val dark: () -> String,
        val light: () -> String,
        val system: () -> String
    )

    data class Units(
        val days: () -> String,
        val hours: () -> String,
        val minutes: () -> String,
        val seconds: () -> String,
        val megabytes: () -> String,
        val pixels: () -> String,
        val resolutionBy: () -> String,
        val approxTime: (Long) -> String = { secs ->
            if (secs < 60) {
                "${secs}${strings().units.seconds()}"
            } else if (secs < 60 * 60) {
                "${secs / 60}${strings().units.minutes()}"
            } else if (secs < 60 * 60 * 10) {
                "${secs / 3600}${strings().units.hours()} ${secs % 3600 / 60}${strings().units.minutes()}"
            } else if (secs < 60 * 60 * 24) {
                "${secs / 3600}${strings().units.hours()}"
            } else if (secs < 60 * 60 * 24 * 10) {
                "${secs / (3600 * 24)}${strings().units.days()} ${secs % (3600 * 24) / 3600}${strings().units.hours()}"
            } else {
                "${secs / (3600 * 24)}${strings().units.days()}"
            }
        },
        val accurateTime: (Long) -> String = { secs ->
            "${secs/3600}:${(secs%3600/60).toString().padStart(2,'0')}'${(secs%60).toString().padStart(2,'0')}''"
        }
    )

    data class Updater(
        val close: () -> String,
        val quit: () -> String,
        val status: Status
    ) {
        data class Status(
            val failureMessage: () -> String,
            val failureTitle: () -> String,
            val fatalMessage: () -> String,
            val fatalTitle: () -> String,
            val successMessage: () -> String,
            val successTitle: () -> String,
            val updatingMessage: () -> String,
            val updatingTitle: () -> String,
            val warningMessage: () -> String,
            val warningTitle: () -> String
        )
    }

    data class Version(
        val fabric: () -> String,
        val forge: () -> String,
        val vanilla: () -> String,
    )
}