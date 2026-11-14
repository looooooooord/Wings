package com.otaku.wings.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Single source of truth for every colour in the app.
 *
 * Brand blue is an accent only. Surfaces are neutral greys so covers stay the visual hero.
 * Values below were contrast-checked (see docs/DESIGN_NOTES.md) against their intended role.
 */

// ── Dark palette ─────────────────────────────────────────────
internal val DarkBackground = Color(0xFF0F1012)
internal val DarkSurface = Color(0xFF16181B)
internal val DarkSurfaceContainer = Color(0xFF1C1F23)
internal val DarkSurfaceContainerHigh = Color(0xFF23272C)
internal val DarkSurfaceContainerHighest = Color(0xFF2A2F35)
internal val DarkSurfaceContainerLow = Color(0xFF141619)
internal val DarkSurfaceContainerLowest = Color(0xFF0B0C0E)
internal val DarkSurfaceVariant = Color(0xFF262A2F)
internal val DarkOutline = Color(0xFF2E3238)
internal val DarkOutlineVariant = Color(0xFF3A3F46)
internal val DarkOnSurface = Color(0xFFE6E8EB)
internal val DarkOnSurfaceVariant = Color(0xFFA3A9B1)
internal val DarkPrimary = Color(0xFF6AA6EB)
internal val DarkOnPrimary = Color(0xFF08192B)
internal val DarkPrimaryContainer = Color(0xFF1D2E42)
internal val DarkOnPrimaryContainer = Color(0xFFD3E4F8)
internal val DarkSecondary = Color(0xFFB6C2CF)
internal val DarkOnSecondary = Color(0xFF1A1D21)
internal val DarkSecondaryContainer = Color(0xFF24303C)
internal val DarkOnSecondaryContainer = Color(0xFFD6E3F2)
internal val DarkError = Color(0xFFFFB4AB)
internal val DarkOnError = Color(0xFF601410)
internal val DarkErrorContainer = Color(0xFF8C1D18)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)
internal val DarkSuccess = Color(0xFF7BD88F)
internal val DarkWarning = Color(0xFFF3C969)

// ── Light palette ────────────────────────────────────────────
internal val LightBackground = Color(0xFFF6F7F9)
internal val LightSurface = Color(0xFFFFFFFF)
internal val LightSurfaceContainer = Color(0xFFF0F2F5)
internal val LightSurfaceContainerHigh = Color(0xFFFFFFFF)
internal val LightSurfaceContainerHighest = Color(0xFFE8EBEF)
internal val LightSurfaceContainerLow = Color(0xFFFAFBFC)
internal val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
internal val LightSurfaceVariant = Color(0xFFE9ECF0)
internal val LightOutline = Color(0xFFE1E4E8)
internal val LightOutlineVariant = Color(0xFFEDEFF2)
internal val LightOnSurface = Color(0xFF14171A)
internal val LightOnSurfaceVariant = Color(0xFF555B63)
// Slightly deeper than the dark-theme tint so accent text passes 4.5:1 on surfaceContainer.
internal val LightPrimary = Color(0xFF1D67C6)
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFFDCE9FA)
internal val LightOnPrimaryContainer = Color(0xFF0C2A4C)
internal val LightSecondary = Color(0xFF4C5561)
internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFE4E8EE)
internal val LightOnSecondaryContainer = Color(0xFF1B2027)
internal val LightError = Color(0xFFB3261E)
internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFF9DEDC)
internal val LightOnErrorContainer = Color(0xFF410E0B)
internal val LightSuccess = Color(0xFF1B6E45)
internal val LightWarning = Color(0xFF8A5A00)

// ── Reader palette ───────────────────────────────────────────
// The reader canvas is intentionally pure black in both themes.
val ReaderCanvas = Color(0xFF000000)
val ReaderScrim = Color(0xCC000000)
val ReaderOnScrim = Color(0xFFF2F4F6)
val ReaderOnScrimMuted = Color(0xFFB9C0C8)
// Reader accent stays light in both themes because it always sits on the black canvas.
val ReaderAccent = Color(0xFF6AA6EB)

// ── Semantic colours not covered by ColorScheme ──────────────
data class WingsExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val lastReadTint: Color,
    val selectedTint: Color
)

internal val DarkExtendedColors = WingsExtendedColors(
    success = DarkSuccess,
    onSuccess = Color(0xFF05230F),
    successContainer = Color(0xFF173525),
    warning = DarkWarning,
    onWarning = Color(0xFF2B1F00),
    warningContainer = Color(0xFF3A2E10),
    lastReadTint = Color(0x246AA6EB),
    selectedTint = Color(0x1F6AA6EB)
)

internal val LightExtendedColors = WingsExtendedColors(
    success = LightSuccess,
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFDCF2E5),
    warning = LightWarning,
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFBEED2),
    lastReadTint = Color(0x1A1D67C6),
    selectedTint = Color(0x141D67C6)
)
