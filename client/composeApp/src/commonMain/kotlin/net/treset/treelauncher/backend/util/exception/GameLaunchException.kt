package net.treset.treelauncher.backend.util.exception

class GameLaunchException : Exception {
    constructor(message: String?, cause: Exception?) : super(message, cause)
    constructor(message: String?) : super(message)
}
