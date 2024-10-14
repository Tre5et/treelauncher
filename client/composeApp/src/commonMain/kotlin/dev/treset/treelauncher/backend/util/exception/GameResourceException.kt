package dev.treset.treelauncher.backend.util.exception

import java.io.IOException

class GameResourceException : IOException {
    constructor(message: String?, cause: Exception?) : super(message, cause)
    constructor(message: String?) : super(message)
}
