'use client';

import { useTheme } from "next-themes"

interface ContentCardProps extends React.PropsWithChildren {
    imgId: string
    imgAlt: string
    rightAligned?: boolean
    keepTextAlignment?: boolean
    imgClassName?: string
    title?: string
}

export function ContentCard({
    imgId, rightAligned, keepTextAlignment, imgAlt, title, imgClassName, children
}: ContentCardProps) {
    const {theme, setTheme} = useTheme()
    return (
        <div className={`relative h-min w-full flex ${rightAligned ? "flex-row-reverse" : "flex-row"} items-center justify-center gap-6`}>
            <img
                src={imgId + (theme==="dark" ? "_dark" : "_light") + ".png"}
                alt={imgAlt}
                className={`w-3/5 object-cover rounded-2xl border-4 border-accent ${imgClassName}`}
            />
            <div>
                {title && (
                    <p className={`text-3xl ${rightAligned && !keepTextAlignment && "text-right"}`}>
                        {title}
                    </p>
                )}
                <p className={`text-lg ${rightAligned ? (keepTextAlignment ? "text-justify" :  "text-right") : "text-left"}`}>
                    {children}
                </p>
            </div>
        </div>
    )
}