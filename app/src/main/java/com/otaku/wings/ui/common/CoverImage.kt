package com.otaku.wings.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.WingsShapeTokens

/**
 * The single cover renderer for the whole app.
 *
 * Shows a neutral placeholder while loading and an icon fallback when the URL is missing or the
 * image fails, so a card never collapses or flashes empty.
 *
 * @param contentDescription pass null when the series title is rendered right next to the cover,
 * to avoid a duplicate TalkBack announcement.
 */
@Composable
fun CoverImage(
    coverUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = WingsShapeTokens.Cover
) {
    val container = MaterialTheme.colorScheme.surfaceContainerHigh
    Box(
        modifier = modifier
            .clip(shape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        if (coverUrl.isBlank()) {
            CoverFallback()
        } else {
            SubcomposeAsyncImage(
                model = coverUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(Dimens.IconInline),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                },
                error = { CoverFallback() }
            )
        }
    }
}

@Composable
private fun CoverFallback() {
    Icon(
        imageVector = Icons.Outlined.Image,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(Dimens.Icon)
    )
}
