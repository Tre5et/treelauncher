import { Metadata } from "next";
import { VersionPage } from "./version-page";
import { logPageView } from "@/app/logging";

export const metadata: Metadata = {
    title: "Download",
    description: "Download versions of TreeLauncher.",
};

export default function Page({
    params: { locale }
   } : {
     params: { locale: string }
   }) {
    logPageView(locale, '/download')

    return (
        <VersionPage params={{locale}}/>
    )
}