'use client';

import { useTheme } from "next-themes"

export function ThemeToggle({...rest} : React.ComponentPropsWithoutRef<"div">) {
    const { theme, setTheme } = useTheme()
    return (
    <div
        {...rest}
    >
        <span 
            className="material-symbols-rounded cursor-pointer text-center select-none translate-y-1"
            onClick={() => theme === "dark" ? setTheme("light") : setTheme("dark")}
            tabIndex={0}
        >
            {theme === "dark" ? (<p>light_mode</p>) : (<p>dark_mode</p>)}
        </span>
    </div>
  )
}