package com.alish.securevault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaultDarkScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Sapphire900,
    primaryContainer = Sapphire700,
    onPrimaryContainer = NeonCyan,
    secondary = Sapphire500,
    onSecondary = Color.White,
    background = Sapphire900,
    surface = Sapphire800,
    surfaceVariant = Sapphire700,
    onSurface = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    outline = NeonCyanDim
)

@Composable
fun SecureVaultTheme(
    darkTheme: Boolean = true, // Always dark for premium feel
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VaultDarkScheme,
        typography = VaultTypography,
        content = content
    )
}

