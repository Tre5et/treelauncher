import Image from "next/image";
import Link from "next/link";
import { Button } from "./button";
import { ThemeToggle } from "./theme-toggle";

export function NavBar() {
    return (
        <div className="sticky top-0 bg-white dark:bg-background z-10">
            <div className="flex justify-between items-center p-2">
                <Link 
                    className="align-middle flex items-center"
                    key="home"
                    href="/"
                >
                    <Image
                        src="/icon.svg"
                        width={32}
                        height={32}
                        alt="Launcher Icon"
                    />
                    <span className="text-lg ml-2">TreeLauncher</span>
                </Link>
                <div className="align-middle flex items-center gap-4">
                    <Link
                        href="/about"
                    >
                        About
                    </Link>
                    <Link
                        href="https://github.com/tre5et/treelauncher"
                    >
                        GitHub
                    </Link>
                    <Link
                        href="/download"
                    >
                        <Button
                        >
                            Download
                        </Button>
                    </Link>
                    <ThemeToggle/>
                </div>
            </div>
            <hr
                className="border-secondary"
            />
        </div>
    )
}