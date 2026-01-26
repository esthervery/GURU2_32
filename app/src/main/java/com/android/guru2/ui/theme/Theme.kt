package com.android.guru2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 색상 정의
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFF0724A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF9F5),
    onPrimaryContainer = Color(0xFF1C0E09),
    secondary = Color(0xFF999999),
    onSecondary = Color.White,
    background = Color(0xFFFFFCFB),
    onBackground = Color(0xFF1C0E09),
    surface = Color.White,
    onSurface = Color(0xFF1C0E09),
    error = Color(0xFFFF0000),
    onError = Color.White
)

@Composable
fun Guru2Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}