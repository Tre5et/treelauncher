import Link from "next/link";

export function Footer() {
    return (
        <div className="w-full bg-white dark:bg-background mt-24">
            <hr className="border-secondary"/>
            <p className="flex flex-row gap-4 justify-center p-2">
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
            </p>
            <p className="text-center">&copy; 2024 by TreSet</p>
        </div>
    )
}