package net.treset.treelauncher.backend.sync

import net.treset.mc_version_loader.json.GenericJsonParsable
import net.treset.mc_version_loader.json.SerializationException

class ComponentData(var version: Int, var fileAmount: Int, var hashTree: List<HashEntry>) : GenericJsonParsable() {
    class HashEntry {
        val path: String
        var children: List<HashEntry>? = null
        var hash: String? = null

        constructor(path: String, hash: String?) {
            this.path = path
            this.hash = hash
        }

        constructor(path: String, children: List<HashEntry>?) {
            this.path = path
            this.children = children
        }
    }

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): ComponentData {
            return fromJson(json, ComponentData::class.java)
        }
    }
}
