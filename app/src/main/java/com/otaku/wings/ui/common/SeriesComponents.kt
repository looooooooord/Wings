package com.otaku.wings.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.otaku.wings.R
import com.otaku.wings.data.model.Series
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.LocalWingsColors
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsShapeTokens
import com.otaku.wings.ui.theme.WingsType

/** Cover + title card used by home carousels and grids. Covers are always 2:3. */
@Composable
fun SeriesCard(
    series: Series,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(Dimens.CarouselCardWidth)
            .clip(WingsShapeTokens.Card)
            .clickable(onClick = onClick)
            .padding(Spacing.xxs)
    ) {
        CoverImage(
            coverUrl = series.coverUrl,
            // The title is rendered directly beneath, so the cover is decorative for TalkBack.
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(Dimens.CoverAspectRatio)
        )
        Text(
            text = series.title,
            style = WingsType.seriesTitleCard,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Spacing.sm, start = Spacing.xxs, end = Spacing.xxs)
        )
        if (series.latestChapter.isNotBlank()) {
            Text(
                text = series.latestChapter,
                style = WingsType.metadataSecondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.xxs, start = Spacing.xxs, end = Spacing.xxs)
            )
        }
    }
}

/**
 * Full-width row used by search, library and category lists.
 * Selection is exposed to accessibility services when [selectable].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeriesListItem(
    series: Series,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectable: Boolean = false,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val colors = LocalWingsColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.TouchTarget)
            .background(if (selected) colors.selectedTint else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .then(if (selectable) Modifier.semantics { this.selected = selected } else Modifier)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            CoverImage(
                coverUrl = series.coverUrl,
                contentDescription = null,
                modifier = Modifier.size(Dimens.ListCoverWidth, Dimens.ListCoverHeight)
            )
            if (selectable) {
                Box(
                    modifier = Modifier
                        .padding(Spacing.xs)
                        .size(Dimens.IconInline)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(Spacing.md)
                        )
                    }
                }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(
                text = series.title,
                style = WingsType.seriesTitleList,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (series.author.isNotBlank()) {
                Text(
                    text = series.author,
                    style = WingsType.metadataSecondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (series.latestChapter.isNotBlank()) {
                Text(
                    text = series.latestChapter,
                    style = WingsType.metadataSecondary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Horizontal section: heading, "show more" action and a keyed lazy carousel. */
@Composable
fun SeriesCarouselSection(
    title: String,
    items: List<Series>,
    onItemClick: (Series) -> Unit,
    onMoreClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(modifier = modifier) {
        SectionHeader(title = title, onMoreClick = onMoreClick)
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(items.take(10), key = { it.id }) { series ->
                SeriesCard(series = series, onClick = { onItemClick(series) })
            }
        }
    }
}

/** Thin divider with the theme's hairline colour. */
@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        thickness = Dimens.DividerThickness,
        color = MaterialTheme.colorScheme.outline
    )
}

/** Accessibility helper for cover descriptions. */
@Composable
fun coverDescription(title: String): String = stringResource(R.string.detail_cover_description, title)
