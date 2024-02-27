package net.treset.treelauncher.localization

import net.treset.treelauncher.instances.InstanceDetails

class DeStrings : Strings(
    components = Components(
        create = { "Neu erstellen" },
        details = Components.Details (
            title = { "Keine Komponente ausgewählt" }
        )
    ),
    comboBox = ComboBox(
        loading = { "Laden..." },
        search = { "Suchen" }
    ),
    creator = Creator(
        buttonCreate = { "Erstellen" },
        component = { "Komponente" },
        errorName = { "Name darf nicht leer sein." },
        errorSelect = { "Keine Komponente ausgewählt."},
        instance = Creator.Instance(
            instance = { "Instanz" },
            mods = { "Mods" },
            popup = Creator.Instance.Popup(
                back = { "Schließen" },
                backToInstances = { "Zurück zu Instanzen" },
                creating = { "Instanz wird erstellt..." },
                failure = { "Erstellung fehlgeschlagen.\nDiesen Fehler bitte melden!" },
                success = { "Erstellung abgeschlossen" },
                undefined = { "Unbekannter Erstellungsstatus.\nDiesen Fehler bitte melden!" }
            ),
            resourcepacks = { "Ressourcenpakete" },
            saves = { "Welten" },
            title = { "Instanz erstellen" },
            options = { "Optionen" },
            version = { "Version" },
        ),
        mods = Creator.Mods (
            version = { "Version" },
            type = { "Mod-Loader"},
        ),
        name = { "Name" },
        radioCreate = { "Erstellen" },
        radioUse = { "Vorhandene Komponente verwenden" },
        radioInherit = { "Vorhandene Komponente kopieren" },
        status = Creator.Status(
            starting = { "Erstellung wird vorbereitet..." },
            mods = { "Mods Komponente wird erstellt..." },
            options = { "Optionskomponente wird erstellt..." },
            resourcepacks = { "Ressoucenpaketkomponente wird erstellt..." },
            saves = { "Weltenkomponente wird erstellt..." },
            version = Creator.Status.Version(
                assets = { "Assets werden heruntergeladen..." },
                fabric = { "Fabric Version wird erstellt..." },
                fabricFile = { "Fabric Version wird heruntergeladen..." },
                fabricLibraries = { "Fabric Bibliotheken werden heruntergeladen..." },
                file = { "Version wird heruntergeladen..." },
                forge = { "Forge Version wird erstellt..." },
                forgeFile = { "Forge Version wird heruntergeladen..." },
                forgeLibraries = { "Forge Bibliotheken werden heruntergeladen..." },
                libraries = { "Bibliotheken werden heruntergeladen..." },
                value = { "Version wird erstellt..." },
                vanilla = { "Minecraft Version wird erstellt..." }
            ),
            java = { "Java Version wird heruntergeladen..." },
            finishing = { "Erstellung wird abgeschlossen..." }
        ),
        version = Creator.Version(
            errorVersion = { "Keine Version ausgewählt." },
            errorType = { "Kein Versionstyp ausgewählt." },
            errorLoader = { "Keine Fabric Version ausgewählt." },
            fabric = { "Fabric Version" },
            forge = { "Forge Version" },
            showSnapshots = { "Snapshots anzeigen" },
            loading = { "Laden..." },
            type = { "Versionstyp" },
            version = { "Version" }
        )
    ),
    changer = Changer(
        apply = { "Anwenden" }
    ),
    error = Error(
        close = { "Akzeptieren" },
        message = { error -> "Fehler:\n${error.message ?: "Unbekannter Fehler"}\nDiesen Fehler bitte melden." },
        title = { "Ein Fehler ist aufgetreten!" },
        severeClose = { "Launcher schließen" },
        severeMessage = { error -> "Fehler:\n${error.message ?: "Unbekannter Fehler"}\nDiesen Fehler bitte melden." },
        severeTitle = { "Ein kritischen Fehler ist aufgetreten!" },
        unknown = { "Unbekannter Fehler" }
    ),
    fixFiles = FixFiles(
        cancel = { "Nicht versuchen" },
        close = { "Schließen" },
        confirm = { "Versuchen, den Launcher wiederherzustellen" },
        failureMessage = { "Ein erneuter Versuch wird wahrscheinlich nicht helfen.\nBitte versuchen, die Dateien manuell zu reparieren oder den Entwickler zu kontaktieren." },
        failureTitle = { "Wiederherstellung des Launchers fehlgeschlagen!" },
        message = { "Der Launcher ist in diesem Zustand nicht nutzbar.\nDies kann durch ein unerwartetes Schließen während das Spiel lief verursacht worden sein.\n\nVersuchen, die Dateien automatisch zu reparieren?\nNicht-Weltdaten aus Ihrer letzten Spielsitzung können hierbei verloren gehen.\n\nDIESEN VORGANG NUR STARTEN, WENN KEINE SPIELINSTANZ LÄUFT!" },
        runningMessage = { "Dies könnte eine Weile dauern." },
        runningTitle = { "Versuchen, den Launcher wiederherzustellen..." },
        successMessage = { "Der Launcher sollte wieder nutzbar sein." },
        successTitle = { "Launcher erfolgreich wiederhergestellt!" },
        title = { "Inkonsistente Launcher-Dateien erkannt!" }
    ),
    game = Game(),
    language = Language(
        systemLanguage = { "Systemsprache" }
    ),
    launcher = Launcher(
        status = Launcher.Status(
            preparing = { progress -> "Einmaliges Setup wird durchgeführt... $progress%" },
            restartRequired = { "Neustart erforderlich. Bitte neu starten." }
        )
    ),
    login = Login(
        browserTitle = { state -> "Einloggen: ${state.pageTitle ?: "Wird geladen..."} (${state.lastLoadedUrl ?: "URL wird abgerufen..."})" },
        button = { "Login mit Microsoft" },
        label = Login.Label(
            authenticating = { "Einloggen..." },
            failure = { "Login fehlgeschlagen. Bitte erneut versuchen!" },
            success = { user -> "Willkommen, ${user ?: "Anonymer Nutzer"}!" }
        ),
        logout = { "Gespeichertes Logindaten löschen" },
        keepLoggedIn = { "Eingeloggt bleiben" }
    ),
    manager = Manager(
        component = Manager.Component(
            addFile = { "Einbegriffene Datei hinzufügen" },
            back = { "Zurück" },
            deleteFile = { "Einbegriffene Datei entfernen" },
            file = { "Datei" },
            fileName = { "Dateiname eingeben" },
            folder = { "Verzeichnis" },
            import = Manager.Component.Import(
                back = { "Zurück" },
                tooltipExpand = { expanded -> if(expanded) "Zuklappen" else "Aufklappen" },
            ),
            includedFiles = { "Einbegriffene Dateien:" },
            settings = { "Komponenteneinstellungen" }
        ),
        instance = Manager.Instance(
            change = Manager.Instance.Change(
                back = { "Schließen" },
                cancel = { "Abbrechen" },
                changing = { "Version wird geändert..." },
                confirm = { "Ja, Version ändern" },
                failure = { "Ein Fehler beim Ändern der Version ist aufgetreten.\nDiesen Fehler bitte melden." },
                message = { "Kompatibilität kann nicht garantiert werden.\nEs ist empfohlen, die Version durch Erstellen einer neuen Instanz mit gleichen Komponenten zu ändern."},
                noComponent = { "Keine Komponente" },
                success = { "Version geändert!" },
                title = { "Die Version dieser Instanz wirklich ändern?" },
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
                saves = { "Welten" },
                resourcepacks = { "Ressourcenpakete" },
                options = { "Optionen" },
                mods = { "Mods" },
                settings = { "Einstellungen" }
            ),
            settings = Manager.Instance.Settings(
                addArgument = { "Argument hinzufügen" },
                argumentPlaceholder = { "Neues Argument eingeben" },
                arguments = { "JVM-Argumente" },
                deleteArgument = { "Argument entfernen" },
                memory = { "Instanzspeicher:" },
                resolution = { "Auflösung:" },
                title = { "Instanzeinstellungen" },
            )
        ),
        mods = Manager.Mods(
            add = { "Lokale mod hinzufügen" },
            addMods = Manager.Mods.Add(
                addLocal = { "Mod manuell hinzufügen" },
                back = { "Zurück" },
                search = { "Online nach einer Mod suchen" },
                searchTooltip = { "Suchen" },
                loading = { "Mods werden gesucht..."},
                noResults = { "Keine Mod mit passender Version gefunden." }
            ),
            card = Manager.Mods.Card(
                changeUsed = { enabled -> if(enabled) "Mod deaktivieren" else "Mod aktivieren" },
                delete = { "Mod Löschen" },
                download = { "Version herunterladen" },
                edit = { "Mod bearbeiten" },
                openBrowser = { "Im Browser öffnen" },
                versionPlaceholder = { "Eine Version auswählen" }
            ),
            change = Manager.Mods.Change(
                title = { "Die Spielversion dieser Komponente wirklich wechseln?" },
                message = { "Dadurch kann Kompatibilität mit den aktuellen Instanzen nicht gewährleistet werden.\n\nVersionswechsel sollten nur unmittelbar nach erstellen der Komponente durchgeführt werden." },
                confirm = { "Ja, Version ändern" },
                cancel = { "Abbrechen" }
            ),
            changeVersion = { "Spielversion:" },
            current = { "Aktuelle Mods" },
            edit = Manager.Mods.Edit(
                cancel = { "Abbrechen" },
                confirm = { current -> current?.let{ "Anwenden" } ?: "Hinzufügen" },
                curseforge = { "Curseforge Projekt ID" },
                curseforgeError = { "Ungültige Projekt ID" },
                file = { "Datei" },
                fileError = { "Keine Datei ausgewählt" },
                modrinth = { "Modrinth Projekt ID" },
                modrinthError = { "Ungültige Projekt ID" },
                name = { "Name" },
                version = { "Version" },
                versionError = { "Keine Version ausgewählt" }
            ),
            empty = { "Keine Mods gefunden" },
            import = Manager.Mods.ImportStrings(
                delete = { "Mod entfernen" },
                displayName = { mod -> "${mod.name} v${mod.version}"},
                import = { "Mods importieren" },
                importComponent = { "Mods aus anderen Komponenten auswählen:" },
                importFile = { "Lokale Moddateien kopieren:" },
                importing = { "Mods werden importiert..." },
                selectedFiles = { "Ausgewählte Mods:" },
                tooltipAdd = { "Hinzufügen" },
                tooltipFile = { "Moddatei auswählen" }
            ),
            searchPlaceholder = { "Nach einer Mod suchen" },
            update = Manager.Mods.Update(
                auto = { "Automatisch aktualisieren" },
                disable = { "Mods ohne passende Version deaktivieren" },
                enable = { "Deaktivierte Mods aktivieren"},
                tooltip = { "Nach Updates suchen" }
            ),
            version = { "Spielversion" }
        ),
        resourcepacks = Manager.Resourcepacks(
            delete = { "Ressourcenpaket löschen" },
            deleteTitle = { "Die Ressourcenpaket wirklich löschen?" },
            deleteMessage = { "Diese Aktion kann nicht rückgängig gemacht werden." },
            deleteConfirm = { "Ja, löschen" },
            deleteCancel = { "Abbrechen" },
            import = Manager.Component.ImportStrings(
                delete = { "Ressourcenpaket entfernen" },
                import = { "Ressourcenpakete importieren" },
                importComponent = { "Ressourcenpakete aus anderen Komponenten auswählen:" },
                importFile = { "Lokale Ressourcenpakete kopieren:" },
                importing = { "Ressourcenpakete werden importiert..." },
                selectedFiles = { "Ausgewählte Ressourcenpakete:" },
                tooltipAdd = { "Hinzufügen" },
                tooltipFile = { "Ressourcenpaket auswählen" }
            ),
            tooltipAdd = { "Ressourcenpaket hinzufügen" },
        ),
        saves = Manager.Saves(
            delete = { "Welt löschen" },
            deleteTitle = { world -> "Die Welt \"${world.name}\" wirklich löschen?" },
            deleteMessage = { "Diese Aktion kann nicht rückgängig gemacht werden.\nAlle Daten dieser Welt werden unwiederruflich gelöscht." },
            deleteConfirm = { world -> "Ja, die Welt \"${world.name}\" permanent löschen" },
            deleteCancel = { "Abbrechen" },
            import = Manager.Component.ImportStrings(
                delete = { "Welt entfernen" },
                import = { "Welten importieren" },
                importComponent = { "Welten aus anderen Komponenten auswählen:" },
                importFile = { "Lokale Weltendateien kopieren:" },
                importing = { "Welten werden importiert..." },
                selectedFiles = { "Ausgewählte Welten:" },
                tooltipAdd = { "Hinzufügen" },
                tooltipFile = { "Weltendatei auswählen" }
            ),
            tooltipAdd = { "Welt hinzufügen" },
        ),
    ),
    nav = Nav(
        add = { "Instanz erstellen" },
        home = { "Instanzen" },
        mods = { "Mods Komponenten" },
        options = { "Optionskomponenten" },
        resourcepacks = { "Ressourcenpaketkomponenten" },
        saves = { "Weltenkomponenten" },
        settings = { "Einstellungen" }
    ),
    news = News(
        close = { "Schließen" },
        important = { "Wichtige Neuigkeiten:" },
        loading = { "Neuigkeiten werden geladen..." },
        none = { "Keine aktuellen Neuigkeiten" },
        other = { "Neuigkeiten:" },
        tooltip = { "Neuigkeiten anzeigen" },
        title = { "Neuigkeiten" }
    ),
    selector = Selector(
        component = Selector.Component(
            delete = Selector.Component.Delete(
                cancel = { "Abbrechen" },
                confirm = { "Löschen" },
                message = { "Diese Aktion kann nicht rückgängig gemacht werden!\nDaten in dieser Komponente werden unwiderruflich gelöscht.\nDiese Komponente wird von keiner Instanz verwendet." },
                title = { "Diese Komponente wirklich löschen?" },
                tooltip = { "Komponente löschen" },
                unableClose = { "Schließen" },
                unableMessage = { instance -> "Sie wird von folgender Instanz verwendet: ${instance.name}." },
                unableTitle = { "Diese Komponente kann nicht gelöscht werden!" },
            ),
            rename = Selector.Component.Rename(
                cancel = { "Abbrechen" },
                confirm = { "Speichern" },
                error = { "Kein neuer Name eingegeben" },
                prompt = { "Neuer Name" },
                title = { "Komponente umbenennen" }
            ),
            openFolder = { "In Dateien öffnen" }
        ),
        instance = Selector.Instance(
            delete = Selector.Instance.Delete(
                cancel = { "Abbrechen" },
                confirm = { "Löschen" },
                message = { "Diese Aktion kann nicht rückgängig gemacht werden.\nAlle verwendeten Komponenten bleiben bestehen." },
                title = { "Diese Instanz wirklich löschen?" },
                tooltip = { "Instanz löschen" }
            ),
            empty = { "Auf den" to "Knopf unten klicken, um Instanzen zu erstellen." },
            emptyTitle = { "Noch keine Instanzen vorhanden." },
            game = Selector.Instance.Game(
                errorMessage = { message -> "Fehler:\n$message\nDiesen Fehler bitte melden."},
                errorTitle ={ "Spielstart fehlgeschlagen!" },
                preparingMessage = { "Das Spiel starten in Kürze." },
                preparingTitle ={ "Spielressourcen werden vorbereitet..." },
                runningMessage ={ "Das Spiel schließen, um Aktionen im Launcher durchzuführen." },
                runningTitle = { "Das Spiel läuft..." },
                crashClose = { "Schließen" },
                crashMessage = { message -> "Fehler:\n$message\nDies wurde vermutlich nicht durch den Launcher verursacht." },
                crashReports = { "Absturzberichte öffnen" },
                crashTitle = { "Das Spiel wurde unerwartet geschlossen" }
            ),
            mods = { "Mods Komponente" },
            options = { "Optionskomponente" },
            play = { "Instanz starten" },
            resourcepacks = { "Ressourcenpaketkomponente" },
            saves = { "Weltenkomponente" },
            title = { "Instanzen" },
            version = { "Version" }
        ),
        mods = Selector.Mods(
            content = Selector.Mods.Content(
                delete = { "Mod löschen" },
                disable = { "Mod deaktivieren" },
                enable = { "Mod aktivieren" },
                install = { "Version installieren" },
                open = { "Im Browser öffnen" }
            ),
            title = { "Mods" }
        ),
        options = Selector.Options(
            title = { "Optionen" }
        ),
        resourcepacks = Selector.Resourcepacks(
            title = { "Ressourcenpakete" }
        ),
        saves = Selector.Saves(
            play = Selector.Saves.Play(
                button = { "Welt starten" },
                multipleClose = { "Abbrechen" },
                multipleMessage = { "In welcher Instanz soll diese Welt gestartet werden?" },
                multiplePlay = { "Welt starten" },
                multipleTitle = { "Mehrere Instanzen nutzen diese Komponente." },
                noClose = { "Schließen" },
                noMessage = { "Schnellstart ist nur für Welten verfügbar, die von einer Instanz genutzt werden." },
                noTitle = { "Keine Instanz nutzt diese Komponente." }
            ),
            servers = { "Server:" },
            title = { "Welten" },
            worlds = { "Welten:"}
        )
    ),
    settings = Settings(
        appearance = { "Darstellung" },
        cleanup = Settings.Cleanup(
            button = { "Ungenutzte Dateien löschen" },
            cancel = { "Abbrechen" },
            close = { "Schließen" },
            confirm = { "Dateien Löschen" },
            deleting = { "Dateien werden gelöscht..." },
            failureMessage = { "Nicht alle Dateien konnten gelöscht werden.\nDies beeinflusst die funtkionalität des Launchers wahrscheinlich nicht.\nDetails wurden in die Logs geschrieben." },
            failureTitle = { "Löschen fehlgeschlagen." },
            libraries = { "Auch ungenutzte Bibliotheken löschen" },
            message = { "Alle versionsdateien, die von keiner Instanz genutzt werden, werden gelöscht um Speicherplatz zu sparen.\n Versionen können jederzeit wieder installiert werden." },
            success = { "Ungenutzte Dateien gelöscht." },
            title = { "Ungenutzte Dateien löschen?" }
        ),
        language = { "Sprache:" },
        logout = { "Ausloggen" },
        path = Settings.Path(
            apply = { "Anwenden" },
            changing = { "Dateipfad wird geändert..." },
            close = { "Schließen" },
            errorTitle = { "Dateipfad konnte nicht geändert werden" },
            errorMessage = { e -> "Der folgende Fehler ist aufgetreten:\n$e" },
            invalid = { "Kein Ordner ausgewählt" },
            remove = { "Dateien vom alten Ort entfernen" },
            select = { "Ordner auswählen" },
            success = { "Dateipfad geändert" },
            title = { "Launcher Dateipfad"}
        ),
        restartRequired = { "Diese Einstellung wird nach dem Neustart angewendet" },
        source = { "Quellcode Repository" },
        sourceTooltip = { "Quellcode Repository öffnen" },
        sync = Settings.Sync(
            close = { "Schließen" },
            failure = { "Test fehlgeschlagen!" },
            title = { "Komponentensynchronisation" },
            key = { "API-Schlüssel:"},
            keyPlaceholder = { "Schlüssel" },
            port = { "Port" },
            success = { "Test erfolgreich!" },
            test = { "Test" },
            url = { "URL" }
        ),
        title = { "Einstellungen" },
        theme = { "Farben:" },
        update = Settings.Update(
            available = { "Update verfügbar!" },
            availableMessage = { new, message ->  "Update: v${strings().launcher.version()} → v$new ${message?.let {"\n\n$it"}}"},
            availableTitle = { "Update verfügbar!" },
            cancel = { "Abbrechen" },
            checkingTitle = { "Es wird nach Updates gesucht..." },
            close = { "Schließen" },
            download = { "Herunterladen" },
            downloadingMessage = { file, current, total -> "$file ($current/$total)" },
            downloadingTitle = { "Update wird heruntergeladen..." },
            latestMessage = { "Aktuelle Version: v${strings().launcher.version()}" },
            latestTitle = { "Der Launcher ist aktuell!" },
            successMessage = { "Der Launcher muss neugestartet werden um das Update anzuwenden." },
            successRestart = { "Jetzt Neustarten" },
            successTitle = { "Das Update wurde heruntergeladen." },
            tooltip = { "Nach Updates suchen" },
            unavailableMessage = { "Ein Update ist verfügbar, aber der Launcher kann nicht automatisch aktualisiert werden.\nBitte online nach manueller Update-Anleitung suchen." },
            unavailableTitle = { "Update kann nicht durchgeführt werden." }
        ),
        user = { "Eingeloggt als:" },
        version = { "Version: v${strings().launcher.version()}" }
    ),
    sortBox = SortBox(
        sort = SortBox.Sort(
            enabledName = { "Name (Aktivierte zuerst)" },
            lastPlayed = { "Zuletzt gespielt" },
            lastUsed = { "Zuletzt verwendet" },
            name = { "Name" },
            time = { "Spielzeit" }
        ),
        reverse = { "Umkehren" }
    ),
    sync = Sync(
        complete = { "Komponente erfolgreich synchronisiert" },
        completeClose = { "Schließen" },
        download = Sync.Download(
            cancel = { "Abbrechen" },
            confirm = { "Herunterladen" },
            message = { "Komponente zum Herunterladen auswählen" },
            noneClose = { "Schließen" },
            noneTitle = { "Keine neuen Komponenten verfügbar" },
            title = { "Komponente heruntergeladen" }
        ),
        status = Sync.Status(
            collecting = { "Synchronisationsdaten werden gesammelt..." },
            creating = { "Benötigte Version wird heruntergeladen..." },
            downloading = { "Dateien werden heruntergeladen..." },
            finished = { "Synchronisation abgeschlossen." },
            starting = { "Synchronisation wird gestartet..." },
            uploading = { "Dateien werden hochgeladen..." }
        ),
        syncing = { "Komponente wird synchronisiert..."},
        unknown = { "<Unbekannter Name>" }
    ),
    theme = Theme(
        dark = { "Dunkel" },
        light = { "Hell" },
        system = { "Systemeinstellung" }
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
        close = { "Schließen" },
        quit = { "Launcher Schließen" },
        status = Updater.Status(
            failureMessage = { "Die vorherige Version wurde wiederhergestellt.\nDetails wurden in die Logs geschrieben.\nDiesen Fehler bitte melden." },
            failureTitle = { "Update fehlgeschlagen." },
            fatalMessage = { "Der Launcher kann nicht weiter verwendet werden.\nDetails wurden in die Logs geschrieben.\nDiesen Fehler bitte melden und manuell beheben." },
            fatalTitle = { "Ein fataler Fehler ist während des Updates aufgetreten." },
            successMessage = { "Die neue Version wurde angewendet." },
            successTitle = { "Der Launcher wurde erfolgreich aktualisiert." },
            updatingMessage = { "Der Launcher kann nicht genutzt werden, während der Updater läuft.\nDen launcher schließen, um das Update fortzusetzen." },
            updatingTitle = { "Der Updater läuft noch." },
            warningMessage = { "Das Löschen temporärer Update Ressourcen ist fehlgeschlagen.\nDas verursacht wahrscheinlich keine Fehler.\nDetails wurden in die Logs geschrieben." },
            warningTitle = { "Der Launcher wurde aktualisiert." }
        )
    ),
    version = Version(
        fabric = { "Fabric" },
        forge = { "Forge" },
        vanilla = { "Vanilla" }
    ),
)