'use server'
import { appendFile } from "fs"

export async function logPageView(locale: string, page: string) {
    let time = new Date()
    let str = `${time.toLocaleString("en-uk", { timeZone: "CET" })} | ${locale} | ${page}`
    console.log("page-view: " + str)
    appendFile("logs/views.log", `${str}\n`, () => {})
}

export async function logPageViewPath(pathname: string) {
    let parts = pathname.split('/')
    let locale = parts[1]
    let page = "/" + parts.slice(2).join('/')
    logPageView(locale, page)
}

export async function logDownload(version: string, packaging: string) {
    let time = new Date()
    let str = `${time.toLocaleString("en-uk", { timeZone: "CET" })} | ${version} | ${packaging}`
    console.log("download: " + str)
    appendFile("logs/downloads.log", `${str}\n`, () => {})
}