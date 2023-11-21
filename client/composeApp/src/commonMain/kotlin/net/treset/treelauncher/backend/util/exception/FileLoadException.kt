package net.treset.treelauncher.backend.util.exception

class FileLoadException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, parent: Exception?) : super(message, parent)
}
