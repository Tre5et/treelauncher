import Image from "next/image";
import { Button } from "./ui/button";
import Link from "next/link";

export function Title() {
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
                        A modern, component based Minecraft Launcher
                    </p>
                    <div className="flex justify-center mt-2">
                        <Link
                        href="/download"
                        className="w-min"
                        >
                            <Button tabIndex={-1}>
                                <p className="text-lg">Download</p>
                            </Button>
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
        <div className="material-symbols-rounded text-5xl w-full text-center select-none">
            expand_more
        </div>
      </div>
    )
}