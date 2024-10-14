import Link from "next/link";

export function Footer(
    {
        className,
        locale,
        ...rest
    } : {
        className?: string,
        locale: string
    }
    ) {
    return (
        <div
            className={`w-full bg-white dark:bg-background pt-8 ${className}`}
            {...rest}
        >
            <hr className="border-secondary"/>
            <div className="flex flex-row gap-4 justify-center p-2">
                <Link
                    href="/about"
                >{{"de": "Über"}[locale] || "About"}</Link>
                &bull;
                <Link
                    href="/download"
                >{{"de": "Herunterladen"}[locale] || "Download"}</Link>
                &bull;
                <a
                    href="https://github.com/tre5et/treelauncher"
                >GitHub</a>
                &bull;
                <a
                    href="https://ko-fi.com/treset"
                >Ko-Fi</a>
            </div>
            <p className="text-center">&copy; 2024 {{"de": "TreSet"}[locale] || "by TreSet"}</p>
        </div>
    )
}