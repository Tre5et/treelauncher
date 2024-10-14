'use client';

import Collapsible from "react-collapsible";
import { Release } from "./version-page";
import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import Markdown from "react-markdown";
import { logDownload } from "@/app/logging";

export function VersionContent({
        release,
        locale,
        current
    } : {
        release: Release,
        locale: string,
        current?: boolean
    }) {
        const [open, setOpen] = useState(false);

        return(
            <Collapsible
                trigger={(
                    <div className="flex flex-row justify-center items-center relative w-full cursor-default">
                        <motion.span 
                            initial={{rotate: -90, translateY: "0.05rem"}} 
                            animate={{rotate: open? 0 : -90}} 
                            className="text-3xl material-symbols-rounded select-none absolute left-0 cursor-pointer"
                        >expand_more</motion.span>
                        <div className="flex-col top-0">
                            <div className={`flex flex-row items-center justify-center gap-1 text-center ${current ? "text-2xl" : "text-xl"}`}>
                                {
                                    release.prerelease && ({"de": "Pre-Release:"}[locale] || "Pre-Release:") 
                                    || current && ({"de": "Neueste Vollversion:"}[locale] || "Latest Release:")
                                    || {"de": "Ältere Version:"}[locale] || "Older Version:"
                                } {release.name} 
                            </div>
                            <div className="flex flex-row gap-x-8 gap-y-1 flex-wrap justify-center items-center">
                                {release.assets.map((asset) => {
                                    if(asset.name.endsWith(".msi") || asset.name.endsWith(".exe")) {
                                        return (
                                            <p key={asset.name} onClick={()=> {
                                                logDownload(release.name, "win/ins")
                                                window.open(asset.browser_download_url, "_blank")
                                            }}>
                                                <a href={asset.browser_download_url}>{{"de": "Windows Installationsdatei"}[locale] || "Windows Installer"}</a>
                                            </p>
                                        )
                                    }
                                    if(asset.name.endsWith(".zip")) {
                                        return (
                                            <p key={asset.name} className="text-sm" onClick={()=>{
                                                logDownload(release.name, "win/zip")
                                                window.open(asset.browser_download_url, "_blank")
                                            }}>
                                            <a href={asset.browser_download_url}>{{"de": "Windows Portable"}[locale] || "Windows Portable"}</a>
                                        </p>)
                                    }
                                    return ""
                                })}
                            </div>
                        </div>
                    </div>
                )}
                onOpening={() => {setOpen(true)}}
                onClosing={() => {setOpen(false)}}
                transitionTime={100}
            >
                <hr className="border-accent border-dashed my-4"/>
                {{"de":<p className="w-full text-center text-lg font-bold pb-2">Die Versionseigenschaften können nicht übersetzt werden!</p>}[locale]}
                <Markdown className="text-center">
                    {release.body}
                </Markdown>
            </Collapsible>
        )
}