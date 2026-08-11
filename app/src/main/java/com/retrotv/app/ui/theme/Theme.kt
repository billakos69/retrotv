package com.retrotv.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val RetroColorScheme = darkColorScheme(
    primary = TvAccentGreen,
    secondary = TvAccentAmber,
    background = TvBackground,
    surface = TvSurface,
    onBackground = TvTextPrimary,
    onSurface = TvTextPrimary
)

@Composable
fun RetroTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = RetroTypography,
        content = content
    )
}
