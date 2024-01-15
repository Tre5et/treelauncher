package net.treset.treelauncher.localization

import com.multiplatform.webview.web.WebViewState
import net.treset.mc_version_loader.launcher.LauncherManifest
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
class LanguageInfo() {
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
    val creator: Creator,
    val error: Error,
    val game: Game,
    val language: Language,
    val launcher: Launcher,
    val login: Login,
    val manager: Manager,
    val menu: Menu,
    val nav: Nav,
    val news: News,
    val selector: Selector,
    val settings: Settings,
    val sorts: Sorts,
    val sync: Sync,
    val theme: Theme,
    val units: Units,
    val updater: Updater
) {
    data class Components(
        val create: () -> String,
        val details: Details
    ) {
        data class Details(
            val title: () -> String
        )
    }

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
                val value: () -> String,
                val vanilla: () -> String,
                val assets: () -> String,
                val libraries: () -> String,
                val fabric: () -> String
            )
        }

        data class Version(
            val errorVersion: () -> String,
            val errorLoader: () -> String,
            val errorType: () -> String,
            val showSnapshots: () -> String,
            val loader: () -> String,
            val loading: () -> String,
            val type: () -> String,
            val version: () -> String,
        )

        data class Mods(
            val version: () -> String
        )
    }

    data class Error(
        val close: () -> String,
        val message: (message: String) -> String,
        val title: () -> String,
        val severeClose: () -> String,
        val severeMessage: (message: String) -> String,
        val severeTitle: () -> String,
        val unknown: () -> String,
    )

    data class Game(
        val versionName: (instance: InstanceData) -> String,
        val versionType: (instance: InstanceData) -> String
    )

    data class Language(
        val english: (default: Boolean) -> String,
        val german: (default: Boolean) -> String
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
        val keepLoggedIn: () -> String
    ) {
        data class Label(
            val authenticating: () -> String,
            val failure: () -> String,
            val success: (name: String?) -> String
        )
    }

    data class Manager(
        val changeApply: () -> String,
        val component: Component,
        val instance: Instance,
        val mods: Mods
    ) {
        data class Component(
            val addCancel: () -> String,
            val addConfirm: () -> String,
            val addTitle: () -> String,
            val file: () -> String,
            val fileName: () -> String,
            val folder: () -> String,
            val includedFiles: () -> String,
            val settings: () -> String,
        )

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
                val argumentAdd: () -> String,
                val arguments: () -> String,
                val memory: () -> String,
                val resolution: () -> String,
                val title: () -> String,
            )
        }

        data class Mods(
            val add: () -> String,
            val local: Local,
            val change: Change,
            val changeVersion: () -> String,
            val current: () -> String,
            val search: Search,
            val update: Update,
            val version: () -> String,
        ) {
            data class Local(
                val cancel: () -> String,
                val confirm: () -> String,
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

            data class Change(
                val title: () -> String,
                val message: () -> String,
                val confirm: () -> String,
                val cancel: () -> String
            )

            data class Search(
                val add: () -> String,
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
    }

    data class Menu(
        val delete: () -> String,
        val edit: () -> String,
        val noSelection: () -> String,
        val folder: () -> String,
        val play: () -> String,
        val sync: () -> String
    )

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
        val importantTitle: () -> String,
        val title: () -> String
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
            val edit: Edit
        ) {
            data class Delete(
                val cancel: () -> String,
                val confirm: () -> String,
                val message: () -> String,
                val title: () -> String,
                val unableClose: () -> String,
                val unableMessage: (instance: LauncherManifest) -> String,
                val unableTitle: () -> String
            )
            data class Edit(
                val cancel: () -> String,
                val confirm: () -> String,
                val error: () -> String,
                val prompt: () -> String,
                val title: () -> String
            )
        }

        data class Saves(
            val play: Play,
            val servers: () -> String,
            val title: () -> String,
            val worlds: () -> String
        ) {
            data class Play(
                val multipleClose: () -> String,
                val multipleMessage: () -> String,
                val multiplePlay: () -> String,
                val multipleTitle: () -> String,
                val noClose: () -> String,
                val noMessage: () -> String,
                val noTitle: () -> String
            )
        }

        data class Resourcepacks(
            val title: () -> String
        )

        data class Options(
            val title: () -> String
        )

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

        data class Instance(
            val delete: Delete,
            val game: Game,
            val mods: () -> String,
            val options: () -> String,
            val resourcepacks: () -> String,
            val saves: () -> String,
            val title: () -> String,
            val version: () -> String
        ) {
            data class Delete(
                val cancel: () -> String,
                val confirm: () -> String,
                val message: () -> String,
                val title: () -> String
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
                val crashReport: () -> String,
                val crashTitle: () -> String
            )
        }
    }

    data class Settings(
        val appearance: () -> String,
        val language: () -> String,
        val logout: () -> String,
        val path: Path,
        val restratRequired: () -> String,
        val source: () -> String,
        val sourceTooltip: () -> String,
        val sync: Sync,
        val title: () -> String,
        val theme: () -> String,
        val update: Update,
        val user: () -> String,
        val version: () -> String
    ) {
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
            val availableMessage: (current: String, new: String) -> String,
            val availableTitle: () -> String,
            val cancel: () -> String,
            val checkingTitle: () -> String,
            val close: () -> String,
            val download: () -> String,
            val downloadingMessage: (file: String, current: Int, total: Int) -> String,
            val downloadingTitle: () -> String,
            val latestMessage: (current: String) -> String,
            val latestTitle: () -> String,
            val successMessage: () -> String,
            val successRestart: () -> String,
            val successTitle: () -> String,
            val tooltip: () -> String,
            val unavailableMessage: () -> String,
            val unavailableTitle: () -> String
        )
    }

    data class Sorts(
        val enabledName: () -> String,
        val lastPlayed: () -> String,
        val name: () -> String,
        val time: () -> String
    )

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
        val resolutionBy: () -> String
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
}