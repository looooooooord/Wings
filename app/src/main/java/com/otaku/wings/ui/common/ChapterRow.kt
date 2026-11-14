package com.otaku.wings.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.otaku.wings.R
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.LocalWingsColors
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType

/**
 * One chapter line: title, optional date, read state and last-read marker.
 * The marker is a thin accent bar at the start edge plus a tinted background.
 */
@Composable
fun ChapterRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    date: String = "",
    isRead: Boolean = false,
    isLastRead: Boolean = false
) {
    val colors = LocalWingsColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.ChapterRowMinHeight)
            .background(if (isLastRead) colors.lastReadTint else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(Dimens.ChapterRowMinHeight)
                .background(
                    if (isLastRead) MaterialTheme.colorScheme.primary
                    else androidx.compose.ui.graphics.Color.Transparent
                )
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRead) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.detail_chapter_read),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.IconInline)
                )
            }
            Text(
                text = title,
                style = WingsType.chapterTitle,
                color = when {
                    isLastRead -> MaterialTheme.colorScheme.primary
                    isRead -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isLastRead) {
                Text(
                    text = stringResource(R.string.detail_chapter_last_read),
                    style = WingsType.label,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (date.isNotBlank()) {
                Text(
                    text = date,
                    style = WingsType.metadataSecondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
