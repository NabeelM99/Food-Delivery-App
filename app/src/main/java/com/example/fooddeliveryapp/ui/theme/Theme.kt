package com.example.fooddeliveryapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val customTypography = Typography(
        headlineMedium = TextStyle(
            fontFamily = YummyFoodiesFontFamily,
            fontWeight = FontWeight.Bold
        ),
        bodyLarge = TextStyle(
            fontFamily = YummyFoodiesFontFamily,
            fontWeight = FontWeight.Normal
        )

    )

    CompositionLocalProvider(
        LocalAppColors provides extendedColors
    ) {
        MaterialTheme(
            typography = customTypography,
            content = content
        )
    }
}


object AppTheme{
    val colors: AppColors
    @Composable
    get() = LocalAppColors.current
    val typography: AppTypography
    @Composable
    get() = LocalAppTypography.current
}