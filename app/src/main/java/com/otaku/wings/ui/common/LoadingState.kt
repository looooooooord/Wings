package com.otaku.wings.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing

/** Centered, low-noise progress indicator used for whole-screen loads. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.size(Spacing.xxxl),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** Compact inline progress for a single row or area. */
@Composable
fun InlineLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.size(Dimens.IconInline),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
