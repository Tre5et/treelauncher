import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { ThemeProvider } from "next-themes";
import { Providers } from "./providers";
import { exo2 } from "./ui/fonts";
import { NavBar } from "./ui/navbar";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "TreeLauncher",
  description: "Homepage of TreeLauncher",
  icons: "/icon.ico"
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={`${inter.className} ${exo2.className} bg-white dark:bg-background text-black dark:text-white`}>
        <Providers>
          <div>
            <NavBar/>
            {children}
          </div>
        </Providers>
      </body>
    </html>
  );
}
