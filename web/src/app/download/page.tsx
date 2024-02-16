'use client';

import { motion } from "framer-motion";
import { release } from "os";
import { useEffect, useState } from "react";
import Collapsible from "react-collapsible";

export default function Page() {

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
                                <div className="flex flex-row text-2xl text-center mb-1 justify-center items-center gap-1">
                                    Latest Release: {release.name} 
                                    <a href={release.html_url} target="_blank" className="material-symbols-rounded text-xl" style={{ transform: 'translateY(0.0625rem)' }}>open_in_new</a>
                                </div>
                                <div className="flex flex-row gap-x-8 gap-y-1 justify-center items-center flex-wrap">
                                    {release.assets.map((asset) => {
                                        if(asset.name.endsWith(".msi") || asset.name.endsWith(".exe")) {
                                            return (<p key={asset.name}><a href={asset.browser_download_url}>Windows Installer</a></p>)
                                        }
                                        if(asset.name.endsWith(".zip")) {
                                            return (<p key={asset.name} className="text-sm"><a href={asset.browser_download_url}>Windows Portable</a></p>)
                                        }
                                        return ""
                                    })}
                                </div>
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
                                className="text-3xl material-symbols-rounded"
                            >expand_more</motion.span>
                            <div className="text-2xl text-center">Older Versions</div>
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
                                        <div className="flex flex-row items-center justify-center gap-1 text-xl text-center">
                                            {release.prerelease && "Pre-Release: "}{release.name} 
                                            <a href={release.html_url} target="_blank" className="material-symbols-rounded text-lg" style={{ transform: 'translateY(0.125rem)' }}>open_in_new</a>
                                        </div>
                                        <div className="flex flex-row gap-x-8 gap-y-1 flex-wrap justify-center items-center">
                                            {release.assets.map((asset) => {
                                                if(asset.name.endsWith(".msi") || asset.name.endsWith(".exe")) {
                                                    return (<p key={asset.name}><a href={asset.browser_download_url}>Windows Installer</a></p>)
                                                }
                                                if(asset.name.endsWith(".zip")) {
                                                    return (<p key={asset.name} className="text-sm"><a href={asset.browser_download_url}>Windows Portable</a></p>)
                                                }
                                                return ""
                                            })}
                                        </div>
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

interface Release {
    name: string
    body: string
    prerelease?: boolean
    html_url: string
    assets: Asset[]
}

interface Asset {
    url: string
    name: string
    browser_download_url: string
}