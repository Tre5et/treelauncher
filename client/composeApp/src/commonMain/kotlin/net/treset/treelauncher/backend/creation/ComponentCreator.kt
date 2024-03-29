package net.treset.treelauncher.backend.creation

import net.treset.treelauncher.backend.util.exception.ComponentCreationException

interface ComponentCreator {
    @get:Throws(ComponentCreationException::class)
    val id: String?
}
