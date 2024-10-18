package dev.treset.treelauncher.backend.util.serialization

import androidx.compose.ui.graphics.Color
import com.google.gson.*
import java.lang.reflect.Type

class ColorSerializer : JsonSerializer<Color> {
    override fun serialize(
        state: Color,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        return jsonSerializationContext.serialize(state.value.toLong())
    }
}

class ColorDeserializer : JsonDeserializer<Color> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        jsonDeserializationContext: JsonDeserializationContext
    ): Color {
        val long = jsonDeserializationContext.deserialize<Long>(jsonElement, Long::class.java)
        return Color(long shr 32)
    }
}