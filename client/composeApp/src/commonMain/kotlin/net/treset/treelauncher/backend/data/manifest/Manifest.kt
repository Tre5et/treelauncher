package net.treset.treelauncher.backend.data.manifest

import dev.treset.mcdl.json.GenericJsonParsable
import java.io.IOException

abstract class  Manifest(
) : GenericJsonParsable() {
    @Throws(IOException::class)
    abstract fun write()
}
