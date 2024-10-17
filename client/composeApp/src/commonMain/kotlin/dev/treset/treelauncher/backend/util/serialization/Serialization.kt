package dev.treset.treelauncher.backend.util.serialization

import androidx.compose.runtime.MutableState
import com.google.gson.GsonBuilder
import dev.treset.mcdl.json.JsonUtils

fun modifySerializer() {
    JsonUtils.setDefaultGson(
        GsonBuilder()
            .registerTypeAdapterFactory(MutableStateTypeAdapter.FACTORY)
            .setPrettyPrinting()
    )
}