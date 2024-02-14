'use client';

import { useTheme } from "next-themes"
import Image from "next/image"

interface ContentCardProps extends React.PropsWithChildren {
    imgId: string
    imgAlt: string
    rightAligned?: boolean
    title: string
}

export function ContentCard({
    imgId: imageSrc, rightAligned: imageRight, imgAlt: imageAlt, title, children
}: ContentCardProps) {
    const {theme, setTheme} = useTheme()
    return (
        <div className="relative h-min w-full flex flex-row items-center justify-center gap-6">
            { imageRight !== true && (
                <img
                    src={imageSrc + (theme==="dark" ? "_dark" : "_light") + ".png"}
                    alt={imageAlt}
                    className={`w-3/5 object-cover rounded-2xl border-4 border-accent`}
                />
            )}
            <div className="flex-initial">
                <p className={`text-3xl ${imageRight && "text-right"}`}>
                    {title}
                </p>
                <p className={`text-lg ${imageRight ? "text-right" : "text-left"}`}>
                    {children}
                </p>
            </div>
            { imageRight === true && (
                <img
                    src={imageSrc + (theme==="dark" ? "_dark" : "_light") + ".png"}
                    alt={imageAlt}
                    className={`w-3/5 object-cover rounded-2xl border-4 border-accent`}
                />
            )}
        </div>
    )
}