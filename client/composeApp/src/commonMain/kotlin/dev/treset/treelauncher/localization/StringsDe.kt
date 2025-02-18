package dev.treset.treelauncher.localization

import dev.treset.mcdl.auth.AuthenticationStep
import dev.treset.treelauncher.components.instances.InstanceDetailsType
import dev.treset.treelauncher.generic.VersionType

class StringsDe : StringsEn(
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
            includeAlternateLoader = {
                "${ 
                    when(it) {
                        VersionType.QUILT -> "Fabric"
                        VersionType.NEO_FORGE -> "Forge"
                        else -> "Alternative"
                    }
                } Mods nutzen"
            },
            type = { "Mod-Loader"},
            version = { "Version" },
        ),
        name = { "Name" },
        radioCreate = { "Erstellen" },
        radioUse = { "Vorhandene Komponente verwenden" },
        radioInherit = { "Vorhandene Komponente kopieren" },
        status = Creator.Status(
            starting = { "Erstellung wird vorbereitet..." },
            instance =  { "Instanz wird erstellt..." },
            message = Creator.Status.Message(
                inheritFiles = { "Dateien werden kopiert..." },
                vanillaVersion = { "Vanilla Version wird erstellt..." },
            ),
            mods = { "Mods Komponente wird erstellt..." },
            options = { "Einstellungskomponente wird erstellt..." },
            resourcepacks = { "Ressourcenpaket Komponente wird erstellt..." },
            saves = { "Welten Komponente wird erstellt..." },
            version = Creator.Status.Version(
                assets = { "Assets werden heruntergeladen..." },
                fabric = { "Fabric Version wird erstellt..." },
                fabricFile = { "Fabric Version wird heruntergeladen..." },
                fabricLibraries = { "Fabric Bibliotheken werden heruntergeladen..." },
                file = { "Version wird heruntergeladen..." },
                forge = { "Forge Version wird erstellt..." },
                forgeInstaller = { "Forge Installation wird ausgeführt..." },
                libraries = { "Bibliotheken werden heruntergeladen..." },
                neoForge = { "NeoForge Version wird erstellt..." },
                neoForgeInstaller = { "NeoForge Installation wird ausgeführt..." },
                quilt = { "Quilt Version wird erstellt..." },
                quiltLibraries = { "Quilt Bibliotheken werden heruntergeladen..." },
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
            neoForge = { "NeoForge Version" },
            quilt = { "Quilt Version" },
            type = { "Versionstyp" },
            version = { "Version" }
        )
    ),
    changer = Changer(
        apply = { "Anwenden" }
    ),
    error = Error(
        close = { "Akzeptieren" },
        notification = { error -> "Ein Fehler ist aufgetreten: ${error.message ?: "Unbekannter Fehler"}. Hier klicken zum schließen." },
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
        message = { "Der Launcher ist in diesem Zustand nicht nutzbar.\nDies kann durch ein unerwartetes Schließen während das Spiel lief verursacht worden sein.\n\nVersuchen, die Dateien automatisch zu reparieren?\nNicht-Weltdaten aus Ihrer letzten Spielsitzung können hierbei verloren gehen.\n\nVor dem Versuch bitte alle Spiel-Ordner und Dateien schließen.\nDIESEN VORGANG NUR STARTEN, WENN KEINE SPIELINSTANZ LÄUFT!" },
        notification = { "Launcher-Dateien inkonsistent! Hier klicken zum Beheben." },
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
        setup = Launcher.Setup(
            dirPicker = { "Ein Verzeichnis auswählen" },
            error = { "Kein valider Pfad: ${it.message}" },
            initializing = { "Datenverzeichnis wird initalisiert" },
            title = { "Datenverzeichnis auswählen" },
            message = { "Hier kann das Verzeichnis, in dem alle Instanzdaten gespeichert werden ausgewählt werden.\nDies kann ein leeres Verzeichis oder ein Verzeichnis mit bestehenden Launcher-Daten sein." }
        ),
        status = Launcher.Status(
            preparing = { progress -> "Einmaliges Setup wird durchgeführt... $progress%" },
            restartRequired = { "Neustart erforderlich. Bitte neu starten." }
        ),
        patch = Launcher.Patch(
            running = { "Daten Aktualisierung läuft..." },
            message = { "Die Daten müssen aktualisiert werden, um mit der neuen Version kompatibel zu sein." },
            title = { "Die Launcher Daten müssen aktualisiert werden" },
            backup = { "Backup vor dem Aktualisieren erstellen" },
            backupHint = { "Abhängig von der Datengröße kann dies viel Zeit und Speicherplatz in Anspruch nehmen."},
            start = { "Aktualisierung starten" },
            status = Launcher.Patch.Status(
                createBackup = { "Backup wird erstellt" },
                gameDataComponents = { "Spieldaten werden bewegt" },
                gameDataSaves = { "Welten werden bewegt" },
                gameDataMods = { "Mods werden bewegt" },
                removeBackupIncludedFiles = { "Backups werden aus einbegriffenen Dateien gelöscht" },
                upgradeComponents = { "Komponenten werden aktualisiert" },
                upgradeMainManifest = { "Launchermanifest wird aktualisiert" },
                upgradeInstances = { "Instanzen werden aktualisiert" },
                upgradeSaves = { "Welten Komponenten werden aktualisiert" },
                upgradeResourcepacks = { "Ressourcenpaket Komponenten werden aktualisiert" },
                upgradeOptions = { "Einstellungskomponenten werden aktualisiert" },
                upgradeMods = { "Mods Komponenten werden aktualisiert" },
                upgradeVersions = { "Versionskomponenten werden aktualisiert" },
                upgradeJavas = { "Java Komponenten werden aktualisiert" },
                componentDirectories = { "Komponentenverzeichnisse werden umbenannt" },
                includedFiles = { "Einbegriffene Dateien werden umstrukturiert" },
                includedFilesInstances = { "Einbegriffene Dateien der Instanzen werden umstrukturiert" },
                includedFilesSaves = { "Einbegriffene Dateien der Welten Komponenten werden umstrukturiert" },
                includedFilesResourcepacks = { "Einbegriffene Dateien der Ressourcenpaket Komponenten werden umstrukturiert" },
                includedFilesOptions = { "Einbegriffene Dateien der Options Komponenten werden umstrukturiert" },
                includedFilesMods = { "Einbegriffene Dateien der Mods Komponenten werden umstrukturiert" },
                removeResourcepacksArgument = { "Ressourcenpaket Argumente werden entfernt" },
                texturepacksIncludedFiles = { "Texturenpakete werden einbegriffenen Dateien hinzugefügt" },
                removeLogin = { "Gespeicherte Logindaten werden entfernt" },
                restructureMods = { "Mods werden umstrukturiert" },
                upgradeSettings = { "Einstellungen werden aktualisiert" },
            )
        ),
    ),
    list = List(
        full = { "Komfortabel" },
        compact = { "Kompakt" },
        minimal = { "Minimal" }
    ),
    login = Login(
        button = { "Login mit Microsoft" },
        cancel = { "Login abbrechen" },
        keepLoggedIn = { "Eingeloggt bleiben" },
        label = Login.Label(
            authenticating = { "Einloggen..." },
            authenticatingSub = { state ->
                state?.let {
                    when(it) {
                        AuthenticationStep.MICROSOFT -> "Mit Microsoft einloggen"
                        AuthenticationStep.XBOX_LIVE -> "Xbox Live Schlüssel abfragen"
                        AuthenticationStep.XBOX_SECURITY -> "Xbox Sicherheitsschlüssel abfragen"
                        AuthenticationStep.MOJANG -> "Mit Mojang einloggen"
                        AuthenticationStep.MINECRAFT -> "Minecraft Profil abfragen"
                    }
                } ?: ""
            },
            failure = { "Login fehlgeschlagen. Bitte erneut versuchen!" },
            success = { user -> "Willkommen, ${user ?: "Anonymer Nutzer"}!" },
            offline = { "Im Offline-Modus gestartet." }
        ),
        logout = { "Gespeichertes Logindaten löschen" },
        offline = { "Im Offline-Modus starten" },
        offlineNotification = { "Offline-Modus aktiv. Funktionalität eingeschränkt." },
        popup = Login.Popup(
            close = { "Schließen" },
            content = { "Zum einloggen den folgenden Link im Browser öffnen:" to "und diesen Code eingeben:" },
            copyContent = { "Kopieren"},
            open = { "Link im Browser öffnen und Code kopieren" },
            title = { "Login mit Microsoft" }
        ),
        tip = { "TIPP: ${
            arrayOf(
                "Dateien können in Welten, Ressourcenpaket oder Mod Komponenten gezogen werden, um diese zu importieren.",
                "Welten oder Server können direkt gestartet werden. Diese auswählen und den Spielen Knopf klicken.",
                "Die Farbe des Launchers kann in den Einstellungen angepasst werden.",
                "Den Nachrichten Knopf am oberen Bildschirmrand klicken, um die neuesten Neuigkeiten zu sehen.",
                "Mit dem Mauszeiger kann über die Spielzeit einer Instanz gefahren werden, um die genaue Zeit anzuzeigen.",
                "Die Mods-Komponente einer Instanz kann durch auswahl des der Option \"Keine Komponente\" entfernt werden.",
                "Launcher-Dateien können an einem anderen Speicherort gespeichert werden. Der Ort wird in den Einstellungen ausgewählt.",
                "Die Sortierung von Komponenten kann durch Klicken auf den Sortierknopf neben dem Zeilenkopf geändert werden.",
                "Der Launcher kann komplett durch benutzen der Tabulator-Taste verwendet werden.",
                "Dateien können in die Komponenteneinstellungen gezogen werden, um diese den Einbegriffenen Dateien hinzuzufügen.",
                "Die größe von Listenelementen in Komponenten kann durch klicken auf den Listenanzeige-Knopf neben dem Titel angepasst werden",
                "Die Anzeigegröße kann in den Einstellungen angepasst werden.",
                "In den Einstellungen kann die Discord Integration angeschaltet werden, die allen anzeigt, was gerade gespielt wird."
            ).random()
        }" },
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
                import = { "Mods importieren" },
                importComponent = { "Mods aus anderen Komponenten auswählen:" },
                importFile = { "Lokale Moddateien kopieren:" },
                importing = { "Mods werden importiert..." },
                selectedFiles = { "Ausgewählte Mods:" },
                tooltipAdd = { "Hinzufügen" },
                tooltipExpand = { expanded -> if(expanded) "Einklappen" else "Ausklappen" },
                tooltipFile = { "Moddatei auswählen" }
            ),
            searchPlaceholder = { "Nach einer Mod suchen" },
            settings = Manager.Mods.Settings(
                order = { down -> if(down) "Priorität verringern" else "Priorität erhöhen" },
                providers = { "Mod-Quellen Priorität"},
                help = { "Wenn möglich, werden mods von der höchst-priorisierten Quelle heruntergeladen.\nWenn eine Quelle verboten ist, werden Mods niemals von dort heruntergeladen." },
                state = { enabled -> if(enabled) "Verbieten" else "Erlauben" },
                tooltip = { "Einstellungen öffnen" }
            ),
            update = Manager.Mods.Update(
                auto = { "Automatisch aktualisieren" },
                disable = { "Mods ohne passende Version deaktivieren" },
                enable = { "Deaktivierte Mods aktivieren"},
                noUpdates = { "Keine Updates verfügbar" },
                notViewed = { "$it weitere Update${if(it > 1) "s" else ""} verfügbar" },
                remaining = { "$it Update${if(it > 1) "s" else ""} verbleiben..."},
                settings = { "Aktualisierungseinstellungen" },
                tooltip = { "Nach Updates suchen" }
            ),
            version = { "Spielversion" },
            noVersion = { "Version konnte nicht bestimmt werden" }
        ),
        resourcepacks = Manager.Resourcepacks(
            delete = { "Ressourcenpaket löschen" },
            deleteTexturepack = { "Texturenpaket löschen" },
            deleteTexturepackTitle = { "Das Ressourcenpaket wirklich löschen?" },
            deleteTitle = { "Das Ressourcenpaket wirklich löschen?" },
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
                tooltipFile = { "Ressourcenpaket auswählen" },
                unknownCancel = { "Abbrechen" },
                unknownConfirm = { "Ressourcenpaket importieren" },
                unknownMessage = { file -> "Die Datei \"${file.name}\" hat kein dem Launcher bekanntes Ressourcenpaketformat.\nDas Ressourcenpaket trotzdem importieren?" },
                unknownTitle = { "Unbekanntes Ressourcenpaketformat" }

            ),
            tooltipAdd = { "Ressourcenpaket hinzufügen" },
        ),
        saves = Manager.Saves(
            delete = { "Welt löschen" },
            deleteTitle = { world -> "Die Welt${world?.let { " \"${world.name}\"" }} wirklich löschen?" },
            deleteMessage = { "Diese Aktion kann nicht rückgängig gemacht werden.\nAlle Daten dieser Welt werden unwiederruflich gelöscht." },
            deleteConfirm = { world -> "Ja, die Welt${world?.let { " \"${world.name}\"" }} permanent löschen" },
            deleteCancel = { "Abbrechen" },
            import = Manager.Component.ImportStrings(
                delete = { "Welt entfernen" },
                import = { "Welten importieren" },
                importComponent = { "Welten aus anderen Komponenten auswählen:" },
                importFile = { "Lokale Weltendateien kopieren:" },
                importing = { "Welten werden importiert..." },
                selectedFiles = { "Ausgewählte Welten:" },
                tooltipAdd = { "Hinzufügen" },
                tooltipFile = { "Weltendatei auswählen" },
                unknownCancel = { "Abbrechen" },
                unknownConfirm = { "Welt importieren" },
                unknownMessage = { file -> "Die Datei \"${file.name}\" hat kein dem Launcher bekanntes Weltenformat.\nDie Welt trotzdem importieren?" },
                unknownTitle = { "Unbekanntes Weltenformat" }
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
        notification = { "Neue Neuigkeiten verfügbar! Hier klicken zum Anzeigen." },
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
                unableMessage = { instance -> "Sie wird von folgender Instanz verwendet: ${instance.name.value}." },
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
            emptyTitle = { "Keine Instanzen erstellt." },
            game = Selector.Instance.Game(
                cleanupFailCancel = { "Abbrechen: der Launcher wird unbenutzbar" },
                cleanupFailMessage = { "Spieldateien konnten nicht an ihren Zielort bewegt werden.\nVor dem erneuten Versuchen bitte alle Spiel-Ordner und Dateien schließen!\nDurch das Klicken von \"Abbrechen\" wird der Launcher unbenutzbar." },
                cleanupFailRetry = { "Erneut versuchen" },
                cleanupFailTitle = { "Aufräumen von Spielressourcen fehlgeschlagen!" },
                errorMessage = { message -> "Fehler:\n$message\nDiesen Fehler bitte melden."},
                errorTitle ={ "Spielstart fehlgeschlagen!" },
                exitingMessage = { "Das Spiel wird geschlossen." },
                exitingTitle = { "Spielressourcen werden aufgeräumt..." },
                preparingMessage = { "Das Spiel starten in Kürze." },
                preparingTitle ={ "Spielressourcen werden vorbereitet..." },
                runningMessage ={ "Das Spiel schließen, um Aktionen im Launcher durchzuführen." },
                runningNotification = { instance -> "Aktuell läuft: ${instance.name.value}" },
                runningOpen = { "Spielordner öffnen" },
                runningStop = { "Spielprozess beenden" },
                runningTitle = { "Das Spiel läuft..." },
                crashClose = { "Schließen" },
                crashMessage = { message -> "Fehler:\n$message\nDies wurde vermutlich nicht durch den Launcher verursacht." },
                crashReports = { "Absturzberichte öffnen" },
                crashTitle = { "Das Spiel wurde unerwartet geschlossen" }
            ),
            mods = { "Mods Komponente" },
            options = { "Einstellungskomponente" },
            play = { "Instanz starten" },
            resourcepacks = { "Ressourcenpaketkomponente" },
            saves = { "Welten Komponente" },
            title = { "Instanzen" },
            version = { "Version" }
        ),
        mods = Selector.Mods(
            empty = { "Mods hierher ziehen oder auf den" to "Knopf oben drücken, um Mods hinzuzufügen." },
            emptyTitle = { "Keine Mods hinzugefügt." },
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
            empty = { "Ressourcenpakete hierher ziehen oder auf den" to "Knopf oben drücken, um welche importieren." },
            emptyTitle = { "Keine Ressourcenpakete hinzugefügt." },
            title = { "Ressourcenpakete" },
            resourcepacks = { "Ressourcenpakete:" },
            texturepacks = { "Texturenpakete:" }
        ),
        saves = Selector.Saves(
            empty = { "Welten hierher ziehen oder auf den" to "Knopf oben drücken, um welche importieren." },
            emptyTitle = { "Keine Welten hinzugefügt." },
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
        appearance = Settings.Appearance(
            decrement = { "Verringern" },
            displayScale = { "Anzeigegröße:" },
            increment = { "Erhöhen" },
            largeHint = { "Große Anzeigegrößen können Inhalt abschneiden" },
            smallHint = { "Kleine Anzeigegrößen können die Lesbarkeit einschränken" },
            title = { "Darstellung" },
            tooltipAdvanced = { "Erweiterte Darstellungseinstellungen ${if(it) "verstecken" else "anzeigen"}"},
            background = { "Hintergrund" },
            backgroundTooltip = { "Hintergrundfarbe auswählen" },
            container = { "Container" },
            containerTooltip = { "Containerfarbe auswählen" },
            text = { "Text" },
            textLight = { "Helle Textfarbe auswählen" },
            textDark = { "Dunkle Textfarbe auswählen" },
            reset = { "Standardfarben wiederherstellen" },
            minimizeOnRunning = { "Minimieren während das Spiel läuft" },
        ),
        cleanup = Settings.Cleanup(
            button = { "Ungenutzte Dateien löschen" },
            cancel = { "Abbrechen" },
            close = { "Schließen" },
            confirm = { "Dateien Löschen" },
            deleting = { "Dateien werden gelöscht..." },
            failureMessage = { "Nicht alle Dateien konnten gelöscht werden.\nDies beeinflusst die funktionalität des Launchers wahrscheinlich nicht.\nDetails wurden in die Logs geschrieben." },
            failureTitle = { "Löschen fehlgeschlagen." },
            libraries = { "Auch ungenutzte Bibliotheken löschen" },
            message = { "Alle versionsdateien, die von keiner Instanz genutzt werden, werden gelöscht um Speicherplatz zu sparen.\n Versionen können jederzeit wieder installiert werden." },
            success = { "Ungenutzte Dateien gelöscht." },
            title = { "Ungenutzte Dateien löschen?" }
        ),
        debugNotification = { "Debug-Modus ${if(it) "aktiviert" else "deaktiviert"}!" },
        discord = Settings.Discord(
            instanceExample = { "MeineInstanz"},
            instanceToggle = { "Instanznamen anzeigen" },
            modLoaderToggle = { "Mod-Loader anzeigen" },
            timeSuffix = { " vergangen" },
            timeToggle = { "Spielzeit anzeigen" },
            title = { "Discord Integration" },
            versionToggle = { "Spielversion anzeigen" },
            watermarkToggle = { "Launchernamen anzeigen" }
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
            copyData = { "Dateien zum neuen Ort kopieren" },
            remove = { "Dateien vom alten Ort entfernen" },
            select = { "Ordner auswählen" },
            success = { "Dateipfad geändert" },
            title = { "Launcher Dateipfad"}
        ),
        resetWindow = { "Fenstergröße und -position zurücksetzen" },
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
        theme = Settings.Theme(
            cancel = { "Abbrechen" },
            confirm = { "Anwenden" },
            title = { "Akzentfarbe auswählen:" }
        ),
        update = Settings.Update(
            available = { "Update verfügbar!" },
            availableMessage = { new, message ->  "Update: v${Strings.launcher.version()} → v$new ${message?.let {"\n\n$it"} ?: ""}"},
            availableTitle = { "Update verfügbar!" },
            cancel = { "Abbrechen" },
            checkingTitle = { "Es wird nach Updates gesucht..." },
            close = { "Schließen" },
            download = { "Herunterladen" },
            downloadingTitle = { "Update wird heruntergeladen..." },
            latestMessage = { "Aktuelle Version: v${Strings.launcher.version()}" },
            latestTitle = { "Der Launcher ist aktuell!" },
            successMessage = { "Der Launcher muss neugestartet werden um das Update anzuwenden." },
            successRestart = { "Jetzt Neustarten" },
            successTitle = { "Das Update wurde heruntergeladen." },
            tooltip = { "Nach Updates suchen" },
            unavailableMessage = { "Ein Update ist verfügbar, aber der Launcher kann nicht automatisch aktualisiert werden.\nBitte online nach manueller Update-Anleitung suchen." },
            unavailableTitle = { "Update kann nicht durchgeführt werden." }
        ),
        updateUrl = Settings.UpdateUrl(
            apply = { "Anwenden" },
            popupClose = { "Schließen" },
            popupMessage = {e -> "Der folgende Fehler ist aufgetreten:\n${e.message}" },
            popupTitle = { "URL konnte nicht geändert werden" },
            title = { "Update URL" }
        ),
        user = { "Eingeloggt als:" },
        version = { "Version: v${Strings.launcher.version()}" }
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
        system = { "Systemeinstellung" },
        green = { "Grün" },
        blue = { "Blau" },
        magenta = { "Magenta" },
        orange = { "Orange" },
        custom = { "Benutzerdefiniert" }
    ),
    units = Units(
        days = { "d" },
        hours = { "h" },
        minutes = { "m" },
        seconds = { "s" },
        megabytes = { "mb" },
        pixels = { "px" },
        resolutionBy = { "x" },
        accurateTime = { secs ->
            "${secs/3600}h ${(secs%3600/60).toString().padStart(2,'0')}m ${(secs%60).toString().padStart(2,'0')}s"
        }
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
        forgeHint = { "Forge Installation ist momentan experimentell" },
        forgeTooltip = { "Das installieren der häufigsten Versionen sollte funktionieren, aber Installationen können fehlschlagen oder unnutzbare Versionen produzieren.\nFehler können gerne gemeldet werden, werden aber voraussichtlich nicht schnell behoben." }
    )
)