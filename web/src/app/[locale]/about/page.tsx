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
                        <p>Die Anmeldung findet über die standard Micorsoft OAuth Methode statt.</p>
                        <p className="mt-1">Die Anmeldung findet durch den Gerätebrowser statt. Dazu muss den angefragten Berechtigungen zugestimmt werden. Anschließend bekommt der Launcher einen Access-Token, der nur für die Minecraft Authentifizierung verwendet werden kann. Skin Daten werden über die offizielle <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">Mojang API</a> abgerufen.</p>
                        <p className="mt-1">Die Access-Daten werden im lokalen Launcher Verzeichnis gespeichert, falls &quot;Eingeloggt bleiben&quot; während des Anmeldens ausgewählt ist, um nach einem Neustart erneut angemeldet zu werden. Diese Datei wird gelöscht, wenn sich der Nutzer über die Einstellungen abmeldet.</p>
                        <p className="mt-1">Die Implementierung wird vom <a href="https://github.com/Tre5et/mcdl">MCDL</a> Auth Modul bereitgestellt.</p>
                    </div>
                }[locale] ||
                    <div>
                        <p>Login is done using the official Microsoft OAuth process.</p>
                        <p className="mt-1">The device browser is used for this process. The requested permissions need to be granted to obtain an access token, that can only be used to authenticate with minecraft. Skin data is retrieved from the official <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">Mojang API</a>.</p>
                        <p className="mt-1">The access data is saved to the launcher data directory if &quot;Keep logged in&quot; is selected on login, to allow re-authentication on login. It is deleted when the logout from settings is used.</p>
                        <p className="mt-1">The login implementation is provided by the <a href="https://github.com/Tre5et/mcdl">MCDL</a> Auth module.</p>
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