import Image from "next/image";
import Link from "next/link";
import { Button } from "./button";
import { ThemeToggle } from "./theme-toggle";

export function NavBar() {
    return (
        <div className="sticky top-0 bg-white dark:bg-background">
            <div className="flex justify-between items-center p-2">
                <Link 
                    className="align-middle flex items-center"
                    key="home"
                    href="/"
                >
                    <Image
                        src="/icon.svg"
                        className="align-middle"
                        width={32}
                        height={32}
                        alt="Launcher Icon"
                    />
                    <span className="align-middle text-lg ml-2">TreeLauncher</span>
                </Link>
                <div className="align-middle flex items-center">
                    <Link
                        href="/about"
                    >
                        About
                    </Link>
                    <Link
                        href="https://github.com/tre5et/treelauncher"
                        className="ml-4 align-middle"
                    >
                        GitHub
                    </Link>
                    <Link
                        href="/download"
                        className="ml-4 align-middle"
                    >
                        <Button
                        >
                            Download
                        </Button>
                    </Link>
                    <ThemeToggle
                        className="align-middle ml-4"
                    />
                </div>
            </div>
            <hr
                className="bg-secondary"
            />
        </div>
    )
}