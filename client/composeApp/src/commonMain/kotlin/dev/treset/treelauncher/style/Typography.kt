package dev.treset.treelauncher.style

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import treelauncher.composeapp.generated.resources.*

val Exo2Font: FontFamily
    @Composable get() = FontFamily(
    Font(Res.font.Exo2_ExtraBold, FontWeight.ExtraBold),
    Font(Res.font.Exo2_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(Res.font.Exo2_ExtraLight, FontWeight.ExtraLight),
    Font(Res.font.Exo2_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.Exo2_Italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.Exo2_Light, FontWeight.Light),
    Font(Res.font.Exo2_LightItalic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.Exo2_Medium, FontWeight.Medium),
    Font(Res.font.Exo2_MediumItalic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.Exo2_Regular, FontWeight.Normal),
    Font(Res.font.Exo2_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Exo2_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.Exo2_Thin, FontWeight.Thin),
    Font(Res.font.Exo2_ThinItalic, FontWeight.Thin, FontStyle.Italic),
)

val GgSansFont: FontFamily
    @Composable get() = FontFamily(
    Font(Res.font.GgSans_SemiBold, FontWeight.SemiBold),
    Font(Res.font.GgSans_Regular, FontWeight.Normal),
)

@Composable
fun typography() = Typography(
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
        ),
        labelMedium = TextStyle(
            fontFamily = Exo2Font,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
        )
    )
