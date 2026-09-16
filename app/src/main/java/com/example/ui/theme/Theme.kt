package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val DarkColorScheme =
    darkColorScheme(
        primary = AppleBlueDarkTheme,
        onPrimary = AppleLabelPrimaryDark,
        primaryContainer = AppleCardVariantDarkContainer(),
        onPrimaryContainer = AppleBlueDarkTheme,
        secondary = AppleGreenDarkTheme,
        onSecondary = AppleLabelPrimaryDark,
        secondaryContainer = AppleCardVariantDarkContainer(),
        onSecondaryContainer = AppleGreenDarkTheme,
        tertiary = AppleIndigoDarkTheme,
        background = AppleSystemBackgroundDark,
        onBackground = AppleLabelPrimaryDark,
        surface = AppleCardSurfaceDark,
        onSurface = AppleLabelPrimaryDark,
        surfaceVariant = AppleCardSurfaceVariantDark,
        onSurfaceVariant = AppleLabelSecondaryDark,
        outline = AppleCardBorderDark,
        outlineVariant = AppleLabelQuaternaryDark
    )

private val LightColorScheme =
    lightColorScheme(
        primary = AppleBlue,
        onPrimary = AppleCardSurface,
        primaryContainer = AppleBlueLight,
        onPrimaryContainer = AppleBlueDark,
        secondary = AppleDarkSlate,
        onSecondary = AppleCardSurface,
        secondaryContainer = AppleGreenLight,
        onSecondaryContainer = AppleGreen,
        tertiary = AppleIndigo,
        tertiaryContainer = AppleIndigoLight,
        background = AppleSystemBackground,
        onBackground = AppleLabelPrimary,
        surface = AppleCardSurface,
        onSurface = AppleLabelPrimary,
        surfaceVariant = AppleCardSurfaceVariant,
        onSurfaceVariant = AppleLabelSecondary,
        outline = AppleCardBorder,
        outlineVariant = AppleCardBorderSubtle
    )

private fun AppleCardVariantDarkContainer() = AppleCardSurfaceVariantDark

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
