package com.example.fooddeliveryapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Orange = Color (0xFFFFA500)
val Green = Color (0xFF84A59D)
val Yellow = Color (0xFFF7EDE2)
val YellowLight = Color (0xFFFFFFF2)
val Dark = Color (0xFF3D405B)
val Red = Color(0xFFFF6347)

@Immutable
data class AppColors (
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val secondarySurface: Color,
    val onSecondarySurface: Color,
    val regularSurface: Color,
    val onRegularSurface: Color,
    val actionSurface: Color,
    val onActionSurface: Color,
    val highlightSurface: Color,
    val onHighLightSurface: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        background = Color.Unspecified,
        onBackground = Color.Unspecified,
        surface = Color.Unspecified,
        onSurface = Color.Unspecified,
        secondarySurface = Color.Unspecified,
        onSecondarySurface = Color.Unspecified,
        regularSurface = Color.Unspecified,
        onRegularSurface = Color.Unspecified,
        actionSurface = Color.Unspecified,
        onActionSurface = Color.Unspecified,
        highlightSurface = Color.Unspecified,
        onHighLightSurface = Color.Unspecified,
    )
}

val extendedColors = AppColors(
    background = Color.White,
    onBackground = Dark,
    surface = Color.White,
    onSurface = Dark,
    secondarySurface = Orange,
    onSecondarySurface = Color.White,
    regularSurface = YellowLight,
    onRegularSurface = Dark,
    actionSurface = Yellow,
    onActionSurface = Orange,
    highlightSurface = Green,
    onHighLightSurface = Color.White
)












