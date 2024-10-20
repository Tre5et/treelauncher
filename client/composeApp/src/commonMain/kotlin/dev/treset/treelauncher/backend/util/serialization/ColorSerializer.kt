package dev.treset.treelauncher.backend.util.serialization

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias ColorData = @Serializable(with = ColorSerializer::class) Color

class ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        val string = "#${value.toArgb().toUInt().toString(16).padStart(8, '0').toUpperCase(Locale.current)}"
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Color {
        try {
            val long = decoder.decodeLong()
            return Color(long shr 32)
        } catch (e: Exception) {
            val string = decoder.decodeString().substring(1)
            return Color(string.toLong(16))
        }
    }
}