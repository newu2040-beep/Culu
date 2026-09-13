package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = ObsidianDark,
    primaryContainer = DeepOcean,
    onPrimaryContainer = CrystalIce,
    secondary = AzureBlue,
    onSecondary = Color.White,
    secondaryContainer = MidnightNavy,
    onSecondaryContainer = CrystalIce,
    background = ObsidianDark,
    onBackground = Color.White,
    surface = DarkSurfaceGlass,
    onSurface = Color.White,
    surfaceVariant = MidnightNavy,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = AzureBlue,
    onPrimary = Color.White,
    primaryContainer = CrystalIce,
    onPrimaryContainer = DeepOcean,
    secondary = CyanGlow,
    onSecondary = ObsidianDark,
    secondaryContainer = Color(0xFFF0F9FF),
    onSecondaryContainer = Color(0xFF0C4A6E),
    background = FrostedWhite,
    onBackground = Color(0xFF0F172A),
    surface = LightSurfaceGlass,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun CuluTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Liquid glass looks most cohesive with custom physical glass tokens
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for any existing preview/test
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CuluTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
