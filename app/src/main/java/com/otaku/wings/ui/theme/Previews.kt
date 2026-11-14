package com.otaku.wings.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** Preview-only wrapper: forces the app theme and RTL layout without touching ThemeState. */
@Composable
fun WingsPreviewTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        WingsThemeForPreview(dark = dark, content = content)
    }
}
