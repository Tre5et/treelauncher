import Image from "next/image";
import { Button } from "./ui/button";
import { Title } from "./title";
import { ContentCard } from "./content-card";
import { Metadata } from "next";
import { useEffect } from "react";
import { logDownload, logPageView } from "../logging";

export const metadata: Metadata = {
  description: "TreeLauncher is a modern Minecraft launcher that introduces the concept of components for managing instances to streamline instance management and creation.",
};


export default function Home({
   params: { locale }
  } : {
    params: { locale: string }
  }) {
  logPageView(locale, '/')

  return (
    <main className="mb-20">
      <Title params={{locale}}/>
      <div className="flex flex-col w-dvh items-center gap-6 mt-16">
        <p className="text-4xl text-center">{
          {
            "de": "Verwalte dein Spiel",
          }[locale] || "Manage your game"
        }</p>
        <div className="flex flex-col items-center max-w-7xl w-full px-4 md:px-12 gap-8 md:gap-4">
          <ContentCard
            imgId="instances"
            imgAlt={{"de": "Instanzverwaltung"}[locale] || "Instance Manager"}
            imgWidth={1050}
            imgHeight={591}
            title= {{"de": "Instanzen"}[locale] || "Instances"}
            locale={locale}
          >
              {{
                "de": (<p>Mehrere Instanzen mit verschiedenen Versionen estellen.<br/>Vanilla, <a href="https://fabricmc.net/">Fabric</a>, <a href="https://minecraftforge.net/">Forge</a> oder <a href="https://quiltmc.org/">Quilt</a>.</p>)
              }[locale] ||
              <p>Create multiple Instances with different versions.<br/>Vanilla, <a href="https://fabricmc.net/">Fabric</a>, <a href="https://minecraftforge.net/">Forge</a> or <a href="https://quiltmc.org/">Quilt</a>.</p>
            }
          </ContentCard>
          <ContentCard
            imgId="saves"
            imgAlt={{"de": "Weltenverwaltung"}[locale] || "Saves Manager"}
            imgWidth={900}
            imgHeight={507}
            rightAligned
            title={{"de": "Welten"}[locale] || "Worlds"}
            locale={locale}
          > 
            {{
              "de": <p>Welten aus lokalen Dateien hinzufügen und zwischen Instanzen kopieren.<br/>Direkt aus dem Launcher.</p>
            }[locale] ||
              <p>Add worlds from local files and copy them between instances.<br/>Directly from the launcher.</p>
            }
          </ContentCard>
          <ContentCard
            imgId="mods"
            imgAlt={{"de": "Modverwaltung"}[locale] || "Mods Manager"}
            imgWidth={900}
            imgHeight={507}
            title="Mods"
            locale={locale}
          > 
            {{
              "de": <p>Modverwaltung direkt aus dem Launcher.</p>
            }[locale] ||
              <p>Manage everything about your mods directly in the Launcher.</p>
            }
          </ContentCard>
        </div>
        <p className="text-3xl">{{"de": "Und vieles mehr..."}[locale] || "And more..."}</p>

        <p className="text-4xl mt-16 text-center">{{"de": "Komponenten für einfache Verwaltung"}[locale] || "Components for easy management"}</p>
        <div className="flex flex-col items-center max-w-7xl px-4 md:px-12 gap-4">
          <ContentCard
            imgId="components"
            imgAlt={{"de": "Komponentenauswahlmenüs"}[locale] || "Component Selectors"}
            imgWidth={790}
            imgHeight={700}
            locale={locale}
          >
            <p className="text-3xl mt-4">{{"de": "Komponenten"}[locale] || "Components"}</p>
            {{
              "de": <p>Komponenten beinhalten Speicherstände, Ressourcenpakete, Einstellungen und Mods.<br/>Instanzen setzen sich aus Komponenten zusammen.</p>
            }[locale] ||
              <p>Components hold Saves, Resourcepacks, Options or Mods.<br/>Instances are made up of components.</p>
            }
            <p className="text-3xl mt-4">{{"de": "Geteilt"}[locale] || "Shared"}</p>
            {{
              "de": <p>Fortschritt wird automatisch zwischen Instanzen, die die gleiche Komponente nutzen, geteilt.<br/>Welten und Einstellungen müssen nicht mehr manuell hin und her kopiert werden.</p>
            }[locale] ||
              <p>Progress is automatically shared between instances that use the same component.<br/>Never copy your worlds or settings between instances again.</p>
            }
            <p className="text-3xl mt-4">{{"de": "Änderbar"}[locale] || "Modifiable"}</p>
            {{
              "de": <p>Kopien von Komponenten werden im Handumdrehen erstellt, sodass der Fortschritt beim aktualiseren bestehen bleibt.<br/>Ohne mauelles Kopieren von Dateien.</p>
            }[locale] ||
              <p>Quickly create copies of your components to keep your progress when updating.<br/>No copying files manually required.</p>
            }
          </ContentCard>
        </div>

        <p className="text-4xl mt-16 text-center">{{"de": "Leistungsstarke Mod-Verwaltung"}[locale] || "Powerfull Mods Manager"}</p>
        <div className="flex flex-col items-center max-w-7xl px-4 md:px-12 gap-4">
          <ContentCard
            imgId="search"
            imgAlt={{"de": "Mod Suche"}[locale] || "Mod Search"}
            imgWidth={800}
            imgHeight={860}
            rightAligned
            keepTextAlignment
            locale={locale}
          >
            <p className="text-3xl mt-4">{{"de": "Vereinigt"}[locale] || "Unified"}</p>
            {{
              "de": <p>Mods werden auf <a href="https://modrinth.com/">Modrinth</a> und <a href="https://www.curseforge.com/">Curseforge</a> parallel gesucht.<br/>Die mods werden in eine einheitliche Ansicht kombiniert.</p>
            }[locale] ||
              <p>Search Mods across <a href="https://modrinth.com/">Modrinth</a> and <a href="https://www.curseforge.com/">Curseforge</a> in one combined search.<br/>Mods from both platforms are automatically merged so that you only see them once.</p>
            }
            <p className="text-3xl mt-4">{{"de": "Automatisch"}[locale] || "Automatic"}</p>
            {{
              "de": <p>Mods können mit einem Knopfdruck auf neue Versionen geprüft werden.<br/>Benötigte Abhängigkeiten der Mods werden automatisch installiert.</p>
            }[locale] ||
              <p>Conveniently check for updates so you&apos;re always up-to-date.<br/>Mod dependencies are automatically installed.</p>
            }
            <p className="text-3xl mt-4">{{"de": "Einstellbar"}[locale] || "Customizable"}</p>
            {{
              "de": <p>Lokale Moddateien können leicht in den Launcher eingefügt und zu Online Mods verlinkte werden, sodass diese automatisch aktualisiert werden, sobald eine neue Version existiert.<br/>Details installierter Mods können bearbeitet werden.</p>
            }[locale] ||
              <p>Add local mod files easily and link them to online mods to update them as soon as there is a newer version.<br/>Edit details of installed mods.</p>
            }
            </ContentCard>
        </div>

        <p className="text-4xl mt-16 text-center">{{"de": "Nutzerfreundliche Bedienung"}[locale] || "Get there easily"}</p>
        <div className="flex flex-col items-center max-w-7xl px-4 md:px-12 gap-4">
          <ContentCard
            imgId="accessibility"
            imgAlt={{"de": "Heller und Dunkler Farbmodus"}[locale] || "Dark and Light Theme"}
            imgWidth={900}
            imgHeight={800}
            locale={locale}
          >
            <p className="text-3xl mt-4">{{"de": "Schnell spielen"}[locale] || "Play quickly"}</p>
            {{
              "de": <p>Eine beliebige Instanz mit zwei Klicks starten.<br/>Oder direkt in eine Welt oder einen Server starten dank Quick-Play.</p>
            }[locale] ||
              <p>Launch any instance with two clicks.<br/>Start a world or server directly using quick-play.</p>
            }
            <p className="text-3xl mt-4">{{"de": "Einfach nutzen"}[locale] || "Use conveniently"}</p>
            {{
              "de": <p>Die Benutzeroberfläche ist für eine schnelle und intutitive Bedienung ohne Ruckler oder Wartezeiten optimiert.</p>
            }[locale] ||
              <p>The interface is optimized for a fast and intuitive user experience without slowdowns or stutters.</p>
            }
            <p className="text-3xl mt-4">{{"de": "Barrierefrei"}[locale] || "Accessible"}</p>
            {{
              "de": <p>Eingebaute Unterstützung für mehrere Sprachen und Farbmodi.<br/>Durch Tooltips, Tab-Navigation und Screenreader-Texte wird eine optimale Benutzererfahrung für jeden sichergestellt.</p>
            }[locale] ||
              <p>Supports multiple languages and themes.<br/>Uses tooltips, tab-navigation and screen-reader texts to provide anyone with an optimal experience.</p>
            }
          </ContentCard>
        </div>

        <p className="text-4xl mt-16 text-center">{{"de": "Verbesserungsvorschläge oder Feedback?"}[locale] || "Suggestions or Feedback?"}</p>
        <div className="flex flex-col items-center max-w-7xl px-4 md:px-12 gap-4">
          {{
            "de": <p className="text-lg text-center">Einfach auf der <a href="https://github.com/tre5et/treelauncher/issues">GitHub Issues</a> Seite hinterlassen.<br/><br/>Oder das Projekt forken und eigene Änderungen implementieren.</p>
          }[locale] ||
            <p className="text-lg text-center">Leave them on the <a href="https://github.com/tre5et/treelauncher/issues">GitHub Issues</a> page.<br/><br/>Or fork the project and implement your own changes.</p>
          }
          </div>

        <p className="text-4xl mt-16 text-center">{{"de": "Die Weiterentwicklung unterstützen"}[locale] || "Support Deveolpment"}</p>
        <div className="flex flex-col items-center max-w-7xl px-4 md:px-12 gap-4">
          <a href="https://ko-fi.com/treset">
            <Button tabIndex={-1}>
              <Image
                src="/kofi.png"
                width={32}
                height={32}
                alt="Ko-Fi Logo"
              />
              {{"de": "Unterstütze mich auf Ko-Fi"}[locale] || "Support me on Ko-Fi"}
            </Button>
          </a>
        </div>
      </div>
    </main>
  );
}
function setMounted(arg0: boolean) {
  throw new Error("Function not implemented.");
}

