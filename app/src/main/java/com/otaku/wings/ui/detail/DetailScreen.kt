package com.otaku.wings.ui.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.otaku.wings.R
import com.otaku.wings.data.model.Chapter
import com.otaku.wings.data.model.Series
import com.otaku.wings.data.model.displayTitle
import com.otaku.wings.ui.common.ChapterRow
import com.otaku.wings.ui.common.CoverImage
import com.otaku.wings.ui.common.EmptyState
import com.otaku.wings.ui.common.ErrorState
import com.otaku.wings.ui.common.HairlineDivider
import com.otaku.wings.ui.common.LoadingState
import com.otaku.wings.ui.common.MetaChip
import com.otaku.wings.ui.common.PreviewData
import com.otaku.wings.ui.common.WingsPreview
import com.otaku.wings.ui.common.WingsTopBar
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType

@Composable
fun DetailScreen(
    seriesUrl: String,
    onBack: () -> Unit,
    onChapterClick: (Chapter, Series) -> Unit
) {
    val app = LocalContext.current.applicationContext as android.app.Application
    val viewModel: DetailViewModel = viewModel(
        key = seriesUrl,
        factory = viewModelFactory {
            initializer { DetailViewModel(app, seriesUrl) }
        }
    )
    val state by viewModel.state.collectAsState()

    DetailContent(
        state = state,
        onBack = onBack,
        onToggleBookmark = viewModel::toggleBookmark,
        onChapterClick = onChapterClick,
        onRetry = viewModel::retry
    )
}

/** Screen body without the ViewModel. */
@Composable
fun DetailContent(
    state: DetailUiState,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onChapterClick: (Chapter, Series) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val collapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 120
        }
    }
    val barColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (collapsed) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(200),
        label = "detail-top-bar"
    )
    val title = state.series?.title.orEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingState()

            state.error != null && state.series == null -> ErrorState(
                kind = state.error,
                onRetry = onRetry
            )

            state.series != null -> DetailBody(
                state = state,
                listState = listState,
                onToggleBookmark = onToggleBookmark,
                onChapterClick = onChapterClick
            )

            else -> ErrorState(kind = com.otaku.wings.ui.common.ErrorKind.GENERIC, onRetry = onRetry)
        }

        WingsTopBar(
            title = title,
            onBack = onBack,
            containerColor = barColor,
            showTitle = collapsed,
            titleColor = MaterialTheme.colorScheme.onSurface,
            navigationIconColor = if (collapsed) MaterialTheme.colorScheme.onSurface else Color.White
        )
    }
}

@Composable
private fun DetailBody(
    state: DetailUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onToggleBookmark: () -> Unit,
    onChapterClick: (Chapter, Series) -> Unit
) {
    val series = state.series ?: return
    val primary = remember(state.chapters, state.lastReadChapterUrl) {
        resolvePrimaryAction(state.chapters, state.lastReadChapterUrl)
    }
    var ascending by rememberSaveable { mutableStateOf(false) }
    val orderedChapters = remember(state.chapters, ascending) {
        if (ascending) state.chapters.reversed() else state.chapters
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = Spacing.xxl)
    ) {
        item(key = "header") {
            DetailHeader(
                series = series,
                isBookmarked = state.isBookmarked,
                primary = primary,
                onToggleBookmark = onToggleBookmark,
                onPrimaryClick = { primary.chapter?.let { onChapterClick(it, series) } }
            )
        }

        item(key = "chapters-header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.xxl, bottom = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.detail_chapter_count,
                        state.chapters.size,
                        state.chapters.size
                    ),
                    style = WingsType.sectionHeading,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (state.chapters.isNotEmpty()) {
                    IconButton(onClick = { ascending = !ascending }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.detail_sort_content_description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = stringResource(
                    if (ascending) R.string.detail_sort_ascending else R.string.detail_sort_descending
                ),
                style = WingsType.metadataSecondary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )
            HairlineDivider(modifier = Modifier.padding(top = Spacing.sm))
        }

        if (orderedChapters.isEmpty()) {
            item(key = "no-chapters") {
                Column(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    EmptyState(
                        title = stringResource(R.string.detail_no_chapters),
                        message = stringResource(R.string.detail_no_chapters_body)
                    )
                }
            }
        } else {
            items(orderedChapters, key = { it.url }, contentType = { "chapter" }) { chapter ->
                ChapterRow(
                    title = chapter.displayTitle(series.title),
                    date = chapter.date,
                    isRead = chapter.isRead,
                    isLastRead = chapter.url == state.lastReadChapterUrl,
                    onClick = { onChapterClick(chapter, series) }
                )
                HairlineDivider()
            }
        }
    }
}

