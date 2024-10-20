package dev.treset.treelauncher.backend.util.serialization

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias MutableStateList<T> = SnapshotStateList<T>

typealias MutableDataStateList<T> = @Serializable(with = SnapshotStateListSerializer::class) SnapshotStateList<T>

class SnapshotStateListSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<SnapshotStateList<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor
    override fun serialize(encoder: Encoder, value: SnapshotStateList<T>) = ListSerializer(dataSerializer).serialize(encoder, value.toList())
    override fun deserialize(decoder: Decoder) = ListSerializer(dataSerializer).deserialize(decoder).toMutableStateList()
}