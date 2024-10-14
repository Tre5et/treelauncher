'use client';

import { useTheme } from "next-themes"
import { useEffect, useState } from "react";

export function ThemeToggle({...rest} : React.ComponentPropsWithoutRef<"div">) {
    const { resolvedTheme, setTheme } = useTheme()
    const [mounted, setMounted] = useState(false)
    useEffect(() => {
      setMounted(true)
    }, [])
  
    if (!mounted) {
      return (
        <div
            {...rest}
        >
            <span 
                className="material-symbols-rounded text-center select-none translate-y-1"
            >
                light_mode
            </span>
        </div>
      )
    }

    return (
    <div
        {...rest}
    >
        <span 
            className="material-symbols-rounded cursor-pointer text-center select-none translate-y-1"
            onClick={() => resolvedTheme === "dark" ? setTheme("light") : setTheme("dark")}
            tabIndex={0}
        >
            <p className="hidden dark:block">light_mode</p> 
            <p className="block dark:hidden">dark_mode</p>
        </span>
    </div>
  )
}