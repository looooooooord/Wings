package com.otaku.wings.ui.search

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

data class SearchUiState(
    val query: String = "",
    val results: List<Series> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: ErrorKind? = null
) {
    /** A finished search that returned nothing — distinct from "not searched yet". */
    val isEmptyResult: Boolean
        get() = hasSearched && !isLoading && error == null && results.isEmpty()
}

class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MangaRepository(app)

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var inFlightQuery: String? = null
    private var lastQuery: String = ""

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    /** Clears the field and returns to the initial state without touching the network. */
    fun clear() {
        _state.value = SearchUiState()
        inFlightQuery = null
    }

    fun search() {
        val keyword = _state.value.query.trim()
        if (keyword.isBlank()) return
        // Never fire the same request twice while it is still running.
        if (inFlightQuery == keyword) return
        inFlightQuery = keyword
        lastQuery = keyword
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, hasSearched = true) }
            try {
                val results = repo.search(keyword)
                _state.update {
                    it.copy(results = results, isLoading = false, error = null)
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed for '$keyword'", e)
                _state.update {
                    it.copy(isLoading = false, error = classifyError(e), results = emptyList())
                }
            } finally {
                inFlightQuery = null
            }
        }
    }

    /** Retries the last submitted query. */
    fun retry() {
        if (lastQuery.isNotBlank()) {
            _state.update { it.copy(query = lastQuery) }
            search()
        }
    }
}
