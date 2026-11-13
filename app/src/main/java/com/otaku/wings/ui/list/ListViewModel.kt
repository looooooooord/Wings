package com.otaku.wings.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otaku.wings.data.model.Series
import com.otaku.wings.data.repository.MangaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ListUiState(
    val title: String = "",
    val items: List<Series> = emptyList(),
    val isLoading: Boolean = true,
    val selectedIds: Set<String> = emptySet(),
    val selectionMode: Boolean = false
)

class ListViewModel(
    app: Application,
    private val category: String
) : AndroidViewModel(app) {

    private val repo = MangaRepository(app)

    // المحفوظات/آخر ما تمت مشاهدته는 로컬 DB 기반이라 꾹 눌러 일괄 حذف 가능; 최신/인기는 원격 목록이라 حذف 불가
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

    private fun load() {
        viewModelScope.launch {
            when (category) {
                "latest" -> {
                    val items = repo.fetchLatest()
                    _state.update { it.copy(items = items, isLoading = false) }
                }
                "popular" -> {
                    val items = repo.fetchPopular()
                    _state.update { it.copy(items = items, isLoading = false) }
                }
                "bookmarked" -> {
                    repo.getBookmarks().collect { items ->
                        _state.update { it.copy(items = items, isLoading = false) }
                    }
                }
                "recent" -> {
                    repo.getRecentHistory(100).collect { items ->
                        _state.update { it.copy(items = items, isLoading = false) }
                    }
                }
            }
        }
    }

    private fun categoryTitle(cat: String) = when (cat) {
        "latest" -> "أحدث القصص المصورة"
        "popular" -> "القصص المصورة الشائعة"
        "bookmarked" -> "المحفوظات"
        "recent" -> "آخر ما تمت مشاهدته"
        else -> ""
    }
}
