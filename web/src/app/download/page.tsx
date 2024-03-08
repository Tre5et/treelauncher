import { Metadata } from "next";
import { VersionPage } from "./version-page";

export const metadata: Metadata = {
    title: "Download",
    description: "Download versions of TreeLauncher.",
};

export default function Page() {
    return (
        <VersionPage/>
    )
}