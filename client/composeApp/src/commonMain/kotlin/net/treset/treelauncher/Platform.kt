package net.treset.treelauncher

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform