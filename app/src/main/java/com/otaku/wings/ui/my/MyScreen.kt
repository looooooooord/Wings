package com.otaku.wings.ui.my

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otaku.wings.R
import com.otaku.wings.data.model.Series
import com.otaku.wings.ui.common.EmptyState
import com.otaku.wings.ui.common.HairlineDivider
import com.otaku.wings.ui.common.PreviewData
import com.otaku.wings.ui.common.SeriesListItem
import com.otaku.wings.ui.common.TabHeader
import com.otaku.wings.ui.common.WingsPreview
import com.otaku.wings.ui.common.WingsPreviewDark
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType

@Composable
fun MyScreen(
    onSeriesClick: (Series) -> Unit,
    onExploreClick: () -> Unit = {},
    viewModel: MyViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    MyContent(
        state = state,
        onSeriesClick = onSeriesClick,
        onExploreClick = onExploreClick
    )
}

/** Screen body without the ViewModel. Bookmarks and history stay separate. */
@Composable
fun MyContent(
    state: MyUiState,
    onSeriesClick: (Series) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val bookmarksState = rememberLazyListState()
    val recentState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize()) {
        TabHeader(title = stringResource(R.string.library_title))
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { positions ->
                if (selectedTab < positions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(positions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            divider = { HairlineDivider() }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = stringResource(R.string.section_bookmarked),
                        style = WingsType.label
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = stringResource(R.string.section_recent),
                        style = WingsType.label
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (selectedTab == 0) {
            if (state.bookmarked.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.library_empty_bookmarks_title),
                    message = stringResource(R.string.library_empty_bookmarks_body),
                    icon = Icons.Outlined.Bookmarks,
                    actionLabel = stringResource(R.string.library_empty_action),
                    onAction = onExploreClick
                )
            } else {
                SeriesList(
                    items = state.bookmarked,
                    listState = bookmarksState,
                    onSeriesClick = onSeriesClick
                )
            }
        } else {
            if (state.recent.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.library_empty_recent_title),
                    message = stringResource(R.string.library_empty_recent_body),
                    icon = Icons.Outlined.History,
                    actionLabel = stringResource(R.string.library_empty_action),
                    onAction = onExploreClick
                )
            } else {
                SeriesList(
                    items = state.recent,
                    listState = recentState,
                    onSeriesClick = onSeriesClick
                )
            }
        }
    }
}

@Composable
private fun SeriesList(
    items: List<Series>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSeriesClick: (Series) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = Spacing.sm, horizontal = Spacing.xs)
    ) {
        items(items, key = { it.id }, contentType = { "series" }) { series ->
            SeriesListItem(series = series, onClick = { onSeriesClick(series) })
            HairlineDivider()
        }
    }
}

@Preview(name = "Library light", heightDp = 700, widthDp = 400)
@Composable
private fun MyContentPreview() {
    WingsPreview {
        MyContent(
            state = MyUiState(
                bookmarked = PreviewData.seriesList,
                recent = PreviewData.seriesList
            ),
            onSeriesClick = {},
            onExploreClick = {}
        )
    }
}

@Preview(name = "Library dark empty", heightDp = 700, widthDp = 400)
@Composable
private fun MyEmptyPreview() {
    WingsPreviewDark {
        MyContent(state = MyUiState(), onSeriesClick = {}, onExploreClick = {})
    }
}
