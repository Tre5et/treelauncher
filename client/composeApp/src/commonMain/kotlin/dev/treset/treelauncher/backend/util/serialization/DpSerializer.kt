package dev.treset.treelauncher.backend.util.serialization

import androidx.compose.ui.unit.Dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias DpData = @Serializable(with = DpSerializer::class) Dp

class DpSerializer : KSerializer<Dp> {
    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.FLOAT)

    override fun serialize(encoder: Encoder, value: Dp) {
        encoder.encodeFloat(value.value)
    }

    override fun deserialize(decoder: Decoder): Dp {
        return Dp(decoder.decodeFloat())
    }
}