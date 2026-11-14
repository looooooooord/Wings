package com.otaku.wings.ui.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otaku.wings.data.model.Chapter
import com.otaku.wings.data.model.Series
import com.otaku.wings.data.repository.MangaRepository
import com.otaku.wings.ui.common.ErrorKind
import com.otaku.wings.ui.common.classifyError
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val series: Series? = null,
    val chapters: List<Chapter> = emptyList(),
    val isBookmarked: Boolean = false,
    val lastReadChapterUrl: String? = null,
    val isLoading: Boolean = true,
    val error: ErrorKind? = null
)

class DetailViewModel(
    app: Application,
    private val seriesUrl: String
) : AndroidViewModel(app) {

    private val repo = MangaRepository(app)

    // Series ids are stored without the decorative "#slug" suffix the source URLs carry.
    private val seriesId = seriesUrl.substringAfter('#').ifBlank {
        seriesUrl.substringAfterLast("/").ifBlank { seriesUrl }
    }

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
        observeBookmark()
        observeLastRead()
    }

    private fun load() {
        loadJob = viewModelScope.launch {
            try {
                val (series, chapters) = repo.fetchSeriesDetail(seriesUrl)
                _state.update { it.copy(series = series, chapters = chapters, isLoading = false, error = null) }
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Failed to load series '$seriesUrl'", e)
                _state.update { it.copy(isLoading = false, error = classifyError(e)) }
            }
        }
    }

    /** Re-runs the same load; used by the error state's retry action. */
    fun retry() {
        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }
        load()
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
