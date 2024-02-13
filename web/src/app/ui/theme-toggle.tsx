'use client';

import { useTheme } from "next-themes"

export function ThemeToggle() {
    const { theme, setTheme } = useTheme()
    return (
    <div>
        <span 
            className="material-symbols-outlined cursor-pointer"
            onClick={() => theme === "dark" ? setTheme("light") : setTheme("dark")}
        >
            {theme === "dark" ? (<p>light_mode</p>) : (<p>dark_mode</p>)}
        </span>
    </div>
  )
}