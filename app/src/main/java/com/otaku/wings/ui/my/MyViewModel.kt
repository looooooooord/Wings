package com.otaku.wings.ui.my

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otaku.wings.data.model.Series
import com.otaku.wings.data.repository.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyUiState(
    val bookmarked: List<Series> = emptyList(),
    val recent: List<Series> = emptyList()
)

class MyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MangaRepository(app)

    private val _state = MutableStateFlow(MyUiState())
    val state: StateFlow<MyUiState> = _state.asStateFlow()

    init {
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
}
