package dev.treset.treelauncher.util

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/* Derived from https://github.com/godaddy/compose-color-picker */

data class HsvColor(

    // from = 0.0, to = 360.0
    val hue: Float,

    // from = 0.0, to = 1.0
    val saturation: Float,

    // from = 0.0, to = 1.0
    val value: Float,
) {

    fun toColor(): Color {
        return Color.hsv(hue, saturation, value, 1f)
    }

    companion object {

        fun from(color: Color): HsvColor {
            val max = max(color.red, max(color.green, color.blue))
            val min = min(color.red, min(color.green, color.blue))

            val d = max - min
            val s = if(max == 0f) 0f else d / max

            var h = max
            if(max == min) {
                h = 0f
            } else {
                when(max){
                    color.red -> h = (color.green - color.blue) / d + (if(color.green < color.blue) 6 else 0)
                    color.green -> h = (color.blue - color.red) / d + 2
                    color.blue -> h = (color.red - color.green) / d + 4
                }
                h /= 6
            }

            return HsvColor(h * 360, s, max)
        }
    }
}