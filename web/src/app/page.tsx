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
            imgAlt="Instance Manager"
            rightAligned
            title="Worlds"
          > 
            Add worlds from local files and copy them between instances.<br/>Directly from the launcher.
          </ContentCard>
          <ContentCard
            imgId="mods_open"
            imgAlt="Instance Manager"
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
            title="Components"
            imgClassName="w-1/2"
          >
            <p>Components hold Saves, Resourcepacks, Options or Mods.<br/>Instances are made up of components.</p>
            <p className="text-3xl mt-4">Shared</p>
            <p>Progress is automatically shared between Instances that use the same component.<br/>Never copy your worlds or settings between instances again.</p>
            <p className="text-3xl mt-4">Modifiable</p>
            <p>Quickly create copies of your components to keep yor progress when updating.<br/>No copying files manually required.</p>
          </ContentCard>
        </div>
      </div>
    </main>
  );
}
