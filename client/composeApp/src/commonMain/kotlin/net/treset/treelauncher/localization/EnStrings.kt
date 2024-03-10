package net.treset.treelauncher.localization

import net.treset.treelauncher.instances.InstanceDetails

class EnStrings : Strings(
    components = Components(
        create = { "Create New" },
        details = Components.Details (
            title = { "No Component selected" }
        )
    ),
    comboBox = ComboBox(
        loading = { "Loading..." },
        search = { "Search" }
    ),
    creator = Creator(
        buttonCreate = { "Create" },
        component = { "Component" },
        errorName = { "Name must not be empty" },
        errorSelect = { "No Component selected"},
        instance = Creator.Instance(
            instance = { "Instance" },
            mods = { "Mods" },
            popup = Creator.Instance.Popup(
                back = { "Close" },
                backToInstances = { "Back to Instances" },
                creating = { "Creating Instance..." },
                failure = { "Instance Creation failed.\nPlease report this!" },
                success = { "Instance successfully created" },
                undefined = { "Unknown instance creation status.\nPlease report this!" }
            ),
            resourcepacks = { "Resourcepacks" },
            saves = { "Saves" },
            title = { "Create Instance" },
            options = { "Options" },
            version = { "Version" },
        ),
        mods = Creator.Mods(
            quiltIncludeFabric = { "Include Fabric Mods" },
            type = { "Mod Loader" },
            version = { "Version" },
        ),
        name = { "Name" },
        radioCreate = { "Create" },
        radioUse = { "Use existing component" },
        radioInherit = { "Copy existing component" },
        status = Creator.Status(
            starting =  { "Preparing creation..." },
            mods = { "Creating mods component..." },
            options = { "Creating options component..." },
            resourcepacks = { "Creating resourcepacks component..." },
            saves = { "Creating saves component..." },
            version = Creator.Status.Version(
                assets = { "Downloading assets..." },
                fabric = { "Creating fabric version..." },
                fabricFile = { "Downloading fabric version..." },
                fabricLibraries = { "Downloading fabric libraries..." },
                file = { "Downloading version..." },
                forge = { "Creating forge version..." },
                forgeFile = { "Patching forge version..." },
                forgeLibraries = { "Downloading forge libraries..." },
                libraries = { "Downloading libraries..." },
                quilt = { "Creating quilt version..." },
                quiltLibraries = { "Downloading quilt libraries..." },
                value = { "Creating version..." },
                vanilla = { "Creating minecraft version..." }
            ),
            java = { "Downloading java version..." },
            finishing = { "Finishing creation..." }
        ),
        version = Creator.Version(
            errorVersion = { "No Version selected" },
            errorType = { "No Version Type selected" },
            errorLoader = { "No Fabric Version selected" },
            fabric = { "Fabric version" },
            forge = { "Forge version" },
            showSnapshots = { "Show Snapshots" },
            loading = { "Loading..." },
            quilt = { "Quilt version" },
            type = { "Version type" },
            version = { "Version" }
        )
    ),
    changer = Changer(
        apply = { "Apply Change" }
    ),
    error = Error(
        close = { "Acknowledge" },
        message = { error -> "Error:\n${error.message ?: "Unknown Error"}\nPlease report this error." },
        title = { "An error occurred!" },
        severeClose = { "Close launcher" },
        severeMessage = { error -> "Error:\n${error.message ?: "Unknown Error"}\nPlease report this error." },
        severeTitle = { "A severe error occurred!" },
        unknown = { "Unknown error" }
    ),
    fixFiles = FixFiles(
        cancel = { "Don't attempt" },
        close = { "Close" },
        confirm = { "Attempt to restore launcher" },
        failureMessage = { "Trying again probably won't help here.\nTry to fix the files manually or contact the developer." },
        failureTitle = { "Failed to restore launcher!" },
        message = { "The launcher is not usable in this state.\nThis may be caused by an unexpected close while the game was running.\n\nAttempt to fix these files automatically?\nNon-world data from your last playing session may be lost.\n\nDO NOT START THIS WHILE A INSTANCE OF THE GAME IS RUNNING!" },
        runningMessage = { "This may take a while." },
        runningTitle = { "Attempting to restore launcher..." },
        successMessage = { "The launcher should be usable again." },
        successTitle = { "Launcher successfully restored!" },
        title = { "Inconsistent launcher files detected!" }
    ),
    game = Game(),
    language = Language(
        systemLanguage = { "system language" }
    ),
    launcher = Launcher(
        status = Launcher.Status(
            preparing = { progress -> "Performing first time setup... $progress%" },
            restartRequired = { "Restart required. Please restart." }
        )
    ),
    login = Login(
        browserTitle = { state -> "Login: ${state.pageTitle ?: "Loading..."} (${state.lastLoadedUrl ?: "Getting url..."})" },
        button = { "Login with Microsoft" },
        label = Login.Label(
            authenticating = { "Logging you in..." },
            failure = { "Login failed. Please try again!" },
            success = { user -> "Welcome, ${user ?: "Anonymous User"}!" }
        ),
        logout = { "Delete Saved Login Data" },
        keepLoggedIn = { "Stay logged in" }
    ),
    manager = Manager(
        component = Manager.Component(
            addFile = { "Add Included File" },
            back = { "Back" },
            deleteFile = { "Remove Included File" },
            file = { "File" },
            fileName = { "Enter Filename" },
            folder = { "Folder" },
            import = Manager.Component.Import(
                back = { "Back" },
                tooltipExpand = { expanded -> if(expanded) "Collapse" else "Expand" },
            ),
            includedFiles = { "Included Files:" },
            settings = { "Component Settings" }
        ),
        instance = Manager.Instance(
            change = Manager.Instance.Change(
                back = { "Close" },
                cancel = { "Cancel" },
                changing = { "Changing Version..." },
                confirm = { "I know what I'm doing, Change" },
                failure = { "There was an error changing version.\nPlease report this." },
                message = { "This is likely to cause incompatibilities.\nIt is recommended to change versions by creating a new instance." },
                noComponent = { "No Component" },
                success = { "Version Changed!" },
                title = { "You are about to change the version of this Instance!" },
                activeTitle = { type, name ->
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
            details = Manager.Instance.Details(
                version = { "Version" },
                saves = { "Saves" },
                resourcepacks = { "Resourcepacks" },
                options = { "Options" },
                mods = { "Mods" },
                settings = { "Settings" }
            ),
            settings = Manager.Instance.Settings(
                addArgument = { "Add Argument" },
                argumentPlaceholder = { "Enter new Argument" },
                arguments = { "JVM-Arguments" },
                deleteArgument = { "Remove Argument" },
                memory = { "Instance Memory:" },
                resolution = { "Resolution:" },
                title = { "Instance Settings" }
            ),
        ),
        mods = Manager.Mods(
            add = { "Add Mod" },
            addMods = Manager.Mods.Add(
                addLocal = { "Add mod manually" },
                back = { "Back" },
                search = { "Search Online for a Mod" },
                searchTooltip = { "Search" },
                loading = { "Searching Mods..."},
                noResults = { "No appropriate Mods found." }
            ),
            card = Manager.Mods.Card(
                changeUsed = { enabled -> if(enabled) "Disable Mod" else "Enable Mod" },
                delete = { "Delete Mod" },
                download = { "Download Version" },
                edit = { "Edit Mod" },
                openBrowser = { "Open in Browser" },
                versionPlaceholder = { "Select a version" }
            ),
            change = Manager.Mods.Change(
                title = { "You are about to change the game version associated with this component!" },
                message = { "This is likely to break compatibility with your instances.\nIt is generally only recommended to change version immediately after creating the component." },
                confirm = { "I know what I'm doing, Change" },
                cancel = { "Cancel" }
            ),
            changeVersion = { "Game Version:" },
            current = { "Current Mods" },
            edit = Manager.Mods.Edit(
                cancel = { "Cancel" },
                confirm = { current -> current?.let{ "Apply" } ?: "Add" },
                curseforge = { "Curseforge Project ID" },
                curseforgeError = { "Invalid Project ID" },
                file = { "File" },
                fileError = { "No file selected" },
                modrinth = { "Modrinth Project ID" },
                modrinthError = { "Invalid Project ID" },
                name = { "Name" },
                version = { "Version" },
                versionError = { "No version selected" }
            ),
            empty = { "No mods Found" },
            import = Manager.Mods.ImportStrings(
                delete = { "Unselect Mod" },
                displayName = { mod -> "${mod.name} v${mod.version}" },
                import = { "Import Mods" },
                importComponent = { "Select mods from other Components:" },
                importFile = { "Copy local mod files:" },
                importing = { "Importing Mods..." },
                selectedFiles = { "Selected Mods:" },
                tooltipAdd = { "Add Mod" },
                tooltipFile = { "Select Mod" },
            ),
            searchPlaceholder = { "Search for a Mod" },
            update = Manager.Mods.Update(
                auto = { "Automatically Update" },
                disable = { "Disable Mods without appropriate version" },
                enable = { "Enable disabled Mods"},
                tooltip = { "Check for Updates" }
            ),
            version = { "Game Version" }
        ),
        resourcepacks = Manager.Resourcepacks(
            delete = { "Delete Resourcepack" },
            deleteTitle = { "You are about to delete this resourcepack!"},
            deleteMessage = { "This action cannot be undone!" },
            deleteConfirm = { "Yes, delete" },
            deleteCancel = { "Cancel" },
            import = Manager.Component.ImportStrings(
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
                unknownMessage = { "This file doesn't have any resourcepack format the launcher is aware of.\nDo you want to add it anyways?" },
                unknownTitle = { "Unknown Resourcepack Format" }
            ),
            tooltipAdd = { "Add Resourcepack" },
        ),
        saves = Manager.Saves(
            delete = { "Delete World" },
            deleteTitle = { world -> "You are about to delete the world \"${world.name}\"!"},
            deleteMessage = { "This action cannot be undone!\nAny data in this world will be lost forever." },
            deleteConfirm = { world -> "Yes, delete the world \"${world.name}\" forever" },
            deleteCancel = { "Cancel" },
            import = Manager.Component.ImportStrings(
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
                unknownMessage = { "This folder doesn't have any world format the launcher is aware of.\nDo you want to add it anyways?" },
                unknownTitle = { "Unknown World Format" }
            ),
            tooltipAdd = { "Add World" },
        ),
    ),
    nav = Nav(
        add = { "Create Instance" },
        home = { "Instances" },
        mods = { "Mods Components" },
        options = { "Options Components" },
        resourcepacks = { "Resourcepacks Components" },
        saves = { "Saves Components" },
        settings = { "Settings" }
    ),
    news = News(
        close = { "Close" },
        important = { "Important News:" },
        loading = { "Loading News..." },
        none = { "No current News" },
        other = { "News:"},
        tooltip = { "Open News" },
        title = { "News" }
    ),
    selector = Selector(
        component = Selector.Component(
            delete = Selector.Component.Delete(
                cancel = { "Cancel" },
                confirm = { "Delete" },
                message = { "This action cannot be undone!\nAny data in this Component will be lost forever.\nThis Component is not currently used by any instance." },
                title = { "You are about to delete this Component!" },
                tooltip = { "Delete Component" },
                unableClose = { "Close" },
                unableMessage = { instance -> "It is used by the following instance: ${instance.name}" },
                unableTitle = { "Unable to delete this component!" },
            ),
            rename = Selector.Component.Rename(
                cancel = { "Cancel" },
                confirm = { "Save" },
                error = { "Not a valid name" },
                prompt = { "New Name" },
                title = { "Rename Component" }
            ),
            openFolder = { "Open in File Explorer" }
        ),
        instance = Selector.Instance(
            delete = Selector.Instance.Delete(
                cancel = { "Cancel" },
                confirm = { "Delete" },
                message = { "This cannot be undone.\nAll used components will still exist after deletion." },
                title = { "You are about to delete this Instance!" },
                tooltip = { "Delete Instance" }
            ),
            empty = { "Click the" to "at the bottom to create one." },
            emptyTitle= { "No Instances created yet." },
            game = Selector.Instance.Game(
                errorMessage = { message -> "Error:\n$message\nPlease report this error."},
                errorTitle = { "Game Launch Failed!" },
                preparingMessage = { "The Game will start shortly." },
                preparingTitle = { "Preparing Game Resources..." },
                runningMessage = { "Close the game to be able to perform actions in the launcher." },
                runningTitle = { "The Game is running..." },
                crashClose = { "Close" },
                crashMessage = { message -> "Error:\n$message\nThis might be unrelated to the launcher." },
                crashReports = { "Open crash reports" },
                crashTitle = { "The Game exited unexpectedly" }
            ),
            mods = { "Mods Component" },
            options = { "Options Component" },
            play = { "Start Instance" },
            resourcepacks = { "Resourcepacks Component" },
            saves = { "Saves Component" },
            title = { "Instances" },
            version = { "Version" }
        ),
        mods = Selector.Mods(
            content = Selector.Mods.Content(
                delete = { "Delete Mod" },
                disable = { "Disable Mod" },
                enable = { "Enable Mod" },
                install = { "Install Version" },
                open = { "Open in Browser" }
            ),
            title = { "Mods" }
        ),
        options = Selector.Options(
            title = { "Options" }
        ),
        resourcepacks = Selector.Resourcepacks(
            title = { "Resourcepacks" }
        ),
        saves = Selector.Saves(
            play = Selector.Saves.Play(
                button = { "Start World" },
                multipleClose = { "Cancel" },
                multipleMessage = { "Which instance should this world be launched in?" },
                multiplePlay = { "Start World" },
                multipleTitle = { "Multiple instances are using this component." },
                noClose = { "Close" },
                noMessage = { "Quick Play is only available if the world is in a used component." },
                noTitle = { "No instance is using this component." }
            ),
            servers = { "Servers:" },
            title = { "Saves" },
            worlds = { "Worlds:"}
        )
    ),
    settings = Settings(
        accentColor = { "Accent Color:" },
        appearance = { "Appearance" },
        cleanup = Settings.Cleanup(
            button = { "Delete unused Files" },
            cancel = { "Cancel" },
            close = { "Close" },
            confirm = { "Delete Files" },
            deleting = { "Deleting Files..." },
            failureMessage = { "The files could not be deleted.\nThis will probably not affect launcher functionality.\nDetails were written to the logs." },
            failureTitle = { "Failed to delete unused files!" },
            libraries = { "Also delete unused Libraries" },
            message = { "This will delete all version files that are not used by any Instance to free up space.\nVersions can be reinstalled at any time." },
            success = { "Unused files have been deleted." },
            title = { "Delete unused Files" }
        ),
        language = { "Language:" },
        logout = { "Logout" },
        path = Settings.Path(
            apply = { "Apply" },
            changing = { "Changing path..." },
            close = { "Close" },
            errorTitle = { "Failed to change path" },
            errorMessage = { e -> "An error occurred:\n$e"},
            invalid = { "No valid folder provided" },
            remove = { "Remove files from old location" },
            select = { "Select a Folder" },
            success = { "Successfully changed path" },
            title = { "Launcher Data Directory"}
        ),
        source = { "Source Repository" },
        sourceTooltip = { "Open Source Repository" },
        sync = Settings.Sync(
            close = { "Close" },
            failure = { "Test failed!" },
            title = { "Component Synchronization" },
            key = { "API-Key:"},
            keyPlaceholder = { "Key" },
            port = { "Port" },
            success = { "Test successfully!" },
            test = { "Test" },
            url = { "URL" }
        ),
        title = { "Settings" },
        theme = { "Theme:" },
        update = Settings.Update(
            available = { "Update Available!" },
            availableMessage = { new, message ->  "Update: v${strings().launcher.version()} → v$new ${message?.let {"\n\n$it"}}"},
            availableTitle = { "Update Available!" },
            cancel = { "Cancel" },
            checkingTitle = { "Checking for updates..." },
            close = { "Close" },
            download = { "Download" },
            downloadingMessage = { file, current, total -> "$file ($current/$total)" },
            downloadingTitle = { "Downloading Update..." },
            latestMessage = { "Current Version: v${strings().launcher.version()}" },
            latestTitle = { "Everything is up to date!" },
            successMessage = { "Restart the launcher for these changes to take effect." },
            successRestart = { "Restart Now" },
            successTitle = { "Update successfully downloaded." },
            tooltip = { "Check for updates" },
            unavailableMessage = { "An update is available, but this version of the launcher can not automatically update to it.\nCheck online how to manually update." },
            unavailableTitle = { "Not able to update." }
        ),
        user = { "Logged in as:" },
        version = { "Version: v${strings().launcher.version()}" }
    ),
    sortBox = SortBox(
        sort = SortBox.Sort(
            enabledName = { "Name (Enabled first)" },
            lastPlayed = { "Last Played" },
            lastUsed = { "Last Used" },
            name = { "Name" },
            time = { "Time Played" }
        ),
        reverse = { "Reverse" }
    ),
    sync = Sync(
        complete = { "Successfully synchronized Component" },
        completeClose = { "Close" },
        download = Sync.Download(
            cancel = { "Cancel" },
            confirm = { "Download" },
            message = { "Select a component to download" },
            noneClose = { "Close" },
            noneTitle = { "No new Components available" },
            title = { "Download Component" }
        ),
        status = Sync.Status(
            collecting = { "Collecting Synchronization Data..." },
            creating = { "Downloading required version..." },
            downloading = { "Downloading Files..." },
            finished = { "Synchronisation has finished." },
            starting = { "Getting started..." },
            uploading = { "Uploading Files..." }
        ),
        syncing = { "Synchronizing Component..."},
        unknown = { "<Unknown Name>" }
    ),
    theme = Theme(
        dark = { "Dark" },
        light = { "Light" },
        system = { "Match System" },
        green = { "Green" },
        blue = { "Blue" },
        red = { "Red" },
        cyan = { "Cyan" },
        magenta = { "Magenta" },
        orange = { "Orange" }
    ),
    units = Units(
        days = { "d" },
        hours = { "h" },
        minutes = { "m" },
        seconds = { "s" },
        megabytes = { "mb" },
        pixels = { "px" },
        resolutionBy = { "x" }
    ),
    updater = Updater(
        close = { "Close" },
        quit = { "Quit Launcher" },
        status = Updater.Status(
            failureMessage = { "The previous version was fully restored.\nDetails were written to the logs.\nPlease report this error." },
            failureTitle = { "The update failed." },
            fatalMessage = { "The launcher can no longer be used.\nDetails were written to the logs.\nPlease report this error and resolve the error manually." },
            fatalTitle = { "A fatal error occurred during the update." },
            successMessage = { "The new version is applied." },
            successTitle = { "The launcher was successfully updated." },
            updatingMessage = { "The launcher can not be used while the updater is running.\nClose the launcher to continue the update." },
            updatingTitle = { "The updater is still running." },
            warningMessage = { "Cleaning up temporary resources failed.\nThis will probably not affect usage.\nDetails were written to the logs." },
            warningTitle = { "The launcher was updated." }
        )
    ),
    version = Version()
)