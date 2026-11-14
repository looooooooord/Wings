package com.otaku.wings.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otaku.wings.R
import com.otaku.wings.data.model.Series
import com.otaku.wings.ui.common.EmptyState
import com.otaku.wings.ui.common.ErrorState
import com.otaku.wings.ui.common.HairlineDivider
import com.otaku.wings.ui.common.LoadingState
import com.otaku.wings.ui.common.PreviewData
import com.otaku.wings.ui.common.SearchField
import com.otaku.wings.ui.common.SeriesListItem
import com.otaku.wings.ui.common.TabHeader
import com.otaku.wings.ui.common.WingsPreview
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing

@Composable
fun SearchScreen(
    onSeriesClick: (Series) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    SearchContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onClear = viewModel::clear,
        onRetry = viewModel::retry,
        onSeriesClick = onSeriesClick
    )
}

/** Screen body without the ViewModel. */
@Composable
fun SearchContent(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onSeriesClick: (Series) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize()) {
        TabHeader(title = stringResource(R.string.tab_explore))
        SearchField(
            value = state.query,
            onValueChange = onQueryChange,
            onSearch = onSearch,
            onClear = onClear,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainer
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // First run: nothing submitted yet.
                !state.hasSearched && !state.isLoading -> EmptyState(
                    title = stringResource(R.string.search_initial_title),
                    message = stringResource(R.string.search_initial_body),
                    icon = Icons.Outlined.Search
                )

                state.error != null -> ErrorState(kind = state.error, onRetry = onRetry)

                // A finished search with no hits must never render a blank screen.
                state.isEmptyResult -> EmptyState(
                    title = stringResource(R.string.search_no_results_title),
                    message = stringResource(
                        R.string.search_no_results_body,
                        state.query.trim()
                    ),
                    icon = Icons.Outlined.SearchOff
                )

                state.results.isEmpty() && state.isLoading -> LoadingState()

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = Spacing.xxl)
                ) {
                    items(state.results, key = { it.id }, contentType = { "series" }) { series ->
                        SeriesListItem(series = series, onClick = { onSeriesClick(series) })
                        HairlineDivider()
                    }
                }
            }
        }
    }
}

@Preview(name = "Search results", heightDp = 700, widthDp = 400)
@Composable
private fun SearchResultsPreview() {
    WingsPreview {
        SearchContent(
            state = SearchUiState(query = "سيف", results = PreviewData.seriesList, hasSearched = true),
            onQueryChange = {},
            onSearch = {},
            onClear = {},
            onRetry = {},
            onSeriesClick = {}
        )
    }
}

@Preview(name = "Search no results", heightDp = 700, widthDp = 400)
@Composable
private fun SearchEmptyPreview() {
    WingsPreview {
        SearchContent(
            state = SearchUiState(query = "xyz", hasSearched = true),
            onQueryChange = {},
            onSearch = {},
            onClear = {},
            onRetry = {},
            onSeriesClick = {}
        )
    }
}
