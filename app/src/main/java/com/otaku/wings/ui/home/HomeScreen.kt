package com.otaku.wings.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otaku.wings.R
import com.otaku.wings.data.model.Series
import com.otaku.wings.ui.common.EmptyState
import com.otaku.wings.ui.common.ErrorState
import com.otaku.wings.ui.common.InlineLoading
import com.otaku.wings.ui.common.InlineMessageBanner
import com.otaku.wings.ui.common.LoadingState
import com.otaku.wings.ui.common.PreviewData
import com.otaku.wings.ui.common.SeriesCarouselSection
import com.otaku.wings.ui.common.TabHeader
import com.otaku.wings.ui.common.WingsPreview
import com.otaku.wings.ui.common.WingsPreviewDark
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSeriesClick: (Series) -> Unit,
    onMoreClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabHeader(
            title = stringResource(R.string.app_name),
            actions = {
                if (state.isLoading) {
                    InlineLoading(modifier = Modifier.size(Dimens.TouchTarget))
                } else {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

        PullToRefreshBox(
            isRefreshing = state.isLoading && state.hasContent,
            onRefresh = { viewModel.refresh() },
            state = rememberPullToRefreshState(),
            modifier = Modifier.fillMaxSize()
        ) {
            HomeContent(
                state = state,
                onSeriesClick = onSeriesClick,
                onMoreClick = onMoreClick,
                onRetry = { viewModel.refresh() }
            )
        }
    }
}

/** Screen body without the ViewModel, so previews and tests can drive it directly. */
@Composable
fun HomeContent(
    state: HomeUiState,
    onSeriesClick: (Series) -> Unit,
    onMoreClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    when {
        state.isLoading && !state.hasAnySection -> LoadingState()
        state.error != null && !state.hasAnySection -> ErrorState(
            kind = state.error,
            onRetry = onRetry
        )

        !state.hasAnySection -> EmptyState(
            title = stringResource(R.string.home_empty_title),
            message = stringResource(R.string.home_empty_body),
            icon = Icons.Outlined.Explore,
            actionLabel = stringResource(R.string.action_refresh),
            onAction = onRetry
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = Spacing.xxl)
        ) {
            if (state.error != null) {
                item(key = "partial-error") {
                    InlineMessageBanner(
                        text = stringResource(R.string.error_generic_body),
                        actionLabel = stringResource(R.string.action_retry),
                        onAction = onRetry
                    )
                }
            }
            if (state.recent.isNotEmpty()) {
                item(key = "recent") {
                    SeriesCarouselSection(
                        title = stringResource(R.string.section_recent),
                        items = state.recent,
                        onItemClick = onSeriesClick,
                        onMoreClick = { onMoreClick("recent") }
                    )
                }
            }
            if (state.latest.isNotEmpty()) {
                item(key = "latest") {
                    SeriesCarouselSection(
                        title = stringResource(R.string.section_latest),
                        items = state.latest,
                        onItemClick = onSeriesClick,
                        onMoreClick = { onMoreClick("latest") }
                    )
                }
            }
            if (state.popular.isNotEmpty()) {
                item(key = "popular") {
                    SeriesCarouselSection(
                        title = stringResource(R.string.section_popular),
                        items = state.popular,
                        onItemClick = onSeriesClick,
                        onMoreClick = { onMoreClick("popular") }
                    )
                }
            }
            if (state.bookmarked.isNotEmpty()) {
                item(key = "bookmarked") {
                    SeriesCarouselSection(
                        title = stringResource(R.string.section_bookmarked),
                        items = state.bookmarked,
                        onItemClick = onSeriesClick,
                        onMoreClick = { onMoreClick("bookmarked") }
                    )
                }
            }
        }
    }
}

@Preview(name = "Home light", heightDp = 720, widthDp = 400)
@Composable
private fun HomeContentPreview() {
    WingsPreview {
        HomeContent(
            state = HomeUiState(
                latest = PreviewData.seriesList,
                popular = PreviewData.seriesList.reversed(),
                bookmarked = listOf(PreviewData.series),
                recent = PreviewData.seriesList
            ),
            onSeriesClick = {},
            onMoreClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Home dark empty", heightDp = 720, widthDp = 400)
@Composable
private fun HomeEmptyPreview() {
    WingsPreviewDark {
        HomeContent(
            state = HomeUiState(),
            onSeriesClick = {},
            onMoreClick = {},
            onRetry = {}
        )
    }
}
