import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "./providers";
import { Footer } from "./ui/footer";
import { NavBar } from "./ui/navbar";
import { languages } from '../i18n/settings'

export const metadata: Metadata = {
  title: {
    template: "%s - TreeLauncher",
    default: "TreeLauncher",
  },
  icons: "icon.ico",
  creator: "TreSet",
  keywords: ["treelauncher", "minecraft", "launcher", "components", "mods", "forge", "fabric", "quilt", "modded", "game", "tre5et", "treset"]
};

export async function generateStaticParams() {
  return languages.map((locale) => ({ locale }))
}

export default function RootLayout({
  children,
  params: { locale },
}: Readonly<{
  children: React.ReactNode,
  params: { locale: string }
}>) {
  return (
    <html lang={locale} suppressHydrationWarning>
      <body className={`bg-white dark:bg-background text-black dark:text-white overflow-x-hidden`}>
        <Providers>
          <div className="flex flex-col min-h-dvh">
            <NavBar params={{locale}}/>
            {children}
            <Footer className="mt-auto" locale={locale}/>
          </div>
        </Providers>
      </body>
    </html>
  );
}
