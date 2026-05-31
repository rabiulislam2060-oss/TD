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
    primary = CleanPrimaryDark,
    secondary = CleanOutputTextDark,
    tertiary = CleanMutedDark,
    background = CleanBgDark,
    surface = CleanSurfaceDark,
    onPrimary = Color(0xFF001D36),
    onSecondary = Color(0xFF001D36),
    onTertiary = Color.White,
    onBackground = CleanTextDark,
    onSurface = CleanTextDark,
    surfaceVariant = CleanCardInputDark,
    onSurfaceVariant = CleanMutedDark
)

private val LightColorScheme = lightColorScheme(
    primary = CleanPrimaryLight,
    secondary = CleanOutputTextLight,
    tertiary = CleanMutedLight,
    background = CleanBgLight,
    surface = CleanSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = CleanTextLight,
    onSurface = CleanTextLight,
    surfaceVariant = CleanCardInputLight,
    onSurfaceVariant = CleanMutedLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize our signature beautiful colors
    content: @Composable () -> Unit,
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
