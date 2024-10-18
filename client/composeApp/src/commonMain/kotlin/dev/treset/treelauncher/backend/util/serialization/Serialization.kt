package dev.treset.treelauncher.backend.util.serialization

import androidx.compose.ui.graphics.Color
import com.google.gson.GsonBuilder
import dev.treset.mcdl.json.JsonUtils

fun modifySerializer() {
    JsonUtils.setDefaultGson(
        GsonBuilder()
            .registerTypeAdapterFactory(MutableStateTypeAdapter.FACTORY)
            .registerTypeAdapter(Color::class.java, ColorSerializer())
            .registerTypeAdapter(Color::class.java, ColorDeserializer())
            .setPrettyPrinting()
    )
}