package net.treset.treelauncher.backend.sync

import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.SerializationException
import net.treset.treelauncher.localization.strings
import java.util.*

class ComponentList(val entries: List<Entry>) : GenericJsonParsable() {
    class Entry(val id: String, val name: String) {

        override fun toString(): String {
            return "${name.ifBlank { strings().sync.unknown() }} (${if (id.length > 8) id.substring(0, 7) else "$id..."})"
        }
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): ComponentList {
            val entries = fromJson(json, Array<Entry>::class.java)
            return ComponentList(listOf(*entries))
        }
    }
}
