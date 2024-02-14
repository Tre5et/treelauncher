import Link from "next/link";

export function Footer({className, ...rest}: React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement>) {
    return (
        <div
            className={`w-full bg-white dark:bg-background pt-8 ${className}`}
            {...rest}
        >
            <hr className="border-secondary"/>
            <div className="flex flex-row gap-4 justify-center p-2">
                <Link
                    href="/about"
                >About</Link>
                &bull;
                <Link
                    href="/download"
                >Download</Link>
                &bull;
                <Link
                    href="https://github.com/tre5et/treelauncher"
                >GitHub</Link>
                &bull;
                <Link
                    href="https://ko-fi.com/treset"
                >Ko-Fi</Link>
            </div>
            <p className="text-center">&copy; 2024 by TreSet</p>
        </div>
    )
}