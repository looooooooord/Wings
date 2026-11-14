package com.otaku.wings.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.otaku.wings.data.local.ThemeMode
import com.otaku.wings.data.local.ThemeState

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = LightPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkSecondary,
    onTertiary = DarkOnSecondary,
    tertiaryContainer = DarkSecondaryContainer,
    onTertiaryContainer = DarkOnSecondaryContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkPrimary,
    surfaceBright = DarkSurfaceContainerHighest,
    surfaceDim = DarkSurfaceContainerLowest,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = ReaderCanvas,
    inverseSurface = DarkOnSurface,
    inverseOnSurface = DarkSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = DarkPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightSecondary,
    onTertiary = LightOnSecondary,
    tertiaryContainer = LightSecondaryContainer,
    onTertiaryContainer = LightOnSecondaryContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightPrimary,
    surfaceBright = LightSurfaceContainerLowest,
    surfaceDim = LightSurfaceContainerHighest,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = ReaderCanvas,
    inverseSurface = LightOnSurface,
    inverseOnSurface = LightSurface,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

/** Semantic colours Material's ColorScheme has no slot for. */
val LocalWingsColors = staticCompositionLocalOf { LightExtendedColors }

@Composable
internal fun WingsThemeForPreview(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalWingsColors provides if (dark) DarkExtendedColors else LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = WingsTypography,
            shapes = WingsShapes,
            content = content
        )
    }
}

@Composable
fun WingsTheme(content: @Composable () -> Unit) {
    val mode by ThemeState.mode.collectAsState()
    val useDark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(
        LocalWingsColors provides if (useDark) DarkExtendedColors else LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = if (useDark) DarkScheme else LightScheme,
            typography = WingsTypography,
            shapes = WingsShapes,
            content = content
        )
    }
}
