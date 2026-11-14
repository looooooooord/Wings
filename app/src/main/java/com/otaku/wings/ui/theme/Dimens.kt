package com.otaku.wings.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The only spacing scale used by the app: 2, 4, 8, 12, 16, 20, 24, 32, 48.
 * Screens must not invent their own dp paddings.
 */
object Spacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val huge: Dp = 48.dp
}

/** Layout and component sizes shared across screens. */
object Dimens {
    val ScreenHorizontalPadding: Dp = 16.dp
    val SectionSpacing: Dp = 24.dp
    val ItemSpacing: Dp = 12.dp
    val CardInnerPadding: Dp = 12.dp

    // Covers are always 2:3.
    const val CoverAspectRatio: Float = 2f / 3f
    val CarouselCardWidth: Dp = 112.dp
    val ListCoverWidth: Dp = 72.dp
    val ListCoverHeight: Dp = 108.dp
    val GridMinCell: Dp = 112.dp
    val DetailCoverWidth: Dp = 120.dp
    val DetailCoverHeight: Dp = 180.dp

    val Icon: Dp = 24.dp
    val IconInline: Dp = 20.dp
    val TouchTarget: Dp = 48.dp
    val ButtonHeight: Dp = 48.dp
    val ButtonHeightCompact: Dp = 40.dp
    val SearchFieldHeight: Dp = 52.dp
    val ChipHeight: Dp = 32.dp
    val ProgressHeight: Dp = 3.dp
    val DividerThickness: Dp = 0.5.dp

    val SettingsRowMinHeight: Dp = 56.dp
    val ChapterRowMinHeight: Dp = 56.dp

    /** Keeps long-form text readable on tablets. */
    val MaxContentWidth: Dp = 600.dp
}
