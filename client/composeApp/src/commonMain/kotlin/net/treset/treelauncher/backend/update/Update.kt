package net.treset.treelauncher.backend.update

import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.SerializationException

class Update(
    var id: String?,
    var changes: List<Change>?,
    var message: String?,
    var latest: Boolean?
) :
    GenericJsonParsable() {
    enum class Mode {
        FILE,
        DELETE,
        REGEX,
        LINE
    }

    class Change(
        var path: String,
        var mode: Mode,
        var elements: List<Element>,
        var updater: Boolean
    ) {
        class Element(
            var pattern: String,
            var value: String,
            var meta: String,
            var isReplace: Boolean
        )
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): Update {
            return fromJson(json, Update::class.java)
        }
    }
}
