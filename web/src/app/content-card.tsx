'use client';

import { motion } from "framer-motion";
import { useTheme } from "next-themes"
import Image from "next/image";
import { useEffect, useState } from "react";

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
    const { resolvedTheme } = useTheme()
    const [mounted, setMounted] = useState(false)
    useEffect(() => {
      setMounted(true)
    }, [])

    let [narrow, setNarrow] = useState(true)
    useEffect(() => {
        const mediaWatcher = window.matchMedia("(max-width: 768px)")
        setNarrow(mediaWatcher.matches);
    
        function updateIsNarrowScreen(e: MediaQueryListEvent) {
            setNarrow(e.matches);
        }
        mediaWatcher.addEventListener('change', updateIsNarrowScreen)
    
        return function cleanup() {
            mediaWatcher.removeEventListener('change', updateIsNarrowScreen)
        }
    }, [])
  
    let src: string
    if(mounted) {
        src = `/${imgId}_${resolvedTheme==="dark" ? 'dark' : 'light'}.png`
    } else {
        src = `/${imgId}_placeholder.png`
    }

    if(narrow) return (
        <div className={`md:opacity-0 relative h-min w-full flex ${rightAligned ? "flex-row-reverse" : "flex-row"} items-center justify-center gap-2 md:gap-6 flex-wrap md:flex-nowrap`}>
            {title && (
                <div className="block md:hidden">
                    <p className={`text-3xl text-center`}>
                        {title}
                    </p>
                </div>
            )}
            <Image
                src={src}
                alt={imgAlt}
                width={imgWidth? imgWidth : 500}
                height={imgHeight? imgHeight : 500}
                className={`md:w-1/2 grow-0 rounded-2xl border-4 border-accent`}
            />
            <div className="md:w-1/2 grow">
                {title && (
                    <p className={`hidden md:block text-3xl text-center ${rightAligned && !keepTextAlignment ? "md:text-right" : "md:text-left"}`}>
                        {title}
                    </p>
                )}
                <div className={`text-lg text-center ${rightAligned ? (keepTextAlignment ? "md:text-justify" :  "md:text-right") : "md:text-left"}`}>
                    {children}
                </div>
            </div>
        </div>
    )

    return (
        <motion.div
            initial={{opacity: 0, left: rightAligned? 500 : -500}}
            whileInView={{opacity: 1, left: 0}}
            viewport={{margin: "0px 500px 0px 500px", once: true}}
            className={`relative h-min w-full flex ${rightAligned ? "flex-row-reverse" : "flex-row"} items-center justify-center gap-2 md:gap-6 flex-wrap md:flex-nowrap`}>
            {title && (
                <div className="block md:hidden">
                    <p className={`text-3xl text-center`}>
                        {title}
                    </p>
                </div>
            )}
            <Image
                src={src}
                alt={imgAlt}
                width={imgWidth? imgWidth : 500}
                height={imgHeight? imgHeight : 500}
                className={`md:w-1/2 grow-0 rounded-2xl border-4 border-accent`}
            />
            <div className="md:w-1/2 grow">
                {title && (
                    <p className={`hidden md:block text-3xl text-center ${rightAligned && !keepTextAlignment ? "md:text-right" : "md:text-left"}`}>
                        {title}
                    </p>
                )}
                <div className={`text-lg text-center ${rightAligned ? (keepTextAlignment ? "md:text-justify" :  "md:text-right") : "md:text-left"}`}>
                    {children}
                </div>
            </div>
        </motion.div>
    )
}