package com.otaku.wings.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otaku.wings.data.model.Series
import com.otaku.wings.data.repository.MangaRepository
import com.otaku.wings.ui.common.ErrorKind
import com.otaku.wings.ui.common.classifyError
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val latest: List<Series> = emptyList(),
    val popular: List<Series> = emptyList(),
    val bookmarked: List<Series> = emptyList(),
    val recent: List<Series> = emptyList(),
    val isLoading: Boolean = false,
    val error: ErrorKind? = null
) {
    val hasContent: Boolean
        get() = latest.isNotEmpty() || popular.isNotEmpty()

    val hasAnySection: Boolean
        get() = latest.isNotEmpty() || popular.isNotEmpty() ||
            bookmarked.isNotEmpty() || recent.isNotEmpty()
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MangaRepository(app)

    private val _state = MutableStateFlow(HomeUiState(isLoading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        observeLocalData()
        refresh()
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            repo.getBookmarks().collect { bookmarks ->
                _state.update { it.copy(bookmarked = bookmarks) }
            }
        }
        viewModelScope.launch {
            repo.getRecentHistory().collect { recent ->
                _state.update { it.copy(recent = recent) }
            }
        }
    }

    fun refresh() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val (latest, popular) = coroutineScope {
                    val latestDeferred = async { repo.fetchLatest() }
                    val popularDeferred = async { repo.fetchPopular() }
                    latestDeferred.await() to popularDeferred.await()
                }
                _state.update { it.copy(latest = latest, popular = popular, isLoading = false) }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to refresh home content", e)
                _state.update { it.copy(isLoading = false, error = classifyError(e)) }
            }
        }
    }
}
