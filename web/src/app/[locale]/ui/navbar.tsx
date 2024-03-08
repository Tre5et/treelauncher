import Image from "next/image";
import Link from "next/link";
import { Button } from "./button";
import { ThemeToggle } from "./theme-toggle";
import { LanguageSelector } from "./language-selector";

export function NavBar({
    params: { locale }
   } : {
     params: { locale: string }
   }) {
    return (
        <div className="sticky top-0 bg-white dark:bg-background z-10">
            <div className="flex justify-between items-center p-2">
                <Link 
                    className="align-middle flex items-center"
                    href={`/${locale}`}
                >
                    <Image
                        src="/icon.svg"
                        width={32}
                        height={32}
                        alt={{"de": "Launcher Titelbild"}[locale] || "Launcher Icon"}
                        className="hidden md:block "
                    />
                    <span className="hidden md:block text-lg ml-2">TreeLauncher</span>
                    <span className="block md:hidden material-symbols-rounded">home</span>
                </Link>
                <div className="align-middle flex items-center gap-4">
                    <Link href={`/${locale}/about`}>
                        {{"de": "Über"}[locale] || "About"}
                    </Link>
                    <a href="https://github.com/tre5et/treelauncher">
                        GitHub
                    </a>
                    <Link href={`/${locale}/download`}>
                        <Button tabIndex={-1}>
                            {{"de": "Herunterladen"}[locale] || "Download"}
                        </Button>
                    </Link>
                    <ThemeToggle/>
                    <LanguageSelector params={{locale}}/>
                </div>
            </div>
            <hr
                className="border-secondary"
            />
        </div>
    )
}