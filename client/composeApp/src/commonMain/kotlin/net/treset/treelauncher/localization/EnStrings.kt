package net.treset.treelauncher.localization

import net.treset.treelauncher.instances.InstanceDetails

class EnStrings : Strings(
    Components(
        { "Create New" },
        Components.Details (
            { "No Component selected" }
        )
    ),
    Creator(
        { "Create" },
        { "Component" },
        { "Name must not be empty" },
        { "No Component selected"},
        Creator.Instance(
            { "Instance" },
            { "Mods" },
            Creator.Instance.Popup(
                { "Close" },
                { "Back to Instances" },
                { "Creating Instance..." },
                { "Instance Creation failed.\nPlease report this!" },
                { "Instance successfully created" },
                { "Unknown instance creation status.\nPlease report this!" }
            ),
            { "Resourcepacks" },
            { "Saves" },
            { "Create Instance" },
            { "Options" },
            { "Version" },

        ),
        Creator.Mods(
            { "Version" }
        ),
        { "Name" },
        { "Create" },
        { "Use existing component" },
        { "Copy existing component" },
        Creator.Status(
            { "Preparing creation..." },
            { "Creating mods component..." },
            { "Creating options component..." },
            { "Creating resourcepacks component..." },
            { "Creating saves component..." },
            Creator.Status.Version(
                { "Creating version..." },
                { "Creating minecraft version..." },
                { "Downloading assets..." },
                { "Downloading libraries..." },
                { "Creating fabric version..." }
            ),
            { "Downloading java version..." },
            { "Finishing creation..." }
        ),
        Creator.Version(
            { "No Version selected" },
            { "No Version Type selected" },
            { "No Fabric Version selected" },
            { "Show Snapshots" },
            { "Fabric version" },
            { "Loading..." },
            { "Version type" },
            { "Version" }
        )
    ),
    Error(
        { "Acknowledge" },
        { message -> "Error:\n$message\nPlease report this error." },
        { "An error occurred!" },
        { "Close launcher" },
        { message -> "Error:\n$message\nPlease report this error." },
        { "A severe error occurred!" },
        { "Unknown error" }
    ),
    Game(
        { instance -> "${strings().launcher.slug()}:${strings().launcher.version()}:${instance.instance.first.id.substring(0,3)}...${instance.instance.first.id.substring(instance.instance.first.id.length - 2)}"},
        { instance -> instance.instance.first.name }
    ),
    Language(
        { default ->  "English${if (default) " (system language)" else ""}" },
        { default -> "German${if (default) " (system language)" else ""}" }
    ),
    Launcher(
        status = Launcher.Status(
            { progress -> "Performing first time setup... $progress%" },
            { "Restart required. Please restart." }
        )
    ),
    Login(
        { state -> "Login: ${state.pageTitle ?: "Loading..."} (${state.lastLoadedUrl ?: "Getting url..."})" },
        { "Login with Microsoft" },
        Login.Label(
            { "Logging you in..." },
            { "Login failed. Please try again!" },
            { user -> "Welcome, ${user?: "Anonymous User"}!" }
        ),
        { "Stay logged in" }
    ),
    Manager(
        { "Apply Change" },
        Manager.Component(
            { "Cancel" },
            { "Add" },
            { "Add included File" },
            { "File" },
            { "Enter Filename" },
            { "Folder" },
            { "Included Files:" },
            { "Component Settings" }
        ),
        Manager.Instance(
            Manager.Instance.Change(
                { "Close" },
                { "Cancel" },
                { "Changing Version..." },
                { "I know what I'm doing, Change" },
                { "There was an error changing version.\nPlease report this." },
                { "This is likely to cause incompatibilities.\nIt is recommended to change versions by creating a new instance." },
                { "No Component" },
                { "Version Changed!" },
                { "Select a component" },
                { type, name ->
                    when(type) {
                        InstanceDetails.SAVES -> strings().manager.instance.details.saves()
                        InstanceDetails.RESOURCE_PACKS -> strings().manager.instance.details.resourcepacks()
                        InstanceDetails.OPTIONS -> strings().manager.instance.details.options()
                        InstanceDetails.MODS -> strings().manager.instance.details.mods()
                        InstanceDetails.VERSION-> strings().manager.instance.details.version()
                        InstanceDetails.SETTINGS -> strings().manager.instance.details.settings()
                    }.let { ts ->
                        name?.let {
                            "$ts: $name"
                        } ?: ts
                    }
                }
            ),
            Manager.Instance.Details(
                { "Version" },
                { "Saves" },
                { "Resourcepacks" },
                { "Options" },
                { "Mods" },
                { "Settings" }
            ),
            Manager.Instance.Settings(
                { "Enter new Argument" },
                { "JVM-Arguments" },
                { "Instance Memory:" },
                { "Resolution:" },
                { "Instance Settings" }
            )
        ),
        Manager.Mods(
            { "Add local mod" },
            Manager.Mods.Local(
                { "Add" },
                { "Cancel" },
                { "Curseforge Project ID" },
                { "Invalid Project ID" },
                { "File" },
                { "No file selected" },
                { "Modrinth Project ID" },
                { "Invalid Project ID" },
                { "Name" },
                { "Version" },
                { "No version selected" }
            ),
            Manager.Mods.Change(
                { "You are about to change the game version associated with this component!" },
                { "This is likely to break compatibility with your instances.\nIt is generally only recommended to change version immediately after creating the component." },
                { "I know what I'm doing, Change" },
                { "Cancel" }
            ),
            { "Game Version:" },
            { "Current Mods" },
            Manager.Mods.Search(
                { "Add mod" },
                { "Back" },
                { "Search for a Mod" },
                { "Search" },
                { "Searching Mods..."},
                { "No appropriate Mods found." }
            ),
            Manager.Mods.Update(
                { "Automatically Update" },
                { "Disable Mods without appropriate version" },
                { "Enable disabled Mods"},
                { "Check for Updates" }
            ),
            { "Game Version" }
        )
    ),
    Menu(
        { "Delete" },
        { "Rename" },
        { "Nothing selected" },
        { "Open in File Explorer" },
        { "Start" },
        { "Upload Component" }
    ),
    Nav(
        { "Create Instance" },
        { "Instances" },
        { "Mods Components" },
        { "Options Components" },
        { "Resourcepacks Components" },
        { "Saves Components" },
        { "Settings" }
    ),
    News(
        { "Close" },
        { "Important News:" },
        { "News:"}
    ),
    Selector(
        Selector.Component(
            Selector.Component.Delete(
                { "Cancel" },
                { "Delete" },
                { "This action cannot be undone!\nAny data in this Component will be lost forever.\nThis Component is not currently used by any instance." },
                { "You are about to delete this Component!" },
                { "Close" },
                { instance -> "It is used by the following instance: ${instance.name}" },
                { "Unable to delete this component!" },
            ),
            Selector.Component.Edit(
                { "Cancel" },
                { "Save" },
                { "Not a valid name" },
                { "New Name" },
                { "Rename Component" }
            )
        ),
        Selector.Instance(
            Selector.Instance.Delete(
                { "Cancel" },
                { "Delete" },
                { "This cannot be undone.\nAll used components will still exist after deletion." },
                { "You are about to delete this Instance!" }
            ),
            Selector.Instance.Game(
                { message -> "Error:\n$message\nPlease report this error."},
                { "Game Launch Failed!" },
                { "The Game will start shortly." },
                { "Preparing Game Resources..." },
                { "Close the game to be able to perform actions in the launcher." },
                { "The Game is running..." },
                { "Close" },
                { message -> "Error:\n$message\nThis might be unrelated to the launcher." },
                { "Open crash reports" },
                { "The Game exited unexpectedly" }
            ),
            { "Mods Component" },
            { "Options Component" },
            { "Resourcepacks Component" },
            { "Saves Component" },
            { "Instances" },
            { "Version" }
        ),
        Selector.Mods(
            Selector.Mods.Content(
                { "Delete Mod" },
                { "Disable Mod" },
                { "Enable Mod" },
                { "Install Version" },
                { "Open in Browser" }
            ),
            { "Mods" }
        ),
        Selector.Options(
            { "Options" }
        ),
        Selector.Resourcepacks(
            { "Resourcepacks" }
        ),
        Selector.Saves(
            Selector.Saves.Play(
                { "Cancel" },
                { "Which instance should this world be launched in?" },
                { "Start World" },
                { "Multiple instances are using this component." },
                { "Close" },
                { "Quick Play is only available if the world is in a used component." },
                { "No instance is using this component." }
            ),
            { "Servers:" },
            { "Saves" },
            { "Worlds:"}
        )
    ),
    Settings(
        { "Appearance" },
        { "Language:" },
        { "Logout" },
        Settings.Path(
            { "Apply" },
            { "Changing path..." },
            { "Close" },
            { "Failed to change path" },
            { e -> "An error occured:\n$e"},
            { "No valid folder provided" },
            { "Remove files from old location" },
            { "Select a Folder" },
            { "Successfully changed path" },
            { "Launcher Data Directory"}
        ),
        { "A restart is required for this to take effect" },
        { "Source Repository" },
        { "Open Source Repository" },
        Settings.Sync(
            { "Close" },
            { "Test failed!" },
            { "Component Synchorization" },
            { "API-Key:"},
            { "Key" },
            { "Port" },
            { "Test successfull!" },
            { "Test" },
            { "URL" }
        ),
        { "Settings" },
        { "Theme:" },
        Settings.Update(
            { "Update Available!" },
            { current, new ->  "Update: v$current → v$new"},
            { "Update Available!" },
            { "Cancel" },
            { "Checking for updates..." },
            { "Close" },
            { "Download" },
            { file, current, total -> "$file ($current/$total)" },
            { "Downloading Update..." },
            { version -> "Current Version: v$version" },
            { "Everything is up to date!" },
            { "Restart the launcher for these changes to take effect." },
            { "Restart Now" },
            { "Update successfully downloaded." },
            { "Check for updates" },
            { "An update is available, but this version of the launcher can not automatically update to it.\nCheck online how to manually update." },
            { "Not able to update." }
        ),
        { "Logged in as:" },
        { "Version: v${strings().launcher.version()}" }
    ),
    Sorts(
        { "Name (Enabled first)" },
        { "Last Played" },
        { "Name" },
        {"Time Played" }
    ),
    Sync(
        { "Successfully synchronized Component" },
        { "Close" },
        Sync.Download(
            { "Cancel" },
            { "Download" },
            { "Select a component to download" },
            { "Close" },
            { "No new Components available" },
            { "Download Component" }
        ),
        Sync.Status(
            { "Collecting Synchronization Data..." },
            { "Downloading required version..." },
            { "Downloading Files..." },
            { "Synchronisation has finished." },
            { "Getting started..." },
            { "Uploading Files..." }
        ),
        { "Synchronizing Component..."},
        { "<Unknown Name>" }
    ),
    Theme(
        { "Dark" },
        { "Light" },
        { "Match System" }
    ),
    Units(
        { "d" },
        { "h" },
        { "m" },
        { "s" },
        { "mb" },
        { "px" },
        { "x" }
    ),
    Updater(
        { "Close" },
        { "Quit Launcher" },
        Updater.Status(
            { "The previous version was fully restored.\nDetails were written to the logs.\nPlease report this error." },
            { "The update failed." },
            { "The launcher can no longer be used.\nDetails were written to the logs.\nPlease report this error and resolve the error manually." },
            { "A fatal error occurred during the update." },
            { "The new version is applied." },
            { "The launcher was successfully updated." },
            { "The launcher can not be used while the updater is running.\nClose the launcher to continue the update." },
            { "The updater is still running." },
            { "Cleaning up temporary resources failed.\nThis will probably not affect usage.\nDetails were written to the logs." },
            { "The launcher was updated." }
        )
    )
)