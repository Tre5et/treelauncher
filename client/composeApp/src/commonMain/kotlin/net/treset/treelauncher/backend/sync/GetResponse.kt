package net.treset.treelauncher.backend.sync

import net.treset.mcdl.json.GenericJsonParsable
import net.treset.mcdl.json.SerializationException

class GetResponse(var version: Int, var difference: Array<String>) : GenericJsonParsable() {

    companion object {
        @Throws(SerializationException::class)
        fun fromJson(json: String?): GetResponse {
            return fromJson(json, GetResponse::class.java)
        }
    }
}
