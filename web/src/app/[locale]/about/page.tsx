import { logPageView } from "@/app/logging";
import { Metadata } from "next";
import Image from "next/image";
import { Logging } from "../ui/logging";

export const metadata: Metadata = {
    title: "About",
    description: "Information about Treelauncher.",
};

export default function Page({
    params: { locale }
   } : {
     params: { locale: string }
   }) {

    return (
        <div className="flex flex-col items-center w-full">
            <Logging/>
            <div className="flex flex-col items-center p-4 text-center max-w-2xl">
                <p className="text-3xl">{{"de": "Über TreeLauncher:"}[locale] || "About Treelauncher:"}</p>
                <p className="text-xl mt-2">{{"de": "Kurzgefasst:"}[locale] || "In Short:"}</p>
                {{
                    "de": <p>TreeLauncher ist ein moderner Minecraft launcher, der das Konzept von Komponenten zur Instanzverwaltung einführt, um die Verwaltung und Erstellung zu vereinfachen.</p>
                }[locale] ||
                    <p>TreeLauncher is a modern Minecraft launcher that introduces the concept of components for managing instances to streamline instance management and creation.</p>
                }
                <p className="text-xl mt-2">{{"de": "Das \"Team\":"}[locale] || "The \"Team\":"}</p>
                {{
                    "de": <p>TreSet (Planung, Entwickung, Design und alles andere)</p>
                }[locale] ||
                    <p>TreSet (Planning, Development, Design and everything else)</p>
                }
                <p className="text-xl mt-2">{{"de": "Probleme und Feedback:"}[locale] || "Issues and Feedback:"}</p>
                {{
                    "de": <p>Probleme auf der <a href="https://github.com/tre5et/treelauncher/issues">GitHub Issues</a> Seite melden.</p>
                }[locale] ||
                    <p>Report issues on the <a href="https://github.com/tre5et/treelauncher/issues">GitHub Issues</a> page.</p>
                }
                <p className="text-xl mt-2">{{"de": "Beitragen:"}[locale] || "Contributing:"}</p>
                {{
                    "de": <p>Das Projekt auf <a href="https://github.com/tre5et/treelauncher">GitHub</a> forken und einen Pull Request erstellen.</p>
                }[locale] ||
                    <p>Fork the project on <a href="https://github.com/tre5et/treelauncher">GitHub</a> and make a pull request.</p>
                }
                <p className="text-xl mt-2">{{"de": "Ein kurzer Hinweis zur Accountverwaltung:"}[locale] || "A brief note on Account handling:"}</p>
                {{
                    "de": <div>
                        <p>Anmeldedaten werden durch die <a href="https://github.com/HyCraftHD/Minecraft-Authenticator">Minecraft-Authenticator</a> Java Bibliothek verwaltet.</p>
                        <p className="mt-1">Die Anmeldung finded in einem eingebauten Browser-Fenster auf der offizierllen Microsoft Anemeldeseite statt. Das Ergebnis wird von Minecraft Authenticator verarbeitet. Der Laucher nutzt die verarbeiteten Nutzerdaten um den Minecraft Nutzer während des Spielstarts zu authentifizieren. Skin Daten werden über die offizielle <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">Mojang API</a> abgerufen.</p>
                        <p className="mt-1">Die durch Minecraft Authenticator erstellten Nutzerdaten werden im lokalen Verzeichnis gespeichert, falls &quot;Eingeloggt bleiben&quot; während des Anmeldens ausgewählt ist. Diese Datei wird gelöscht, wenn sich der Nutzer über die Einstellungen abmeldet. Sie wird durch Minecraft Authenticator gelesen und verarbeitet, um eine automatische Anmeldung bei Launcherstart zu ermöglichen.</p>
                    </div>
                }[locale] ||
                    <div>
                        <p>Any login data is handled by the <a href="https://github.com/HyCraftHD/Minecraft-Authenticator">Minecraft-Authenticator</a> third-party Java library.</p>
                        <p className="mt-1">Login is done through a built-in browser window to an official Microsoft Login page. The Login results are processed by Minecraft Authenticator. The launcher uses the User data provided to authenticate the Minecraft user at game launch. Skin data is retrieved from the official <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">Mojang API</a>.</p>
                        <p className="mt-1">The secrets file provided by Minecraft-Authenticator is saved to the launcher data directory if &quot;Keep logged in&quot; is selected on login. It is deleted when the logout from settings is used. The file is read and used by Minecraft-Authenticator to reauthenticate on each app start.</p>
                    </div>
                }
                <p className="text-3xl mt-6">{{"de": "Über Mich:"}[locale] || "About Me:"}</p>
                <p className="text-xl mt-2">{{"de": "Ich bin TreSet"}[locale] || "I'm TreSet:"}</p> 
                <div className="flex flex-row items-center gap-2">
                    <p>{{"de": "Soziale Medien:"}[locale] || "Socials:"}</p>
                    <a href="https://github.com/tre5et">
                        <Image 
                            src="/github.svg" 
                            width={20} 
                            height={20} 
                            alt={{"de": "GitHub Profil"}[locale] || "GitHub profile"}
                            className="invert dark:invert-0"
                        />
                    </a>
                    <a href="https://modrinth.com/user/TreSet">
                        <Image 
                            src="/modrinth.svg" 
                            width={20} 
                            height={20} 
                            alt={{"de": "Modrinth Profil"}[locale] || "Modrinth profile"}
                            className="invert dark:invert-0"
                        />
                    </a>
                    <a href="https://www.curseforge.com/members/tre5et">
                        <Image 
                            src="/curseforge.svg" 
                            width={20} 
                            height={20} 
                            alt={{"de": "Curseforge Profil"}[locale] || "Curseforge profile"}
                            className="invert dark:invert-0"
                        />
                    </a>
                    <a href="https://twitter.com/ThatTreSet">
                        <Image 
                            src="/twitter.svg" 
                            width={20} 
                            height={20} 
                            alt={{"de": "Twitter (X) Profil"}[locale] || "Twitter (X) profile"}
                            className="invert dark:invert-0"
                        />
                    </a>
                </div>
                <p className="text-xl mt-2">{{"de": "Andere Projekte:"}[locale] || "Other Projects:"}</p>
                <p>Minecraft Mods:&nbsp;
                    <a href="https://modrinth.com/mod/ridehud">RideHud</a>&nbsp;
                    <a href="https://modrinth.com/mod/adaptiveview">AdaptiveView</a>&nbsp;
                    <a href="https://modrinth.com/mod/simple-compass">Simple Compass</a>&nbsp;
                    <a href="https://modrinth.com/mod/vanillaconfig">VanillaConfig</a>&nbsp;
                </p>
                <p>Minecraft Server Discord Manager: <a href="https://github.com/Tre5et/mcs-discman">MCS-Discman</a></p>
                <p className="text-xl mt-2">{{"de": "Spenden:"}[locale] || "Dontate:"}</p>
            {{
                "de": <p>Unterstütze mich auf <a href="https://ko-fi.com/treset">Ko-Fi</a>.</p>
            }[locale] ||
                <p>Support me on <a href="https://ko-fi.com/treset">Ko-Fi</a>.</p>
            }
            {{
                "de": <p>Spenden werden nicht unbedingt für die Weiterentwicklung verwendet.</p>
            }[locale] ||
                <p>Funds may not necessarily go towards further development.</p> 
            }
            </div>
        </div>    
    )
}