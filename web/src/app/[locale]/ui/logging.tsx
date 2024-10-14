'use client';

import { logPageViewPath } from "@/app/logging"
import { usePathname } from "next/navigation";
import { useEffect } from "react"

export function Logging({
    ...rest
}) {
    const path = usePathname()
    useEffect(() => {
        logPageViewPath(path)
    }, [path])

    return ((<></>))
}