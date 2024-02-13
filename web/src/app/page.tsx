import Image from "next/image";
import { ThemeToggle } from "./ui/theme-toggle";
import { NavBar } from "./ui/navbar";
import { Button } from "./ui/button";
import Link from "next/link";

export default function Home() {
  return (
    <main>
      <div
        className="flex justify-center items-center gap-8 flex-wrap-reverse my-32"
      >
        <div>
          <p className="text-center text-6xl font-semibold text-primary">
            TreeLauncher
          </p>
          <p className="text-center">
            A modern, component based Minecraft Launcher
          </p>
          <div className="flex justify-center mt-2">
            <Link
              href="/download"
              className="w-min"
            >
              <Button>Download</Button>
            </Link>
          </div>
        </div>
        <div>
          <Image
            src="/icon.svg"
            height={128}
            width={128}
            alt="Launcher Icon"
          />
        </div>
      </div>
      <div className="material-symbols-rounded text-5xl w-dvw text-center select-none">
        expand_more
      </div>
    </main>
  );
}
