'use client';

import { useTheme } from "next-themes"
import Image from "next/image";

interface ContentCardProps extends React.PropsWithChildren {
    imgId: string
    imgAlt: string
    imgWidth?: number
    imgHeight?: number
    rightAligned?: boolean
    keepTextAlignment?: boolean
    title?: string
}

export function ContentCard({
    imgId, rightAligned, keepTextAlignment, imgAlt, imgWidth, imgHeight, title, children
}: ContentCardProps) {
    const {theme, setTheme} = useTheme()
    return (
        <div className={`relative h-min w-full flex ${rightAligned ? "flex-row-reverse" : "flex-row"} items-center justify-center gap-2 md:gap-6 flex-wrap md:flex-nowrap`}>
            {title && (
                <div className="block md:hidden">
                    <p className={`text-3xl text-center`}>
                        {title}
                    </p>
                </div>
            )}
            <Image
                src={"/" + imgId + (theme==="dark" ? "_dark" : "_light") + ".png"}
                alt={imgAlt}
                width={imgWidth? imgWidth : 500}
                height={imgHeight? imgHeight : 500}
                className={`md:w-1/2 object-cover rounded-2xl border-4 border-accent`}
            />
            <div className="md:w-1/2">
                {title && (
                    <p className={`hidden md:block text-3xl text-center ${rightAligned && !keepTextAlignment ? "md:text-right" : "md:text-left"}`}>
                        {title}
                    </p>
                )}
                <p className={`text-lg text-center ${rightAligned ? (keepTextAlignment ? "md:text-justify" :  "md:text-right") : "md:text-left"}`}>
                    {children}
                </p>
            </div>
        </div>
    )
}