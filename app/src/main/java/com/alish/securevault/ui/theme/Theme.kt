package com.alish.securevault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaultDarkScheme = darkColorScheme(
    primary = Color(0xFF8DB9FF),
    onPrimary = Color(0xFF05213F),
    primaryContainer = Color(0xFF13375F),
    background = Color(0xFF08111C),
    surface = Color(0xFF0F1A27),
    surfaceVariant = Color(0xFF162636),
    onSurface = Color(0xFFE8F0FF),
    onSurfaceVariant = Color(0xFFAAC0D7)
)

private val VaultLightScheme = lightColorScheme(
    primary = Color(0xFF145DA0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8FF),
    background = Color(0xFFF3F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EEF5),
    onSurface = Color(0xFF142032),
    onSurfaceVariant = Color(0xFF556579)
)

@Composable
fun SecureVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) VaultDarkScheme else VaultLightScheme,
        typography = VaultTypography,
        content = content
    )
}

