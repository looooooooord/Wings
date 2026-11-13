package com.otaku.wings.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otaku.wings.data.model.Chapter
import com.otaku.wings.data.model.Series
import com.otaku.wings.data.repository.MangaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetailUiState(
    val series: Series? = null,
    val chapters: List<Chapter> = emptyList(),
    val isBookmarked: Boolean = false,
    val lastReadChapterUrl: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class DetailViewModel(
    app: Application,
    private val seriesUrl: String
) : AndroidViewModel(app) {

    private val repo = MangaRepository(app)
    private val seriesId = seriesUrl.substringAfter('#').ifBlank {
        seriesUrl.substringAfterLast("/").ifBlank { seriesUrl }
    }

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        load()
        observeBookmark()
        observeLastRead()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val (series, chapters) = repo.fetchSeriesDetail(seriesUrl)
                _state.update { it.copy(series = series, chapters = chapters, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun observeBookmark() {
        viewModelScope.launch {
            repo.isBookmarked(seriesId).collect { bookmarked ->
                _state.update { it.copy(isBookmarked = bookmarked) }
            }
        }
    }

    private fun observeLastRead() {
        viewModelScope.launch {
            repo.getLastReadChapterUrl(seriesId).collect { url ->
                _state.update { it.copy(lastReadChapterUrl = url) }
            }
        }
    }

    fun toggleBookmark() {
        val series = _state.value.series ?: return
        viewModelScope.launch {
            if (_state.value.isBookmarked) {
                repo.removeBookmark(seriesId)
            } else {
                repo.addBookmark(series)
            }
        }
    }
}