@Composable
private fun DetailHeader(
    series: Series,
    isBookmarked: Boolean,
    primary: PrimaryAction,
    onToggleBookmark: () -> Unit,
    onPrimaryClick: () -> Unit
) {
    var synopsisExpanded by rememberSaveable { mutableStateOf(false) }
    var synopsisOverflows by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Subtle backdrop: the cover, heavily dimmed by the surface colour, never a loud gradient.
        CoverImage(
            coverUrl = series.coverUrl,
            contentDescription = null,
            shape = RectangleShape,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                CoverImage(
                    coverUrl = series.coverUrl,
                    contentDescription = stringResource(R.string.detail_cover_description, series.title),
                    modifier = Modifier.size(Dimens.DetailCoverWidth, Dimens.DetailCoverHeight)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = series.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (series.author.isNotBlank()) {
                        Text(
                            text = series.author,
                            style = WingsType.metadataPrimary,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (series.genre.isNotBlank()) {
                        MetaChip(text = series.genre)
                    }
                    if (series.latestChapter.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.detail_latest_chapter, series.latestChapter),
                            style = WingsType.metadataSecondary,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Button(
                    onClick = onPrimaryClick,
                    enabled = primary.chapter != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.ButtonHeight)
                ) {
                    Text(
                        text = stringResource(
                            if (primary.isContinue) R.string.detail_continue else R.string.detail_start
                        ),
                        style = WingsType.button
                    )
                }
                OutlinedButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.ButtonHeight)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.xl)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        text = stringResource(
                            if (isBookmarked) R.string.detail_bookmarked else R.string.detail_bookmark
                        ),
                        style = WingsType.button,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (series.synopsis.isNotBlank()) {
                Text(
                    text = series.synopsis,
                    style = WingsType.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (synopsisExpanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { result ->
                        if (!synopsisExpanded) synopsisOverflows = result.hasVisualOverflow
                    },
                    modifier = Modifier
                        .padding(top = Spacing.lg)
                        .animateContentSize()
                )
                if (synopsisExpanded || synopsisOverflows) {
                    TextButton(
                        onClick = { synopsisExpanded = !synopsisExpanded },
                        contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = Spacing.xxs)
                    ) {
                        Text(
                            text = stringResource(
                                if (synopsisExpanded) R.string.action_show_less else R.string.action_show_more
                            ),
                            style = WingsType.label,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Detail light", heightDp = 800, widthDp = 400)
@Composable
private fun DetailPreview() {
    WingsPreview {
        DetailContent(
            state = DetailUiState(
                series = PreviewData.series,
                chapters = previewChapters(),
                isBookmarked = true,
                lastReadChapterUrl = "c3",
                isLoading = false
            ),
            onBack = {},
            onToggleBookmark = {},
            onChapterClick = { _, _ -> },
            onRetry = {}
        )
    }
}

@Preview(name = "Detail loading", heightDp = 800, widthDp = 400)
@Composable
private fun DetailLoadingPreview() {
    WingsPreview {
        DetailContent(
            state = DetailUiState(isLoading = true),
            onBack = {},
            onToggleBookmark = {},
            onChapterClick = { _, _ -> },
            onRetry = {}
        )
    }
}

internal fun previewChapters(): List<Chapter> = listOf(
    Chapter(id = "c5", seriesId = "s1", number = "5", title = "الفصل 5", date = "12/03", url = "c5", isRead = false),
    Chapter(id = "c4", seriesId = "s1", number = "4", title = "الفصل 4", date = "05/03", url = "c4", isRead = false),
    Chapter(id = "c3", seriesId = "s1", number = "3", title = "الفصل 3", date = "26/02", url = "c3", isRead = true)
)
