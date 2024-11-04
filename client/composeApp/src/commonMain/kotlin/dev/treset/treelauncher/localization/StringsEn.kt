package dev.treset.treelauncher.localization

import dev.treset.mcdl.auth.AuthenticationStep
import dev.treset.mcdl.mods.ModProvider
import dev.treset.mcdl.saves.Save
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.data.LauncherMod
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.components.instances.InstanceDetailsType

import kotlin.math.roundToInt

open class StringsEn(
    val components: Components = Components(),
    val comboBox: ComboBox = ComboBox(),
    val creator: Creator = Creator(),
    val changer: Changer = Changer(),
    val error: Error = Error(),
    val fixFiles: FixFiles = FixFiles(),
    val game: Game = Game(),
    val language: Language = Language(),
    val launcher: Launcher = Launcher(),
    val list: List = List(),
    val login: Login = Login(),
    val manager: Manager = Manager(),
    val nav: Nav = Nav(),
    val news: News = News(),
    val selector: Selector = Selector(),
    val settings: Settings = Settings(),
    val sortBox: SortBox = SortBox(),
    val sync: Sync = Sync(),
    val theme: Theme = Theme(),
    val units: Units = Units(),
    val updater: Updater = Updater(),
    val version: Version = Version(),
    val statusDetailsMessage: (String, Int, Int) -> String = { file, current, total -> if(file.isBlank()) "$current / $total" else "$file ($current/$total)" },
) {
    data class Components(
        val create: () -> String = { "Create New" },
        val details: Details = Details()
    ) {
        data class Details(
            val title: () -> String = { "No Component selected" }
        )
    }

    data class ComboBox(
        val loading: () -> String = { "Loading..." },
        val search: () -> String = { "Search" }
    )

    data class Creator(
        val buttonCreate: () -> String = { "Create" },
        val component: () -> String = { "Component" },
        val instance: Instance = Instance(),
        val mods: Mods = Mods(),
        val name: () -> String = { "Name" },
        val radioCreate: () -> String = { "Create" },
        val radioUse: () -> String = { "Use existing component" },
        val radioInherit: () -> String = { "Copy existing component" },
        val status: Status = Status(),
        val version: Version = Version()
    ) {
        data class Instance(
            val instance: () -> String = { "Instance" },
            val mods: () -> String = { "Mods" },
            val popup: Popup = Popup(),
            val resourcepacks: () -> String = { "Resourcepacks" },
            val saves: () -> String = { "Saves" },
            val title: () -> String = { "Create Instance" },
            val options: () -> String = { "Options" },
            val version: () -> String = { "Version" }
        ) {
            data class Popup(
                val back: () -> String = { "Close" },
                val backToInstances: () -> String = { "Back to Instances" },
                val creating: () -> String = { "Creating Instance..." },
                val failure: () -> String = { "Instance Creation failed.\nPlease report this!" },
                val success: () -> String = { "Instance successfully created" },
                val undefined: () -> String = { "Unknown instance creation status.\nPlease report this!" }
            )
        }

        data class Mods(
            val quiltIncludeFabric: () -> String = { "Include Fabric Mods" },
            val type: () -> String = { "Mod Loader" },
            val version: () -> String = { "Version" }
        )

        data class Status(
            val starting: () -> String = { "Preparing creation..." },
            val instance: () -> String = { "Creating instance..." },
            val message: Message = Message(),
            val mods: () -> String = { "Creating mods component..." },
            val options: () -> String = { "Creating options component..." },
            val resourcepacks: () -> String = { "Creating resourcepacks component..." },
            val saves: () -> String = { "Creating saves component..." },
            val version: Version = Version(),
            val java: () -> String = { "Downloading java version..." },
            val finishing: () -> String = { "Finishing creation..." }
        ) {
            data class Version(
                val assets: () -> String = { "Downloading assets..." },
                val fabric: () -> String = { "Creating fabric version..." },
                val fabricFile: () -> String = { "Downloading fabric version..." },
                val fabricLibraries: () -> String = { "Downloading fabric libraries..." },
                val file: () -> String = { "Downloading version..." },
                val forge: () -> String = { "Creating forge version..." },
                val forgeFile: () -> String = { "Patching forge version..." },
                val forgeLibraries: () -> String = { "Downloading forge libraries..." },
                val libraries: () -> String = { "Downloading libraries..." },
                val quilt: () -> String = { "Creating quilt version..." },
                val quiltLibraries: () -> String = { "Downloading quilt libraries..." },
                val value: () -> String = { "Creating version..." },
                val vanilla: () -> String = { "Creating minecraft version..." }
            )

            data class Message(
                val inheritFiles: () -> String = { "Inheriting files..." },
                val vanillaVersion: () -> String = { "Creating vanilla version..." }
            )
        }

        data class Version(
            val errorVersion: () -> String = { "No Version selected" },
            val errorType: () -> String = { "No Version Type selected" },
            val errorLoader: () -> String = { "No Fabric Version selected" },
            val fabric: () -> String = { "Fabric version" },
            val forge: () -> String = { "Forge version" },
            val showSnapshots: () -> String = { "Show Snapshots" },
            val loading: () -> String = { "Loading..." },
            val quilt: () -> String = { "Quilt version" },
            val type: () -> String = { "Version type" },
            val version: () -> String = { "Version" }
        )
    }

    data class Changer(
        val apply: () -> String = { "Apply Change" }
    )

    data class Error(
        val close: () -> String = { "Acknowledge" },
        val notification: (error: Exception) -> String = { error -> "An Error occurred: ${error.message ?: "Unknown Error"}. Click here to dismiss." },
        val severeClose: () -> String = { "Close launcher" },
        val severeMessage: (error: Exception) -> String = { error -> "Error:\n${error.message ?: "Unknown Error"}\nPlease report this error." },
        val severeTitle: () -> String = { "A severe error occurred!" },
        val unknown: () -> String = { "Unknown error" },
    )

    data class FixFiles(
        val cancel: () -> String = { "Don't attempt" },
        val close: () -> String = { "Close" },
        val confirm: () -> String = { "Attempt to restore launcher" },
        val failureMessage: () -> String = { "Trying again probably won't help here.\nTry to fix the files manually or contact the developer." },
        val failureTitle: () -> String = { "Failed to restore launcher!" },
        val message: () -> String = { "The launcher is not usable in this state.\nThis may be caused by an unexpected close while the game was running.\n\nAttempt to fix these files automatically?\nNon-world data from your last playing session may be lost.\n\nClose all game related files and folders before attempting.\nDO NOT START THIS WHILE A INSTANCE OF THE GAME IS RUNNING!" },
        val notification: () -> String = { "Inconsistent files detected! Click here to fix."},
        val runningMessage: () -> String = { "This may take a while." },
        val runningTitle: () -> String = { "Attempting to restore launcher..." },
        val successMessage: () -> String = { "The launcher should be usable again." },
        val successTitle: () -> String = { "Launcher successfully restored!" },
        val title: () -> String = { "Inconsistent launcher files detected!" }
    )

    data class Game(
        val versionName: (instance: InstanceComponent) -> String = { instance -> "${Strings.launcher.slug()}:${Strings.launcher.version()}:${instance.id.value.substring(0,3)}...${instance.id.value.substring(instance.id.value.length - 2)}"},
        val versionType: (instance: InstanceComponent) -> String = { instance -> instance.name.value }
    )

    data class Language(
        val systemLanguage: () -> String = { "system language" },
        val english: (default: Boolean) -> String = { default ->  "English${if (default) " (${Strings.language.systemLanguage()})" else ""}" },
        val german: (default: Boolean) -> String = { default ->  "Deutsch${if (default) " (${Strings.language.systemLanguage()})" else ""}" }
    )

    data class Launcher(
        val name: () -> String = { "TreeLauncher" },
        val patch: Patch = Patch(),
        val setup: Setup = Setup(),
        val slug: () -> String = { "treelauncher" },
        val status: Status = Status(),
        val version: () -> String = { "3.0.0" }
    ) {
        data class Setup(
            val initializing: () -> String = { "Initializing data directory" },
            val title: () -> String = { "Select a data directory" },
            val message: () -> String = { "Select a directory where your instances should be stored.\nThis can be an empty directory or existing launcher data." },
            val dirPicker: () -> String = { "Select a directory" },
            val error: (Exception) -> String = { "Invalid path: ${it.message}" }
        )

        data class Status(
            val preparing: (Int) -> String = { progress -> "Performing first time setup... $progress%" },
            val restartRequired: () -> String = { "Restart required. Please restart." }
        )

        data class Patch(
            val running: () -> String = { "Upgrading data..." },
            val message: () -> String = { "The data needs to be upgraded in order to work with this version of the launcher." },
            val title: () -> String = { "Launcher Data needs to be upgraded" },
            val backup: () -> String = { "Create Data Backup before upgrading" },
            val backupHint: () -> String = { "Depending on the amount of launcher data this may take a significant amount of time and storage." },
            val start: () -> String = { "Start Upgrade" },
            val status: Status = Status()
        ) {
            data class Status(
                val createBackup: () -> String = { "Creating backup" },
                val gameDataComponents: () -> String = { "Moving game data components" },
                val gameDataSaves: () -> String = { "Moving game data save components" },
                val gameDataMods: () -> String = { "Moving game data mod components" },
                val removeBackupIncludedFiles: () -> String = { "Removing backup from instance excluded files" },
                val upgradeComponents: () -> String = { "Upgrading components" },
                val upgradeMainManifest: () -> String = { "Upgrading launcher manifest" },
                val upgradeInstances: () -> String = { "Upgrading instances" },
                val upgradeSaves: () -> String = { "Upgrading save components" },
                val upgradeResourcepacks: () -> String = { "Upgrading resourcepack components" },
                val upgradeOptions: () -> String = { "Upgrading option components" },
                val upgradeMods: () -> String = { "Upgrading mod components" },
                val upgradeVersions: () -> String = { "Upgrading version components" },
                val upgradeJavas: () -> String = { "Upgrading java components" },
                val componentDirectories: () -> String = { "Renaming component directories" },
                val includedFiles: () -> String = { "Restructuring included files" },
                val includedFilesInstances: () -> String = { "Restructuring included files of instances" },
                val includedFilesSaves: () -> String = { "Restructuring included files of saves" },
                val includedFilesResourcepacks: () -> String = { "Restructuring included files of resourcepacks" },
                val includedFilesOptions: () -> String = { "Restructuring included files of options" },
                val includedFilesMods: () -> String = { "Restructuring included files of mods" },
                val removeResourcepacksArgument: () -> String = { "Removing resoucepacks directory version arguments" },
                val texturepacksIncludedFiles: () -> String = { "Adding texturepacks to included files" },
                val removeLogin: () -> String = { "Removing old login storage" },
                val upgradeSettings: () -> String = { "Upgrading Version in Settings" }
            )
        }
    }

    data class List(
        val compact: () -> String = { "Compact" },
        val full: () -> String = { "Comfortable" },
        val minimal: () -> String = { "Minimal" }
    )

    data class Login(
        val button: () -> String = { "Login with Microsoft" },
        val cancel: () -> String = { "Cancel Login" },
        val keepLoggedIn: () -> String = { "Stay logged in" },
        val label: Label = Label(),
        val logout: () -> String = { "Delete Saved Login Data" },
        val offline: () -> String = { "Start in Offline Mode" },
        val offlineNotification: () -> String = { "Offline Mode active. Functionality limited." },
        val popup: Popup = Popup(),
        val tip: () -> String = { "TIP: ${
            arrayOf(
                "Drag and Drop files into Saves, Resourcepack or Mods components to import them.",
                "Directly start a world or server by selecting it and clicking the play button.",
                "Customize the color of the Launcher in the settings.",
                "Click the news button at the top of the screen to view the latest news.",
                "Hover over the instance played time to see a more accurate representation.",
                "You can remove mods from an instance by selecting \"No Component\" in the instance options.",
                "Files can be stored in different locations. Change the data path in the settings.",
                "You can change the sorting of all component menus. Click the sort button next to the title.",
                "You can navigate all of the Launcher by using the Tab key.",
                "Drag and Drop files or folders into component settings to add the to included files.",
                "You can change how much space list items in a component take up by clicking the list display button next to the title.",
                "Display scaling can be adjusted in the settings.",
                "Enable discord integration in the settings to show your friends what you're playing."
            ).random()
        }" }
    ) {
        data class Label(
            val authenticating: (state: AuthenticationStep?) -> String = {
                "Logging you in..."
            },
            val authenticatingSub: (state: AuthenticationStep?) -> String = { state ->
                state?.let {
                    when(it) {
                        AuthenticationStep.MICROSOFT -> "Logging in with Microsoft"
                        AuthenticationStep.XBOX_LIVE -> "Obtaining Xbox Live Token"
                        AuthenticationStep.XBOX_SECURITY -> "Obtaining Xbox Security Token"
                        AuthenticationStep.MOJANG -> "Logging in with Mojang"
                        AuthenticationStep.MINECRAFT -> "Obtaining Minecraft Profile"
                    }
                } ?: ""
            },
            val failure: () -> String = { "Login failed. Please try again!" },
            val success: (String?) -> String = { user -> "Welcome, ${user ?: "Anonymous User"}!" },
            val offline: () -> String = { "Started in offline mode" }
        )

        data class Popup(
            val close: () -> String = { "Close" },
            val content: () -> Pair<String, String> = { "Open the following URL in your browser:" to "and enter the code:" },
            val copyContent: () -> String = { "Copy" },
            val open: () -> String = { "Open URL and copy Code" },
            val title: () -> String = { "Login with Microsoft" }
        )
    }

    data class Manager(
        val component: Component = Component(),
        val instance: Instance = Instance(),
        val mods: Mods = Mods(),
        val resourcepacks: Resourcepacks = Resourcepacks(),
        val saves: Saves = Saves()
    ) {
        data class Component(
            val addFile: () -> String = { "Add Included File" },
            val back: () -> String = { "Back" },
            val deleteFile: () -> String = { "Remove Included File" },
            val file: () -> String = { "File" },
            val fileName: () -> String = { "Enter Filename" },
            val folder: () -> String = { "Folder" },
            val import: Import = Import(),
            val includedFiles: () -> String = { "Included Files:" },
            val settings: () -> String = { "Component Settings" }
        ) {
            data class Import(
                val back: () -> String = { "Back" },
                val tooltipExpand: (Boolean) -> String = { expanded -> if(expanded) "Collapse" else "Expand" }
            )

            data class ImportStrings(
                val back: () -> String = { Strings.manager.component.import.back() },
                val delete: () -> String,
                val import: () -> String,
                val importComponent: () -> String,
                val importFile: () -> String,
                val importing: () -> String,
                val selectedFiles: () -> String,
                val tooltipAdd: () -> String,
                val tooltipFile: () -> String,
                val tooltipExpand: (expanded: Boolean) -> String = { Strings.manager.component.import.tooltipExpand(it) },
                val unknownCancel: () -> String,
                val unknownConfirm: () -> String,
                val unknownMessage: (file: LauncherFile) -> String,
                val unknownTitle: (file: LauncherFile) -> String
            )
        }

        data class Instance(
            val change: Change = Change(),
            val details: Details = Details(),
            val settings: Settings = Settings()
        ) {
            data class Change(
                val back: () -> String = { "Close" },
                val cancel: () -> String = { "Cancel" },
                val changing: () -> String = { "Changing Version..." },
                val confirm: () -> String = { "I know what I'm doing, Change" },
                val failure: () -> String = { "There was an error changing version.\nPlease report this." },
                val message: () -> String = { "This is likely to cause incompatibilities.\nIt is recommended to change versions by creating a new instance." },
                val noComponent: () -> String = { "No Component" },
                val success: () -> String = { "Version Changed!" },
                val title: () -> String = { "You are about to change the version of this Instance!" },
                val activeTitle: (InstanceDetailsType, String?) -> String = { type, name ->
                    when(type) {
                        InstanceDetailsType.SAVES -> Strings.manager.instance.details.saves()
                        InstanceDetailsType.RESOURCE_PACKS -> Strings.manager.instance.details.resourcepacks()
                        InstanceDetailsType.OPTIONS -> Strings.manager.instance.details.options()
                        InstanceDetailsType.MODS -> Strings.manager.instance.details.mods()
                        InstanceDetailsType.VERSION-> Strings.manager.instance.details.version()
                        InstanceDetailsType.SETTINGS -> Strings.manager.instance.details.settings()
                    }.let { ts ->
                        name?.let {
                            "$ts: $name"
                        } ?: ts
                    }
                }
            )

            data class Details(
                val version: () -> String = { "Version" },
                val saves: () -> String = { "Saves" },
                val resourcepacks: () -> String = { "Resourcepacks" },
                val options: () -> String = { "Options" },
                val mods: () -> String = { "Mods" },
                val settings: () -> String = { "Settings" }
            )

            data class Settings(
                val addArgument: () -> String = { "Add Argument" },
                val argumentPlaceholder: () -> String = { "Enter new Argument" },
                val arguments: () -> String = { "JVM-Arguments" },
                val deleteArgument: () -> String = { "Remove Argument" },
                val memory: () -> String = { "Instance Memory:" },
                val resolution: () -> String = { "Resolution:" },
                val title: () -> String = { "Instance Settings" }
            )
        }

        data class Mods(
            val add: () -> String = { "Add Mod" },
            val addMods: Add = Add(),
            val card: Card = Card(),
            val change: Change = Change(),
            val changeVersion: () -> String = { "Game Version:" },
            val current: () -> String = { "Current Mods" },
            val edit: Edit = Edit(),
            val empty: () -> String = { "No mods Found" },
            val import: ImportStrings = ImportStrings(),
            val searchPlaceholder: () -> String = { "Search for a Mod" },
            val settings: Settings = Settings(),
            val update: Update = Update(),
            val version: () -> String = { "Game Version" }
        ) {
            data class Add(
                val addLocal: () -> String = { "Add mod manually" },
                val back: () -> String = { "Back" },
                val search: () -> String = { "Search Online for a Mod" },
                val searchTooltip: () -> String = { "Search" },
                val loading: () -> String = { "Searching Mods..." },
                val noResults: () -> String = { "No appropriate Mods found." }
            )

            data class Card(
                val changeUsed: (Boolean) -> String = { enabled -> if(enabled) "Disable Mod" else "Enable Mod" },
                val delete: () -> String = { "Delete Mod" },
                val download: () -> String = { "Download Version" },
                val edit: () -> String = { "Edit Mod" },
                val openBrowser: () -> String = { "Open in Browser" },
                val versionPlaceholder: () -> String = { "Select a version" }
            )

            data class Change(
                val title: () -> String = { "You are about to change the game version associated with this component!" },
                val message: () -> String = { "This is likely to break compatibility with your instances.\nIt is generally only recommended to change version immediately after creating the component." },
                val confirm: () -> String = { "I know what I'm doing, Change" },
                val cancel: () -> String = { "Cancel" }
            )

            data class Edit(
                val cancel: () -> String = { "Cancel" },
                val confirm: (LauncherMod?) -> String = { current -> current?.let{ "Apply" } ?: "Add" },
                val curseforge: () -> String = { "Curseforge Project ID" },
                val curseforgeError: () -> String = { "Invalid Project ID" },
                val file: () -> String = { "File" },
                val fileError: () -> String = { "No file selected" },
                val modrinth: () -> String = { "Modrinth Project ID" },
                val modrinthError: () -> String = { "Invalid Project ID" },
                val name: () -> String = { "Name" },
                val version: () -> String = { "Version" },
                val versionError: () -> String = { "No version selected" }
            )

            data class ImportStrings(
                val delete: () -> String = { "Unselect Mod" },
                val displayName: (LauncherMod) -> String = { mod -> "${mod.name.value} v${mod.version.value}" },
                val import: () -> String = { "Import Mods" },
                val importComponent: () -> String = { "Select mods from other Components:" },
                val importFile: () -> String = { "Copy local mod files:" },
                val importing: () -> String = { "Importing Mods..." },
                val selectedFiles: () -> String = { "Selected Mods:" },
                val tooltipAdd: () -> String = { "Add Mod" },
                val tooltipExpand: (Boolean) -> String = { expanded -> if(expanded) "Collapse" else "Expand" },
                val tooltipFile: () -> String = { "Select Mod" }
            )

            data class Settings(
                val modProvider: (ModProvider) -> String = {
                    when (it) {
                        ModProvider.CURSEFORGE -> "Curseforge"
                        ModProvider.MODRINTH -> "Modrinth"
                    }
                },
                val order: (Boolean) -> String = { down -> if(down) "Deprioritize" else "Prioritize" },
                val providers: () -> String = { "Mod Source Priority" },
                val help: () -> String = { "If available, mods will be downloaded from the topmost source.\nIf a source is disallowed, versions will never be downloaded or searched from there." },
                val state: (Boolean) -> String = { enabled -> if(enabled) "Disallow" else "Allow" },
                val tooltip: () -> String = { "Open Settings" },
            )

            data class Update(
                val auto: () -> String = { "Automatically Update" },
                val disable: () -> String = { "Disable Mods without appropriate version" },
                val enable: () -> String = { "Enable disabled Mods"},
                val noUpdates: () -> String = { "No updates available" },
                val notViewed: (Int) -> String = { "$it more Update${if(it > 1) "s" else ""} available" },
                val remaining: (Int) -> String = { "Updating $it more Mod${if(it > 1) "s" else ""}..." },
                val settings: () -> String = { "Update Settings" },
                val tooltip: () -> String = { "Check for Updates" }
            )
        }

        data class Resourcepacks(
            val delete: () -> String = { "Delete Resourcepack" },
            val deleteTexturepack: () -> String = { "Delete Texturepack" },
            val deleteTexturepackTitle: () -> String = { "You are about to delete this texturepack!" },
            val deleteTitle: () -> String = { "You are about to delete this resourcepack!" },
            val deleteMessage: () -> String = { "This action cannot be undone!" },
            val deleteConfirm: () -> String = { "Yes, delete" },
            val deleteCancel: () -> String = { "Cancel" },
            val import: Component.ImportStrings = Component.ImportStrings(
                delete = { "Unselect Resourcepacks" },
                import = { "Import Resourcepacks" },
                importComponent = { "Select resourcepacks from other Components:" },
                importFile = { "Copy local resourcepack files:" },
                importing = { "Importing Resourcepacks..." },
                selectedFiles = { "Selected Resourcepacks:" },
                tooltipAdd = { "Add Resourcepack" },
                tooltipFile = { "Select Resourcepack" },
                unknownCancel = { "Cancel" },
                unknownConfirm = { "Add Resourcepack" },
                unknownMessage = { file -> "The file \"${file.name}\" doesn't have any resourcepack format the launcher is aware of.\nDo you want to add it anyways?" },
                unknownTitle = { "Unknown Resourcepack Format" }
            ),
            val tooltipAdd: () -> String = { "Add Resourcepack" }
        )

        data class Saves(
            val delete: () -> String = { "Delete World" },
            val deleteTitle: (Save) -> String = { world -> "You are about to delete the world \"${world.name}\"!" },
            val deleteMessage: (Save) -> String = { "This action cannot be undone!\nAny data in this world will be lost forever." },
            val deleteConfirm: (Save) -> String = { world -> "Yes, delete the world \"${world.name}\" forever" },
            val deleteCancel: () -> String = { "Cancel" },
            val import: Component.ImportStrings = Component.ImportStrings(
                delete = { "Unselect World" },
                import = { "Import Worlds" },
                importComponent = { "Select worlds from other Components:" },
                importFile = { "Copy local world files:" },
                importing = { "Importing Worlds..." },
                selectedFiles = { "Selected Worlds:" },
                tooltipAdd = { "Add World" },
                tooltipFile = { "Select World" },
                unknownCancel = { "Cancel" },
                unknownConfirm = { "Add World" },
                unknownMessage = { file -> "The file \"${file.name}\" doesn't have any world format the launcher is aware of.\nDo you want to add it anyways?" },
                unknownTitle = { "Unknown World Format" }
            ),
            val tooltipAdd: () -> String = { "Add World" }
        )
    }

    data class Nav(
        val add: () -> String = { "Create Instance" },
        val home: () -> String = { "Instances" },
        val mods: () -> String = { "Mods Components" },
        val options: () -> String = { "Options Components" },
        val resourcepacks: () -> String = { "Resourcepacks Components" },
        val saves: () -> String = { "Saves Components" },
        val settings: () -> String = { "Settings" }
    )

    data class News(
        val close: () -> String = { "Close" },
        val important: () -> String = { "Important News:" },
        val loading: () -> String = { "Loading News..." },
        val none: () -> String = { "No current News" },
        val notification: () -> String = { "New News available! Click here to view." },
        val other: () -> String = { "News:"},
        val tooltip: () -> String = { "Open News" },
        val title: () -> String = { "News" }
    )

    data class Selector(
        val component: Component = Component(),
        val instance: Instance = Instance(),
        val mods: Mods = Mods(),
        val options: Options = Options(),
        val resourcepacks: Resourcepacks = Resourcepacks(),
        val saves: Saves = Saves()
    ) {
        data class Component(
            val delete: Delete = Delete(),
            val rename: Rename = Rename(),
            val openFolder: () -> String = { "Open in File Explorer" }
        ) {
            data class Delete(
                val cancel: () -> String = { "Cancel" },
                val confirm: () -> String = { "Delete" },
                val message: () -> String = { "This action cannot be undone!\nAny data in this Component will be lost forever." },
                val title: () -> String = { "You are about to delete this Component!" },
                val tooltip: () -> String = { "Delete Component" },
                val unableClose: () -> String = { "Close" },
                val unableMessage: (dev.treset.treelauncher.backend.data.manifest.Component) -> String = { instance -> "It is used by the following instance: ${instance.name.value}" },
                val unableTitle: () -> String = { "Unable to delete this component!" },
            )

            data class Rename(
                val cancel: () -> String = { "Cancel" },
                val confirm: () -> String = { "Save" },
                val error: () -> String = { "Not a valid name" },
                val prompt: () -> String = { "New Name" },
                val title: () -> String = { "Rename Component" }
            )
        }

        data class Instance(
            val delete: Delete = Delete(),
            val empty: () -> Pair<String, String> = { "Click the" to "at the bottom to create one." },
            val emptyTitle: () -> String = { "No Instances created yet." },
            val game: Game = Game(),
            val mods: () -> String = { "Mods Component" },
            val options: () -> String = { "Options Component" },
            val play: () -> String = { "Start Instance" },
            val resourcepacks: () -> String = { "Resourcepacks Component" },
            val saves: () -> String = { "Saves Component" },
            val title: () -> String = { "Instances" },
            val version: () -> String = { "Version" }
        ) {
            data class Delete(
                val cancel: () -> String = { "Cancel" },
                val confirm: () -> String = { "Delete" },
                val message: () -> String = { "This cannot be undone.\nAll used components will still exist after deletion." },
                val title: () -> String = { "You are about to delete this Instance!" },
                val tooltip: () -> String = { "Delete Instance" }
            )

            data class Game(
                val cleanupFailCancel: () -> String = { "Cancel; launcher will be unusable" },
                val cleanupFailMessage: () -> String = { "The game files were unable to be moved to their destination.\nMake sure to close any game related folders and files before retrying.\nIf you cancel this, the launcher will not be function afterwards." },
                val cleanupFailRetry: () -> String = { "Retry" },
                val cleanupFailTitle: () -> String = { "Resource cleanup failed!" },
                val errorMessage: (String) -> String = { message -> "Error:\n$message\nPlease report this error." },
                val errorTitle: () -> String = { "Game Launch Failed!" },
                val exitingMessage: () -> String = { "The game will exit shortly..." },
                val exitingTitle: () -> String = { "Cleaning Game Resources..." },
                val preparingMessage: () -> String = { "The Game will start shortly." },
                val preparingTitle: () -> String = { "Preparing Game Resources..." },
                val runningMessage: () -> String = { "Close the game to be able to perform actions in the launcher." },
                val runningNotification: (InstanceComponent) -> String = { instance -> "Currently Playing: ${instance.name.value}" },
                val runningOpen: () -> String = { "Open Game folder" },
                val runningStop: () -> String = { "Kill Game process" },
                val runningTitle: () -> String = { "The Game is running..." },
                val crashClose: () -> String = { "Close" },
                val crashMessage: (String) -> String = { message -> "Error:\n$message\nThis might be unrelated to the launcher." },
                val crashReports: () -> String = { "Open crash reports" },
                val crashTitle: () -> String = { "The Game exited unexpectedly" }
            )
        }

        data class Mods(
            val content: Content = Content(),
            val empty: () -> Pair<String, String> = { "Drag and Drop Mods here or click the" to "at the top to add a mod." },
            val emptyTitle: () -> String = { "No Mods added yet." },
            val title: () -> String = { "Mods" }
        ) {
            data class Content(
                val delete: () -> String = { "Delete Mod" },
                val disable: () -> String = { "Disable Mod" },
                val enable: () -> String = { "Enable Mod" },
                val install: () -> String = { "Install Version" },
                val open: () -> String = { "Open in Browser" }
            )
        }

        data class Options(
            val title: () -> String = { "Options" }
        )

        data class Resourcepacks(
            val empty: () -> Pair<String, String> = { "Drag and Drop Resourcepacks here or click the" to "at the top to import some." },
            val emptyTitle: () -> String = { "No Resourcepacks added yet." },
            val resourcepacks: () -> String = { "Resourcepacks:" },
            val texturepacks: () -> String = { "Texturepacks:" },
            val title: () -> String = { "Resourcepacks" }
        )

        data class Saves(
            val empty: () -> Pair<String, String> = { "Drag and Drop Worlds here or click the" to "at the top to import some." },
            val emptyTitle: () -> String = { "No Worlds added yet." },
            val play: Play = Play(),
            val servers: () -> String = { "Servers:" },
            val title: () -> String = { "Saves" },
            val worlds: () -> String = { "Worlds:" }
        ) {
            data class Play(
                val button: () -> String = { "Start World" },
                val multipleClose: () -> String = { "Cancel" },
                val multipleMessage: () -> String = { "Which instance should this world be launched in?" },
                val multiplePlay: () -> String = { "Start World" },
                val multipleTitle: () -> String = { "Multiple instances are using this component." },
                val noClose: () -> String = { "Close" },
                val noMessage: () -> String = { "Quick Play is only available if the world is in a used component." },
                val noTitle: () -> String = { "No instance is using this component." }
            )
        }
    }

    data class Settings(
        val appearance: Appearance = Appearance(),
        val cleanup: Cleanup = Cleanup(),
        val debugNotification: (Boolean) -> String = { "Debug mode ${if(it) "enabled" else "disabled"}!" },
        val discord: Discord = Discord(),
        val language: () -> String = { "Language:" },
        val logout: () -> String = { "Logout" },
        val path: Path = Path(),
        val resetWindow: () -> String = { "Reset Window Position and Size" },
        val source: () -> String = { "Source Repository" },
        val sourceTooltip: () -> String = { "Open Source Repository" },
        val sync: Sync = Sync(),
        val title: () -> String = { "Settings" },
        val theme: Theme = Theme(),
        val update: Update = Update(),
        val updateUrl: UpdateUrl = UpdateUrl(),
        val user: () -> String = { "Logged in as:" },
        val version: () -> String = { "Version: v${Strings.launcher.version()}" }
    ) {
        data class Appearance(
            val decrement: () -> String = { "Decrease by 10%" },
            val displayScale: () -> String = { "Display Scale:" },
            val increment: () -> String = { "Increase by 10%" },
            val largeHint: () -> String = { "Large display scaling may cut off content" },
            val scaling: (Int) -> String = {
                "${(it / 10f).roundToInt()}%"
            },
            val smallHint: () -> String = { "Small display scaling may compromise readability" },
            val title: () -> String = { "Appearance" },
            val tooltipAdvanced: (Boolean) -> String = { "${if(it) "Hide" else "Show"} Advanced Customization" },
            val background: () -> String = { "Background" },
            val backgroundTooltip: () -> String = { "Choose Background Color" },
            val container: () -> String = { "Container" },
            val containerTooltip: () -> String = { "Choose Container Color" },
            val text: () -> String = { "Text" },
            val textLight: () -> String = { "Choose Light Text Color" },
            val textDark: () -> String = { "Choose Dark Text Color" },
            val reset: () -> String = { "Revert Colors to Default" }
        )

        data class Cleanup(
            val button: () -> String = { "Delete unused Files" },
            val cancel: () -> String = { "Cancel" },
            val close: () -> String = { "Close" },
            val confirm: () -> String = { "Delete Files" },
            val deleting: () -> String = { "Deleting Files..." },
            val failureMessage: () -> String = { "The files could not be deleted.\nThis will probably not affect launcher functionality.\nDetails were written to the logs." },
            val failureTitle: () -> String = { "Failed to delete unused files!" },
            val libraries: () -> String = { "Also delete unused Libraries" },
            val message: () -> String = { "This will delete all version files that are not used by any Instance to free up space.\nVersions can be reinstalled at any time." },
            val success: () -> String = { "Unused files have been deleted." },
            val title: () -> String = { "Delete unused Files" }
        )

        data class Discord(
            val versionLoader: (String, String) -> String = {version, modLoader ->
                val builder = StringBuilder()
                if(AppSettings.discordShowVersion.value) {
                    builder.append(version)
                }

                if(AppSettings.discordShowVersion.value && AppSettings.discordShowModLoader.value && modLoader != "vanilla") {
                    builder.append(" ")
                }

                if(AppSettings.discordShowModLoader.value && modLoader != "vanilla") {
                    builder.append(modLoader)
                }
                builder.toString()
            },
            val details: (String, String, String) -> String = { name, version, modLoader ->
                val builder = StringBuilder()
                if(AppSettings.discordShowInstance.value) {
                    builder.append(name)
                    if(AppSettings.discordShowVersion.value || AppSettings.discordShowModLoader.value && modLoader != "vanilla") {
                        builder.append(" (")
                        builder.append(versionLoader(version, modLoader))
                        builder.append(")")
                    }
                } else {
                    builder.append(versionLoader(version, modLoader))
                }
                if(AppSettings.discordShowWatermark.value) {
                    builder.append(Strings.settings.discord.watermark())
                }
                builder.toString()
            },
            val instanceExample: () -> String = { "MyInstance" },
            val instanceToggle: () -> String = { "Show instance name" },
            val modLoaderExample: () -> String = { "Fabric" },
            val modLoaderToggle: () -> String = { "Show mod loader" },
            val timeExample: () -> String = { "01:36" },
            val timeSuffix: () -> String = { " elapsed" },
            val timeToggle: () -> String = { "Show playtime" },
            val title: () -> String = { "Discord Integration" },
            val versionExample: () -> String = { "1.20.5" },
            val versionToggle: () -> String = { "Show game version" },
            val watermark: () -> String = { " via TreeLauncher" },
            val watermarkToggle: () -> String = { "Show launcher name" }
        )

        data class Path(
            val apply: () -> String = { "Apply" },
            val changing: () -> String = { "Changing path..." },
            val close: () -> String = { "Close" },
            val errorTitle: () -> String = { "Failed to change path" },
            val errorMessage: (Exception) -> String = { e -> "An error occurred:\n$e" },
            val invalid: () -> String = { "No valid folder provided" },
            val copyData: () -> String = { "Copy files to destination" },
            val remove: () -> String = { "Remove files from old location" },
            val select: () -> String = { "Select a Folder" },
            val success: () -> String = { "Successfully changed path" },
            val title: () -> String = { "Launcher Data Directory" }
        )

        data class Sync(
            val close: () -> String = { "Close" },
            val failure: () -> String = { "Test failed!" },
            val title: () -> String = { "Component Synchronization" },
            val key: () -> String = { "API-Key:" },
            val keyPlaceholder: () -> String = { "Key" },
            val port: () -> String = { "Port" },
            val success: () -> String = { "Test successfully!" },
            val test: () -> String = { "Test" },
            val url: () -> String = { "URL" }
        )

        data class Theme(
            val cancel: () -> String = { "Cancel" },
            val confirm: () -> String = { "Apply" },
            val title: () -> String = { "Select a custom accent color:" }
        )

        data class Update(
            val available: () -> String = { "Update Available!" },
            val availableMessage: (String, String?) -> String = { new, message -> "Update: v${Strings.launcher.version()} → v$new ${message?.let { "\n\n$it" } ?: ""}" },
            val availableTitle: () -> String = { "Update Available!" },
            val cancel: () -> String = { "Cancel" },
            val checkingTitle: () -> String = { "Checking for updates..." },
            val close: () -> String = { "Close" },
            val download: () -> String = { "Download" },
            val downloadingTitle: () -> String = { "Downloading Update..." },
            val latestMessage: () -> String = { "Current Version: v${Strings.launcher.version()}" },
            val latestTitle: () -> String = { "Everything is up to date!" },
            val successMessage: () -> String = { "Restart the launcher for these changes to take effect." },
            val successRestart: () -> String = { "Restart Now" },
            val successTitle: () -> String = { "Update successfully downloaded." },
            val tooltip: () -> String = { "Check for updates" },
            val unavailableMessage: () -> String = { "An update is available, but this version of the launcher can not automatically update to it.\nCheck online how to manually update." },
            val unavailableTitle: () -> String = { "Not able to update." }
        )

        data class UpdateUrl(
            val apply: () -> String = { "Apply" },
            val popupClose: () -> String = { "Close" },
            val popupMessage: (Exception) -> String = { e -> "The following error occured:\n${e.message}" },
            val popupTitle: () -> String = { "Invalid Update URL" },
            val title: () -> String = { "Update URL" }
        )
    }

    data class SortBox(
        val sort: Sort = Sort(),
        val reverse: () -> String = { "Reverse" }
    ) {
        data class Sort(
            val enabledName: () -> String = { "Name (Enabled first)" },
            val lastPlayed: () -> String = { "Last Played" },
            val lastUsed: () -> String = { "Last Used" },
            val name: () -> String = { "Name" },
            val time: () -> String = { "Time Played" }
        )
    }

    data class Sync(
        val complete: () -> String = { "Successfully synchronized Component" },
        val completeClose: () -> String = { "Close" },
        val download: Download = Download(),
        val status: Status = Status(),
        val syncing: () -> String = { "Synchronizing Component..." },
        val unknown: () -> String = { "<Unknown Name>" }
    ) {
        data class Download(
            val cancel: () -> String = { "Cancel" },
            val confirm: () -> String = { "Download" },
            val message: () -> String = { "Select a component to download" },
            val noneClose: () -> String = { "Close" },
            val noneTitle: () -> String = { "No new Components available" },
            val title: () -> String = { "Download Component" }
        )

        data class Status(
            val collecting: () -> String = { "Collecting Synchronization Data..." },
            val creating: () -> String = { "Downloading required version..." },
            val downloading: () -> String = { "Downloading Files..." },
            val finished: () -> String = { "Synchronisation has finished." },
            val starting: () -> String = { "Getting started..." },
            val uploading: () -> String = { "Uploading Files..." }
        )
    }

    data class Theme(
        val dark: () -> String = { "Dark" },
        val light: () -> String = { "Light" },
        val system: () -> String = { "Match System" },
        val green: () -> String = { "Green" },
        val blue: () -> String = { "Blue" },
        val magenta: () -> String = { "Magenta" },
        val orange: () -> String = { "Orange" },
        val custom: () -> String = { "Custom" }
    )

    data class Units(
        val days: () -> String = { "d" },
        val hours: () -> String = { "h" },
        val minutes: () -> String = { "m" },
        val seconds: () -> String = { "s" },
        val megabytes: () -> String = { "mb" },
        val pixels: () -> String = { "px" },
        val resolutionBy: () -> String = { "x" },
        val approxTime: (Long) -> String = { secs ->
            if (secs < 60) {
                "${secs}${Strings.units.seconds()}"
            } else if (secs < 60 * 60) {
                "${secs / 60}${Strings.units.minutes()}"
            } else if (secs < 60 * 60 * 10) {
                "${secs / 3600}${Strings.units.hours()} ${secs % 3600 / 60}${Strings.units.minutes()}"
            } else if (secs < 60 * 60 * 24) {
                "${secs / 3600}${Strings.units.hours()}"
            } else if (secs < 60 * 60 * 24 * 10) {
                "${secs / (3600 * 24)}${Strings.units.days()} ${secs % (3600 * 24) / 3600}${Strings.units.hours()}"
            } else {
                "${secs / (3600 * 24)}${Strings.units.days()}"
            }
        },
        val accurateTime: (Long) -> String = { secs ->
            "${secs/3600}:${(secs%3600/60).toString().padStart(2,'0')}'${(secs%60).toString().padStart(2,'0')}''"
        }
    )

    data class Updater(
        val close: () -> String = { "Close" },
        val quit: () -> String = { "Quit Launcher" },
        val status: Status = Status()
    ) {
        data class Status(
            val failureMessage: () -> String = { "The previous version was fully restored.\nDetails were written to the logs.\nPlease report this error." },
            val failureTitle: () -> String = { "The update failed." },
            val fatalMessage: () -> String = { "The launcher can no longer be used.\nDetails were written to the logs.\nPlease report this error and resolve the error manually." },
            val fatalTitle: () -> String = { "A fatal error occurred during the update." },
            val successMessage: () -> String = { "The new version is applied." },
            val successTitle: () -> String = { "The launcher was successfully updated." },
            val updatingMessage: () -> String = { "The launcher can not be used while the updater is running.\nClose the launcher to continue the update." },
            val updatingTitle: () -> String = { "The updater is still running." },
            val warningMessage: () -> String = { "Cleaning up temporary resources failed.\nThis will probably not affect usage.\nDetails were written to the logs." },
            val warningTitle: () -> String = { "The launcher was updated." }
        )
    }

    data class Version(
        val fabric: () -> String = { "Fabric" },
        val forge: () -> String = { "Forge" },
        val forgeHint: () -> String = { "Forge support is currently experimental" },
        val forgeTooltip: () -> String = { "Installing most common versions should work but installations may fail or produce unusable versions.\nFeel free to report any issues but don't expect quick fixes." },
        val quilt: () -> String = { "Quilt" },
        val vanilla: () -> String = { "Vanilla" },
    )
}