import Image from "next/image";
import { ThemeToggle } from "./ui/theme-toggle";
import { NavBar } from "./ui/navbar";
import { Button } from "./ui/button";
import Link from "next/link";
import { Title } from "./title";
import { ContentCard } from "./content-card";

export default function Home() {
  return (
    <main>
      <Title/>
      <div className="flex flex-col w-dvh items-center gap-6 mt-16">
        <p className="text-4xl">Manage your game</p>
        <div className="flex flex-col items-center max-w-6xl px-12 gap-4">
          <ContentCard
            imgId="instances_settings"
            imgAlt="Instance Manager"
            title="Instances"
          > 
            Create multiple Instances with different versions.<br/>Vanilla or fabric.
          </ContentCard>
          <ContentCard
            imgId="saves_open"
            imgAlt="Saves Manager"
            rightAligned
            title="Worlds"
          > 
            Add worlds from local files and copy them between instances.<br/>Directly from the launcher.
          </ContentCard>
          <ContentCard
            imgId="mods_open"
            imgAlt="Mods Manager"
            title="Mods"
          > 
            Manage everything about your mods directly in the Launcher.
          </ContentCard>
        </div>
        <p className="text-3xl">And more...</p>

        <p className="text-4xl mt-16">Components for easy management</p>
        <div className="flex flex-col items-center max-w-6xl px-12 gap-4">
          <ContentCard
            imgId="components"
            imgAlt="Component Selectors"
            imgClassName="w-1/2"
          >
            <p className="text-3xl mt-4">Components</p>
            <p>Components hold Saves, Resourcepacks, Options or Mods.<br/>Instances are made up of components.</p>
            <p className="text-3xl mt-4">Shared</p>
            <p>Progress is automatically shared between Instances that use the same component.<br/>Never copy your worlds or settings between instances again.</p>
            <p className="text-3xl mt-4">Modifiable</p>
            <p>Quickly create copies of your components to keep yor progress when updating.<br/>No copying files manually required.</p>
          </ContentCard>
        </div>

        <p className="text-4xl mt-16">Powerful Mods Manager</p>
        <div className="flex flex-col items-center max-w-6xl px-12 gap-4">
          <ContentCard
            imgId="mods_search"
            imgAlt="Mod Search"
            imgClassName="w-1/2"
            rightAligned
            keepTextAlignment
          >
            <p className="text-3xl mt-4">Unified</p>
            <p>Search Mods across Modrinth and Curseforge in one combined search.<br/>Mods from both platforms are automatically merged so that you only see them once.</p>
            <p className="text-3xl mt-4">Automatic</p>
            <p>Conveniently check for updates so yor're always up-to-date.<br/>Mod dependencies are automatically installed.</p>
            <p className="text-3xl mt-4">Customizable</p>
            <p>Add local mod files easily and link them to online mods to update them as soon as there is a newer version.<br/>Edit details of installed mods.</p>
          </ContentCard>
        </div>

        <p className="text-4xl mt-16">Get there easily</p>
        <div className="flex flex-col items-center max-w-6xl px-12 gap-4">
          <ContentCard
            imgId="accessibility"
            imgAlt="Dark and Light Theme"
            imgClassName="w-1/2"
          >
            <p className="text-3xl mt-4">Play quickly</p>
            <p>Launch any instance with two clicks.<br/>Start a world or server directly using quick-play.</p>
            <p className="text-3xl mt-4">Use conveniently</p>
            <p>The interface is optimized for a fast and intuitive user experience without slowdowns or stutters.</p>
            <p className="text-3xl mt-4">Customizable</p>
            <p>Supports multiple languages and themes.<br/>Uses tooltips, tab-navigation and screen-reader texts to provide anyone with an optimal experience.</p>
          </ContentCard>
        </div>

        <p className="text-4xl mt-16">Suggestions or feedback?</p>
        <div className="flex flex-col items-center max-w-6xl px-12 gap-4">
          <p className="text-lg text-center">Leave them on the <a href="https://github.com/tre5et/treelauncher" className="underline">TreeLauncher GitHub page</a>.<br/><br/>Or fork the project and implement your own changes.</p>
        </div>

        <p className="text-4xl mt-16">Support Development</p>
        <div className="flex flex-col items-center max-w-6xl px-12 gap-4">
          <Link
            href="https://ko-fi.com/treset"
          >
            <Button tabIndex={-1}>
              <Image
                src="/kofi.png"
                width={32}
                height={32}
                alt="Ko-Fi Logo"
              />
              Support me on Ko-Fi
            </Button>
          </Link>
        </div>
      </div>
    </main>
  );
}
