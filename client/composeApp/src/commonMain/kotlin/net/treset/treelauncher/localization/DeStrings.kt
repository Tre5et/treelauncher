package net.treset.treelauncher.localization

import net.treset.treelauncher.instances.InstanceDetails

class DeStrings : Strings(
    Components(
        { "Neu erstellen" },
        Components.Details (
            { "Keine Komponente ausgewählt" }
        )
    ),
    Creator(
        { "Erstellen" },
        { "Komponente" },
        { "Name darf nicht leer sein." },
        { "Keine Komponente ausgewählt."},
        Creator.Instance(
            { "Instanz" },
            Creator.Instance.Popup(
                { "Zurück zu Instanzen" },
                { "Instanz wird erstellt..." },
                { "Erstellung fehlgeschlagen.\nDiesen Fehler bitte melden!" },
                { "Erstellung abgeschlossen" },
                { "Unbekannter Erstellungsstatus.\nDiesen Fehler bitte melden!" }
            ),
            { "Welten" },
            { "Ressourcenpakete" },
            { "Optionen" },
            { "Mods" },
            { "Version" },

            ),
        Creator.Mods (
            { "Version" }
        ),
        { "Name" },
        { "Erstellen" },
        { "Vorhandene Komponente verwenden" },
        { "Vorhandene Komponente kopieren" },
        Creator.Status(
            { "Erstellung wird vorbereitet..." },
            { "Mods Komponente wird erstellt..." },
            { "Optionskomponente wirde erstellt..." },
            { "Ressoucenpaketkomponente wird erstellt..." },
            { "Werltenkomponente wird erstellt..." },
            Creator.Status.Version(
                { "Version wird erstellt..." },
                { "Minecraft Version wird erstellt..." },
                { "Assets werden heruntergeladen..." },
                { "Bibliotheken werden heruntergeladen..." },
                { "Fabric Version wird erstellt..." }
            ),
            { "Java Version wird heruntergeladen..." },
            { "Erstellung wird abgeschlossen..." }
        ),
        Creator.Version(
            { "Keine Version ausgewählt." },
            { "Kein Versionstyp ausgewählt." },
            { "Keine Fabric Version ausgewählt." },
            { "Snapshots anzeigen" },
            { "Fabric Version" },
            { "Laden..." },
            { "Versionstyp" },
            { "Version" }
        )
    ),
    Error(
        { "Akzeptieren" },
        { message -> "Fehler:\n$message\nDiesen Fehler bitte melden." },
        { "Ein Fehler ist aufgetreten!" },
        { "Launcher schließen" },
        { message -> "Fehler:\n$message\nDiesen Fehler bitte melden." },
        { "Ein kritischen Fehler ist aufgetreten!" },
        { "Unbekannter Fehler" }
    ),
    Game(
        { instance -> "${strings().launcher.slug()}:${strings().launcher.version()}:${instance.instance.first.id.substring(0,3)}...${instance.instance.first.id.substring(instance.instance.first.id.length - 2)}"},
        { instance -> instance.instance.first.name }
    ),
    Language(
        { default -> "Englisch${if (default) " (Systemsprache)" else ""}" },
        { default -> "Deutsch${if (default) " (Systemsprache)" else ""}" }
    ),
    Launcher(
        status = Launcher.Status(
            { progress -> "Einmaliges Setup wird durchgeführt... $progress%" },
            { "Neustart erforderlich. Bitte neu starten." }
        )
    ),
    Login(
        { state -> "Einloggen: ${state.pageTitle ?: "Wird geladen..."} (${state.lastLoadedUrl ?: "URL wird abgerufen..."})" },
        { "Login mit Microsoft" },
        Login.Label(
            { "Einloggen..." },
            { "Login fehlgeschlagen. Bitte erneut versuchen!" },
            { user -> "Willkommen, ${user ?: "Anonymer Nutzer"}!" }
        ),
        { "Eingeloggt bleiben" }
    ),
    Manager(
        { "Änderung anwenden" },
        Manager.Component(
            { "Abbrechen" },
            { "Hinzufügen" },
            { "Einbegriffene Datei hinzufügen" },
            { "Datei" },
            { "Dateiname eingeben" },
            { "Verzeichnis" },
            { "Einbegriffene Dateien:" },
            { "Komponenteneinstellungen" }
        ),
        Manager.Instance(
            Manager.Instance.Change(
                { "Schließen" },
                { "Abbrechen" },
                { "Version wird geändert..." },
                { "Ja, Version ändern" },
                { "Ein Fehler beim Ändern der Version ist aufgetreten.\nDiesen Fehler bitte melden." },
                { "Kompatibilität kann nicht garantiert werden.\nEs ist empfohlen, die Version durch Erstellen einer neuen Instanz mit gleichen Komponenten zu ändern."},
                { "Version geändert!" },
                { "Eine Komponente auswählen" },
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
                { "Welten" },
                { "Ressourcenpakete" },
                { "Optionen" },
                { "Mods" },
                { "Einstellungen" }
            ),
            Manager.Instance.Settings(
                { "Neues Argument eingeben" },
                { "JVM-Argumente" },
                { "Instanzspeicher:" },
                { "Auflösung:" },
                { "Instanzeinstellungen" },
            )
        ),
        Manager.Mods(
            { "Lokale mod hinzufügen" },
            Manager.Mods.Local(
                { "Hinzufügen" },
                { "Abbrechen" },
                { "Curseforge Projekt ID" },
                { "Ungültige Projekt ID" },
                { "Datei" },
                { "Keine Datei ausgewählt" },
                { "Modrinth Projekt ID" },
                { "Ungültige Projekt ID" },
                { "Name" },
                { "Version" },
                { "Keine Version ausgewählt" }
            ),
            Manager.Mods.Change(
                { "Die Spielversion dieser Komponente wirklich wechseln?" },
                { "Dadurch kann Kompatibilität mit den aktuellen Instanzen nicht gewährleistet werden.\n\nVersionswechsel sollten nur unmittelbar nach erstellen der Komponente durchgeführt werden." },
                { "Ja, Version ändern" },
                { "Abbrechen" }
            ),
            { "Spielversion:" },
            { "Aktuelle Mods" },
            Manager.Mods.Search(
                { "Mod hinzufügen" },
                { "Zurück" },
                { "Nach einer Mod suchen" },
                { "Suchen" },
                { "Mods werden gesucht..."},
                { "Keine Mod mit passender Version gefunden." }
            ),
            Manager.Mods.Update(
                { "Automatisch aktualisieren" },
                { "Mods ohne passende Version deaktivieren" },
                { "Deaktivierte Mods aktivieren"},
                { "Nach Updates suchen" }
            ),
            { "Spielversion" }
        )
    ),
    Menu(
        { "Löschen" },
        { "Umbenennen" },
        { "Nichts ausgewählt" },
        { "Im Dateiexplorer öffnen" },
        { "Start" },
        { "Komponente hochladen" }
    ),
    Nav(
        { "Instanz erstellen" },
        { "Instanzen" },
        { "Mods Komponenten" },
        { "Optionskomponenten" },
        { "Ressourcenpaketkomponenten" },
        { "Weltenkomponenten" },
        { "Einstellungen" }
    ),
    News(
        { "Schließen" },
        { "Wichtige Neuigkeiten:" },
        { "Neuigkeiten:"}
    ),
    Selector(
        Selector.Component(
            Selector.Component.Delete(
                { "Abbrechen" },
                { "Löschen" },
                { "Diese Aktion kann nicht rückgängig gemacht werden!\nDaten in dieser Komponente werden unwiderruflich gelöscht.\nDiese Komponente wird von keiner Instanz verwendet." },
                { "Diese Komponente wirklich löschen?" },
                { "Schließen" },
                { instance -> "Sie wird von folgender Instanz verwendet: ${instance.instance.first.name}." },
                { "Diese Komponente kann nicht gelöscht werden!" },
            ),
            Selector.Component.Edit(
                { "Abbrechen" },
                { "Speichern" },
                { "Kein neuer Name eingegeben" },
                { "Neuer Name" },
                { "Komponente umbenennen" }
            )
        ),
        Selector.Instance(
            Selector.Instance.Delete(
                { "Abbrechen" },
                { "Löschen" },
                { "Diese Aktion kann nicht rückgängig gemacht werden.\nAlle verwendeten Komponenten bleiben bestehen." },
                { "Diese Instanz wirklich löschen?" }
            ),
            Selector.Instance.Game(
                { message -> "Fehler:\n$message\nDiesen Fehler bitte melden."},
                { "Spielstart fehlgeschlagen!" },
                { "Das Spiel starten in Kürze." },
                { "Spielressourcen werden vorbereitet..." },
                { "Das Spiel schließen, um Aktionen im Launcher durchzuführen." },
                { "Das Spiel läuft..." },
                { "Schließen" },
                { message -> "Fehler:\n$message\nDies wurde vermutlich nicht durch den Launcher verursacht." },
                { "Absturzberichte öffnen" },
                { "Das Spiel wurde unerwartet geschlossen" }
            ),
            { "Mods Komponente" },
            { "Optionskomponente" },
            { "Ressourcenpaketkomponente" },
            { "Weltenkomponente" },
            { "Instanzen" },
            { "Version" }
        ),
        Selector.Mods(
            Selector.Mods.Content(
                { "Mod löschen" },
                { "Mod deaktivieren" },
                { "Mod aktivieren" },
                { "Version installieren" },
                { "Im Browser öffnen" }
            ),
            { "Mods" }
        ),
        Selector.Options(
            { "Optionen" }
        ),
        Selector.Resourcepacks(
            { "Ressourcenpakete" }
        ),
        Selector.Saves(
            Selector.Saves.Play(
                { "Abbrechen" },
                { "In welcher Instanz soll diese Welt gestartet werden?" },
                { "Welt starten" },
                { "Mehrere Instanzen nutzen diese Komponente." },
                { "Schließen" },
                { "Schnellstart ist nur für Welten verfügbar, die von einer Instanz genutzt werden." },
                { "Keine Instanz nutzt diese Komponente." }
            ),
            { "Server:" },
            { "Welten" },
            { "Welten:"}
        )
    ),
    Settings(
        { "Darstellung" },
        { "Sprache:" },
        { "Ausloggen" },
        Settings.Path(
            { "Anwenden" },
            { "Dateipfad wird geändert..." },
            { "Schließen" },
            { "Dateipfad konnte nicht geändert werden" },
            { e -> "Der folgende Fehler ist aufgetreten:\n$e" },
            { "Kein Ordner ausgewählt" },
            { "Dateien vom alten Ort entfernen" },
            { "Ordner auswählen" },
            { "Dateipfad geändert" },
            { "Launcher Dateipfad"}
        ),
        { "Diese Einstellung wird nach dem Neustart angewendet" },
        { "Quellcode Repository" },
        { "Quellcode Repository öffnen" },
        Settings.Sync(
            { "Schließen" },
            { "Test fehlgeschlagen!" },
            { "Komponentensynchronisation" },
            { "API-Schlüssel:"},
            { "Schlüssel" },
            { "Port" },
            { "Test erfolgreich!" },
            { "Test" },
            { "URL" }
        ),
        { "Einstellungen" },
        { "Farben:" },
        Settings.Update(
            { "Update verfügbar!" },
            { current, new ->  "Update: v$current → v$new"},
            { "Update verfügbar!" },
            { "Abbrechen" },
            { "Es wird nach Updates gesucht..." },
            { "Schließen" },
            { "Herunterladen" },
            { file, current, total -> "$file ($current/$total)" },
            { "Update wird heruntergeladen..." },
            { version -> "Aktuelle Version: v$version" },
            { "Der Launcher ist aktuell!" },
            { "Der Launcher muss neugestartet werden um das Update anzuwenden." },
            { "Jetzt Neustarten" },
            { "Das Update wurde heruntergeladen." },
            { "Nach Updates suchen" },
            { "Ein Update ist verfügbar, aber der Launcher kann nicht automatisch aktualisiert werden.\nBitte online nach manueller Update-Anleitung suchen." },
            { "Update kann nicht durchgeführt werden." }
        ),
        { "Eingeloggt als:" },
        { "Version: v${strings().launcher.version()}" }
    ),
    Sorts(
        { "Name (Aktivierte zuerst)" },
        { "Zuletzt gespielt" },
        { "Name" },
        { "Spielzeit" }
    ),
    Sync(
        { "Komponente erfolgreich synchronisiert" },
        { "Schließen" },
        Sync.Download(
            { "Abbrechen" },
            { "Herunterladen" },
            { "Komponente zum Herunterladen auswählen" },
            { "Schließen" },
            { "Keine neuen Komponenten verfügbar" },
            { "Komponente heruntergeladen" }
        ),
        Sync.Status(
            { "Synchronisationsdaten werden gesammelt..." },
            { "Benötigte Version wird heruntergeladen..." },
            { "Dateien werden heruntergeladen..." },
            { "Synchronisation abgeschlossen." },
            { "Synchronisation wird gestartet..." },
            { "Dateien werden hochgeladen..." }
        ),
        { "Komponente wird synchronisiert..."},
        { "<Unbekannter Name>" }
    ),
    Theme(
        { "Dunkel" },
        { "Hell" },
        { "Systemeinstellung" }
    ),
    Units(
        { "t" },
        { "h" },
        { "m" },
        { "s" },
        { "mb" },
        { "px" },
        { "x" }

    ),
    Updater(
        { "Schließen" },
        { "Launcher Schließen" },
        Updater.Status(
            { "Die vorherige Version wurde wiederhergestellt.\nDetails wurden in die Logs geschrieben.\nDiesen Fehler bitte melden." },
            { "Update fehlgeschlagen." },
            { "Der Launcher kann nicht weiter verwendet werden.\nDetails wurden in die Logs geschrieben.\nDiesen Fehler bitte melden und manuell beheben." },
            { "Ein fataler Fehler ist während des Updates aufgetreten." },
            { "Die neue Version wurde angewendet." },
            { "Der Launcher wurde erfolgreich aktualisiert." },
            { "Der Launcher kann nicht genutzt werden, während der Updater läuft.\nDen launcher schließen, um das Update fortzusetzen." },
            { "Der Updater läuft noch." },
            { "Das Löschen temporärer Update Ressourcen ist fehlgeschlagen.\nDas verursacht wahrscheinlich keine Fehler.\nDetails wurden in die Logs geschrieben." },
            { "Der Launcher wurde aktualisiert." }
        )
    )
)