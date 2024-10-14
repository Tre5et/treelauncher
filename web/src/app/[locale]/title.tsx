import Image from "next/image";
import { Button } from "./ui/button";
import Link from "next/link";

export function Title({
    params: { locale }
   } : {
     params: { locale: string }
   }) {
    return (
        <div>
            <div
                className="flex flex-col-reverse md:flex-row justify-center items-center gap-8 my-32 px-4"
            >
                <div>
                    <p className="text-center text-6xl font-semibold text-primary">
                        TreeLauncher
                    </p>
                    <p className="text-center">
                        {{"de": "Ein moderner, Komponenten-basierter Minecraft Launcher"}[locale] || "A modern, component based Minecraft launcher"}
                    </p>
                    <div className="flex justify-center mt-2">
                        <Link
                            href={`/${locale}/download`}
                            className="w-min"
                        >
                            <Button tabIndex={-1}>
                                <p className="text-lg">{{"de": "Herunterladen"}[locale] || "Download"}</p>
                            </Button>
                        </Link>
                    </div>
                </div>
                <div>
                <Image
                    src="/icon.svg"
                    height={128}
                    width={128}
                    alt={{"de": "Launcher Titelbild"}[locale] || "Launcher Icon"}
                />
                </div>
        </div>
        <div className="material-symbols-rounded text-5xl w-full text-center select-none">
            expand_more
        </div>
      </div>
    )
}