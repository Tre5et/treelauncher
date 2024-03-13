'use client';

import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import Collapsible from "react-collapsible";
import { VersionContent } from "./version-card";

export function VersionPage({
    params: { locale }
   } : {
     params: { locale: string }
   }) {
    const [releases, setReleases] = useState<Release[]>()
    useEffect(() => {
        fetch("https://api.github.com/repos/tre5et/treelauncher/releases")
            .then(function(response) {
                response.json()
                    .then(function(json) {
                        setReleases(json)
                    })
            })
    }, [])

    const [open, setOpen] = useState(false);

    if(releases == null) return

    const firstReleaseIndex = releases!.findIndex((release) => {return !release.prerelease})

    return (
        <div className="flex flex-col items-center gap-8 p-4">
            {
                releases!.map((release, index) => {
                    if(index == firstReleaseIndex) {
                        return (
                            <div key={release.name} className="bg-secondary rounded-2xl w-full max-w-2xl p-4">
                                <VersionContent release={release} locale={locale} current/>
                            </div>
                        )
                    }
                    return ""
                })
            }

            <div className="bg-secondary rounded-2xl w-full max-w-2xl p-4">
                <Collapsible
                    trigger={(
                        <div className="flex flex-row justify-center items-center">
                            <motion.span 
                                initial={{rotate: -90, translateY: "0.05rem"}} 
                                animate={{rotate: open? 0 : -90}} 
                                className="text-3xl material-symbols-rounded select-none"
                            >expand_more</motion.span>
                            <div className="text-2xl text-center">{{"de": "Andere Versionen"}[locale] || "Other Versions"}</div>
                        </div>
                    )}
                    onOpening={() => {setOpen(true)}}
                    onClosing={() => {setOpen(false)}}
                    transitionTime={150}
                >
                    {
                        releases!.map((release, index) => {
                            if(index != firstReleaseIndex) {
                                return (
                                    <div key={index.toString()}>
                                        <hr className="border-accent my-2"/>
                                        <VersionContent release={release} locale={locale}/>
                                    </div>
                                )
                            }

                            return ""
                        })
                    }
                </Collapsible>
            </div>
        </div>
    )
}

export interface Release {
    name: string
    body: string
    prerelease?: boolean
    html_url: string
    assets: Asset[]
}

export interface Asset {
    url: string
    name: string
    browser_download_url: string
}