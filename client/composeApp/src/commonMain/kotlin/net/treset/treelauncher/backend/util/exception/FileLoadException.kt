package net.treset.treelauncher.backend.util.exception

import java.io.IOException

class FileLoadException : IOException {
    constructor(message: String?) : super(message)
    constructor(message: String?, parent: Exception?) : super(message, parent)
}
