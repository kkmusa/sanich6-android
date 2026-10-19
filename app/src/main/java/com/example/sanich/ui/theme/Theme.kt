package com.example.sanich.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = BrandBlack,
    onPrimary = Color.White,
    secondary = BrandOrange,
    onSecondary = BrandBlack,
    tertiary = BrandYellow,
    background = BrandBackground,
    onBackground = BrandBlack,
    surface = Color.White,
    onSurface = BrandBlack,
    surfaceVariant = Color(0xFFE4F5F2),
    onSurfaceVariant = BrandDark
)

@Composable
fun SanichTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
