package dev.treset.treelauncher.backend.util.sort

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class SortProviderSerializer<T>: KSerializer<SortProvider<T>> {
    override val descriptor = PrimitiveSerialDescriptor("Comparator", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): SortProvider<T> {
        val id = decoder.decodeString()

        try {
            return sortProviders[id] as SortProvider<T>
        } catch (e: Exception) {
            throw SerializationException("Unknown comparator id: $id")
        }
    }
    override fun serialize(encoder: Encoder, value: SortProvider<T>) {
        encoder.encodeString(value.id)
    }
}

@Serializable(with = SortProviderSerializer::class)
abstract class SortProvider<T> : Comparator<T> {
    abstract val name: String
    abstract val id: String

    override fun toString(): String = name
}