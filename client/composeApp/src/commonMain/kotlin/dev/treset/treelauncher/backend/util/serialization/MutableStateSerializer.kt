package dev.treset.treelauncher.backend.util.serialization

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*


class MutableStateSerializer : JsonSerializer<MutableState<*>> {
    override fun serialize(
        state: MutableState<*>,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        return jsonSerializationContext.serialize(state.value)
    }
}

class MutableStateDeserializer<T> : JsonDeserializer<MutableState<T>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        jsonDeserializationContext: JsonDeserializationContext
    ): MutableState<T> {
        val state = jsonDeserializationContext.deserialize<T>(jsonElement, object : TypeToken<T>() {}.type)
        return mutableStateOf(state)
    }
}

class MutableStateTypeAdapter<E>(
    private val adapter: TypeAdapter<E>
) : TypeAdapter<MutableState<E>>() {
    @Throws(IOException::class)
    override fun read(reader: JsonReader): MutableState<E> {
        val read = adapter.read(reader)
        return mutableStateOf(read)
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: MutableState<E>) {
        adapter.write(out, value.value)
    }

    companion object {
        val FACTORY: TypeAdapterFactory = object : TypeAdapterFactory {
            override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                val rawType = type.rawType as Class<*>
                if (rawType != MutableState::class.java) {
                    return null
                }
                val parameterizedType = type.type as ParameterizedType
                val actualType = parameterizedType.actualTypeArguments[0]
                val adapter = gson.getAdapter(TypeToken.get(actualType))
                return MutableStateTypeAdapter(adapter) as TypeAdapter<T>
            }
        }
    }
}