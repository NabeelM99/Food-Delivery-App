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

val YummyFoodiesFontFamily = FontFamily(
    Font(R.font.yummy_foodies_regular, FontWeight.Normal)
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

val customTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    )
)




val extendedTypography = AppTypography(
    headline = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 55.sp,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.Normal
    ),
    titleMedium = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 25.sp,
        fontWeight = FontWeight.Normal
    ),
    titleSmall = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    body = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),
    label = TextStyle(
        fontFamily = YummyFoodiesFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Light
    ),

)





