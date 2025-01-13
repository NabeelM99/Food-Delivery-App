package com.example.fooddeliveryapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fooddeliveryapp.R

private val UnboundedFontFamily= FontFamily(
    Font(R.font.yummy_foodies_regular, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.yummy_foodies_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.yummy_foodies_regular, FontWeight.Light, FontStyle.Normal),
    )

@Immutable
data class AppTypography(
    val headline: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle
)

val LocalAppTypography = staticCompositionLocalOf {
    AppTypography(
        headline = TextStyle.Default,
        titleLarge = TextStyle.Default,
        titleMedium = TextStyle.Default,
        titleSmall = TextStyle.Default,
        body = TextStyle.Default,
        bodySmall = TextStyle.Default,
        label = TextStyle.Default
    )
}

val extendedTypography = AppTypography(
    headline = TextStyle(
        fontFamily = UnboundedFontFamily,
        fontSize = 55.sp,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = TextStyle(
        fontFamily = UnboundedFontFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.Normal
    ),
    titleMedium = TextStyle(
        fontFamily = UnboundedFontFamily,
        fontSize = 25.sp,
        fontWeight = FontWeight.Normal
    ),
    titleSmall = TextStyle(
        fontFamily = UnboundedFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    body = TextStyle(
        fontFamily = UnboundedFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontFamily = UnboundedFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),
    label = TextStyle(
        fontFamily = UnboundedFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Light
    ),
)





