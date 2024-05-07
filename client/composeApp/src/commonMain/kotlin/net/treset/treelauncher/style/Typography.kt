package net.treset.treelauncher.style

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

val Exo2Font = FontFamily(
    Font("font/Exo2/Exo2-ExtraBold.ttf", FontWeight.ExtraBold),
    Font("font/Exo2/Exo2-ExtraBoldItalic.ttf", FontWeight.ExtraBold, FontStyle.Italic),
    Font("font/Exo2/Exo2-ExtraLight.ttf", FontWeight.ExtraLight),
    Font("font/Exo2/Exo2-ExtraLightItalic.ttf", FontWeight.ExtraLight, FontStyle.Italic),
    Font("font/Exo2/Exo2-Italic.ttf", FontWeight.Normal, FontStyle.Italic),
    Font("font/Exo2/Exo2-Light.ttf", FontWeight.Light),
    Font("font/Exo2/Exo2-LightItalic.ttf", FontWeight.Light, FontStyle.Italic),
    Font("font/Exo2/Exo2-Medium.ttf", FontWeight.Medium),
    Font("font/Exo2/Exo2-MediumItalic.ttf", FontWeight.Medium, FontStyle.Italic),
    Font("font/Exo2/Exo2-Regular.ttf", FontWeight.Normal),
    Font("font/Exo2/Exo2-SemiBold.ttf", FontWeight.SemiBold),
    Font("font/Exo2/Exo2-SemiBoldItalic.ttf", FontWeight.SemiBold, FontStyle.Italic),
    Font("font/Exo2/Exo2-Thin.ttf", FontWeight.Thin),
    Font("font/Exo2/Exo2-ThinItalic.ttf", FontWeight.Thin, FontStyle.Italic),
)

@Composable
fun typography() = rememberSaveable {
    Typography(
        titleLarge = TextStyle(
            fontFamily = Exo2Font,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp
        ),
        titleMedium = TextStyle(
            fontFamily = Exo2Font,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp
        ),
        titleSmall = TextStyle(
            fontFamily = Exo2Font,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = Exo2Font,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = Exo2Font,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
        )       ,
        labelMedium = TextStyle(
            fontFamily = Exo2Font,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
        )
    )
}
