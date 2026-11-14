package com.otaku.wings.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Single place to swap the app font. Drop an Arabic-capable font into res/font and replace
 * `FontFamily.Default` here — nothing else has to change.
 */
val WingsFontFamily: FontFamily = FontFamily.Default

private val Trim = LineHeightStyle.Trim.None

private fun arabicStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = WingsFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    // Never clip Arabic diacritics or tall glyphs.
    platformStyle = PlatformTextStyle(includeFontPadding = true),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = Trim
    )
)

/** Material type scale. Arabic body copy runs at 1.5x+ line height. */
val WingsTypography = Typography(
    displaySmall = arabicStyle(30, 42, FontWeight.SemiBold),
    headlineMedium = arabicStyle(26, 36, FontWeight.SemiBold),
    headlineSmall = arabicStyle(24, 34, FontWeight.SemiBold),
    titleLarge = arabicStyle(22, 31, FontWeight.SemiBold),
    titleMedium = arabicStyle(16, 24, FontWeight.SemiBold),
    titleSmall = arabicStyle(14, 21, FontWeight.Medium),
    bodyLarge = arabicStyle(16, 26),
    bodyMedium = arabicStyle(14, 22),
    bodySmall = arabicStyle(12, 19),
    labelLarge = arabicStyle(14, 20, FontWeight.Medium),
    labelMedium = arabicStyle(12, 18, FontWeight.Medium),
    labelSmall = arabicStyle(12, 17, FontWeight.Medium)
)

/**
 * Named app styles. Screens reference these instead of raw sizes so the hierarchy stays
 * consistent across every surface.
 */
object WingsType {
    val screenTitle = arabicStyle(24, 33, FontWeight.SemiBold)
    val sectionHeading = arabicStyle(17, 25, FontWeight.SemiBold)
    val seriesTitleCard = arabicStyle(13, 19, FontWeight.Medium)
    val seriesTitleList = arabicStyle(15, 22, FontWeight.Medium)
    val chapterTitle = arabicStyle(14, 21, FontWeight.Medium)
    val metadataPrimary = arabicStyle(13, 19)
    val metadataSecondary = arabicStyle(12, 18)
    val description = arabicStyle(15, 24)
    val button = arabicStyle(14, 20, FontWeight.Medium)
    val label = arabicStyle(12, 17, FontWeight.Medium)
    val settingsTitle = arabicStyle(15, 22)
    val settingsSupporting = arabicStyle(12, 18)
    val error = arabicStyle(14, 21)
    val emptyStateTitle = arabicStyle(17, 25, FontWeight.SemiBold)
    val emptyStateBody = arabicStyle(13, 20)
    val readerTitle = arabicStyle(15, 21, FontWeight.Medium)
    val readerMeta = arabicStyle(12, 18)
}
