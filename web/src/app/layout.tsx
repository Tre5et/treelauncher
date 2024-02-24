import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "./providers";
import { Footer } from "./ui/footer";
import { NavBar } from "./ui/navbar";

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
      <body className={`bg-white dark:bg-background text-black dark:text-white overflow-x-hidden`}>
        <Providers>
          <div className="flex flex-col min-h-dvh">
            <NavBar/>
            {children}
            <Footer className="mt-auto"/>
          </div>
        </Providers>
      </body>
    </html>
  );
}
