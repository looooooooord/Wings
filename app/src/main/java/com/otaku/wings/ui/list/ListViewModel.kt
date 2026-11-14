package com.otaku.wings.ui.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otaku.wings.data.model.Series
import com.otaku.wings.data.repository.MangaRepository
import com.otaku.wings.ui.common.ErrorKind
import com.otaku.wings.ui.common.classifyError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListUiState(
    val title: String = "",
    val items: List<Series> = emptyList(),
    val isLoading: Boolean = true,
    val error: ErrorKind? = null,
    val selectedIds: Set<String> = emptySet(),
    val selectionMode: Boolean = false
)

class ListViewModel(
    app: Application,
    private val category: String
) : AndroidViewModel(app) {

    private val repo = MangaRepository(app)

    // Only local-DB categories can be deleted in bulk; latest/popular are remote lists.
    val isDeletable = category == "bookmarked" || category == "recent"

    private val _state = MutableStateFlow(ListUiState(title = categoryTitle(category)))
    val state: StateFlow<ListUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun startSelection(seriesId: String) {
        if (!isDeletable) return
        _state.update { it.copy(selectionMode = true, selectedIds = setOf(seriesId)) }
    }

    fun toggleSelection(seriesId: String) {
        _state.update { s ->
            val next = if (seriesId in s.selectedIds) s.selectedIds - seriesId else s.selectedIds + seriesId
            s.copy(selectedIds = next, selectionMode = next.isNotEmpty())
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectionMode = false, selectedIds = emptySet()) }
    }

    fun deleteSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { id ->
                when (category) {
                    "bookmarked" -> repo.removeBookmark(id)
                    "recent" -> repo.removeHistory(id)
                }
            }
            _state.update { it.copy(selectionMode = false, selectedIds = emptySet()) }
        }
    }

    /** Re-runs the same load; used by the error state's retry action. */
    fun retry() {
        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }
        load()
    }

    private var loadJob: kotlinx.coroutines.Job? = null

    private fun load() {
        loadJob = viewModelScope.launch {
            try {
                when (category) {
                    "latest" -> {
                        val items = repo.fetchLatest()
                        _state.update { it.copy(items = items, isLoading = false, error = null) }
                    }

                    "popular" -> {
                        val items = repo.fetchPopular()
                        _state.update { it.copy(items = items, isLoading = false, error = null) }
                    }

                    "bookmarked" -> {
                        repo.getBookmarks().collect { items ->
                            _state.update { it.copy(items = items, isLoading = false, error = null) }
                        }
                    }

                    "recent" -> {
                        repo.getRecentHistory(100).collect { items ->
                            _state.update { it.copy(items = items, isLoading = false, error = null) }
                        }
                    }

                    else -> _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("ListViewModel", "Failed to load list '$category'", e)
                _state.update { it.copy(isLoading = false, error = classifyError(e)) }
            }
        }
    }

    companion object {
        fun categoryTitle(cat: String) = when (cat) {
            "latest" -> "أحدث القصص المصورة"
            "popular" -> "القصص المصورة الشائعة"
            "bookmarked" -> "المحفوظات"
            "recent" -> "آخر ما تمت مشاهدته"
            else -> ""
        }
    }
}
