package com.otaku.wings.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.otaku.wings.R
import com.otaku.wings.data.model.Series
import com.otaku.wings.ui.common.ConfirmDialog
import com.otaku.wings.ui.common.EmptyState
import com.otaku.wings.ui.common.ErrorState
import com.otaku.wings.ui.common.HairlineDivider
import com.otaku.wings.ui.common.LoadingState
import com.otaku.wings.ui.common.PreviewData
import com.otaku.wings.ui.common.SeriesListItem
import com.otaku.wings.ui.common.WingsPreview
import com.otaku.wings.ui.common.WingsTopBar
import com.otaku.wings.ui.theme.Dimens
import com.otaku.wings.ui.theme.Spacing
import com.otaku.wings.ui.theme.WingsType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    category: String,
    onBack: () -> Unit,
    onSeriesClick: (Series) -> Unit
) {
    val context = LocalContext.current.applicationContext as android.app.Application
    val viewModel: ListViewModel = viewModel(
        key = category,
        factory = viewModelFactory {
            initializer { ListViewModel(context, category) }
        }
    )
    val state by viewModel.state.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ListContent(
        state = state,
        category = category,
        isDeletable = viewModel.isDeletable,
        onBack = onBack,
        onSeriesClick = onSeriesClick,
        onLongClick = { id -> viewModel.startSelection(id) },
        onToggleSelection = viewModel::toggleSelection,
        onClearSelection = viewModel::clearSelection,
        onRequestDelete = { showDeleteConfirm = true },
        onRetry = viewModel::retry
    )

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.list_delete_title),
            message = pluralStringResource(
                R.plurals.list_delete_body,
                state.selectedIds.size,
                state.selectedIds.size
            ),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.deleteSelected()
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false },
            destructive = true
        )
    }
}

/** Screen body without the ViewModel. */
@Composable
fun ListContent(
    state: ListUiState,
    category: String,
    isDeletable: Boolean,
    onBack: () -> Unit,
    onSeriesClick: (Series) -> Unit,
    onLongClick: (String) -> Unit,
    onToggleSelection: (String) -> Unit,
    onClearSelection: () -> Unit,
    onRequestDelete: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize()) {
        if (state.selectionMode) {
            SelectionBar(
                count = state.selectedIds.size,
                onClose = onClearSelection,
                onDelete = onRequestDelete
            )
        } else {
            WingsTopBar(title = state.title, onBack = onBack)
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading && state.items.isEmpty() -> LoadingState()
                state.error != null && state.items.isEmpty() -> ErrorState(
                    kind = state.error,
                    onRetry = onRetry
                )

                state.items.isEmpty() -> EmptyState(
                    title = stringResource(emptyTitleFor(category)),
                    message = stringResource(R.string.list_empty_body)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = Spacing.xxl)
                ) {
                    items(state.items, key = { it.id }, contentType = { "series" }) { series ->
                        SeriesListItem(
                            series = series,
                            selectable = state.selectionMode,
                            selected = series.id in state.selectedIds,
                            onClick = {
                                if (state.selectionMode) onToggleSelection(series.id)
                                else onSeriesClick(series)
                            },
                            onLongClick = if (isDeletable) {
                                { onLongClick(series.id) }
                            } else null
                        )
                        HairlineDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionBar(count: Int, onClose: () -> Unit, onDelete: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.TouchTarget)
                .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_clear_selection),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = pluralStringResource(R.plurals.selected_count, count, count),
                style = WingsType.sectionHeading,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, enabled = count > 0) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = if (count > 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun emptyTitleFor(category: String): Int = when (category) {
    "latest" -> R.string.list_empty_latest
    "popular" -> R.string.list_empty_popular
    "bookmarked" -> R.string.list_empty_bookmarks
    "recent" -> R.string.list_empty_recent
    else -> R.string.list_empty_latest
}

@Preview(name = "List light", heightDp = 700, widthDp = 400)
@Composable
private fun ListContentPreview() {
    WingsPreview {
        ListContent(
            state = ListUiState(title = "المحفوظات", items = PreviewData.seriesList, isLoading = false),
            category = "bookmarked",
            isDeletable = true,
            onBack = {},
            onSeriesClick = {},
            onLongClick = {},
            onToggleSelection = {},
            onClearSelection = {},
            onRequestDelete = {},
            onRetry = {}
        )
    }
}

@Preview(name = "List selection", heightDp = 700, widthDp = 400)
@Composable
private fun ListSelectionPreview() {
    WingsPreview {
        ListContent(
            state = ListUiState(
                title = "المحفوظات",
                items = PreviewData.seriesList,
                isLoading = false,
                selectionMode = true,
                selectedIds = setOf(PreviewData.series.id)
            ),
            category = "bookmarked",
            isDeletable = true,
            onBack = {},
            onSeriesClick = {},
            onLongClick = {},
            onToggleSelection = {},
            onClearSelection = {},
            onRequestDelete = {},
            onRetry = {}
        )
    }
}
