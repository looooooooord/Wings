package com.otaku.wings.ui.viewer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.otaku.wings.data.local.ReadingDirection
import com.otaku.wings.data.local.SettingsRepository
import com.otaku.wings.data.model.Chapter
import com.otaku.wings.data.model.MangaPage
import com.otaku.wings.data.model.ViewerMode
import com.otaku.wings.data.repository.MangaRepository
import com.otaku.wings.ui.common.ErrorKind
import com.otaku.wings.ui.common.classifyError
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ViewerUiState(
    val pages: List<MangaPage> = emptyList(),
    // -1 means "no page recorded yet". Using 0 would make entering page 0 look like "unchanged"
    // and the first reading-history write would be skipped.
    val currentPage: Int = -1,
    val mode: ViewerMode = ViewerMode.WEBTOON,
    val readingDirection: ReadingDirection = ReadingDirection.LTR,
    val twoPageMode: Boolean = false,
    val firstPageSingle: Boolean = true,
    val showControls: Boolean = true,
    val showSettings: Boolean = false,
    val prevChapter: Chapter? = null,
    val nextChapter: Chapter? = null,
    val isLoading: Boolean = true,
    val error: ErrorKind? = null
)

class ViewerViewModel(
    app: Application,
    private val chapterUrl: String,
    private val chapterTitle: String,
    private val seriesId: String,
    private val seriesUrl: String,
    private val seriesTitle: String,
    private val seriesAuthor: String,
    private val coverUrl: String
) : AndroidViewModel(app) {

    private val repo = MangaRepository(app)
    private val settingsRepo = SettingsRepository(app)

    private val _state = MutableStateFlow(
        ViewerUiState(
            mode = settingsRepo.getDefaultViewerMode(),
            readingDirection = settingsRepo.getReadingDirection(),
            twoPageMode = settingsRepo.getTwoPageMode(),
            firstPageSingle = settingsRepo.getFirstPageSingle()
        )
    )
    val state: StateFlow<ViewerUiState> = _state.asStateFlow()

    private var pagesJob: Job? = null

    init {
        loadPages()
        loadAdjacentChapters()
    }

    private fun loadPages() {
        pagesJob = viewModelScope.launch {
            try {
                val pages = repo.fetchPages(chapterUrl)
                _state.update {
                    it.copy(
                        pages = pages,
                        isLoading = false,
                        error = if (pages.isEmpty()) ErrorKind.GENERIC else null
                    )
                }
            } catch (e: Exception) {
                Log.e("ViewerViewModel", "Failed to load pages for '$chapterUrl'", e)
                _state.update { it.copy(isLoading = false, error = classifyError(e)) }
            }
        }
    }

    /** Reloads the chapter; used by the chapter error state's retry action. */
    fun retry() {
        pagesJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }
        loadPages()
    }

    private fun loadAdjacentChapters() {
        viewModelScope.launch {
            try {
                val chapters = repo.fetchChapters(chapterUrl)
                val index = chapters.indexOfFirst { it.url == chapterUrl }
                if (index == -1) return@launch
                _state.update {
                    it.copy(
                        prevChapter = chapters.getOrNull(index + 1),
                        nextChapter = chapters.getOrNull(index - 1)
                    )
                }
            } catch (e: Exception) {
                Log.w("ViewerViewModel", "Adjacent chapters unavailable", e)
            }
        }
    }

    fun toggleControls() = _state.update { it.copy(showControls = !it.showControls) }

    fun toggleSettings() = _state.update { it.copy(showSettings = !it.showSettings) }

    fun setMode(mode: ViewerMode) {
        settingsRepo.setDefaultViewerMode(mode)
        _state.update { it.copy(mode = mode) }
    }

    fun setReadingDirection(direction: ReadingDirection) {
        settingsRepo.setReadingDirection(direction)
        _state.update { it.copy(readingDirection = direction) }
    }

    fun setTwoPageMode(value: Boolean) {
        settingsRepo.setTwoPageMode(value)
        _state.update { it.copy(twoPageMode = value) }
    }

    fun setFirstPageSingle(value: Boolean) {
        settingsRepo.setFirstPageSingle(value)
        _state.update { it.copy(firstPageSingle = value) }
    }

    /**
     * Records the visible page. Called from a debounced UI effect, so scrolling does not write to
     * the database on every frame.
     */
    fun onPageChanged(page: Int) {
        val totalPages = _state.value.pages.size
        if (totalPages == 0) return
        val safePage = clampPage(page, totalPages)
        val prev = _state.value.currentPage
        _state.update { it.copy(currentPage = safePage) }
        if (safePage == prev) return
        viewModelScope.launch {
            try {
                repo.recordHistory(
                    chapterUrl = chapterUrl,
                    chapterTitle = chapterTitle,
                    seriesId = seriesId,
                    seriesUrl = seriesUrl,
                    seriesTitle = seriesTitle,
                    seriesAuthor = seriesAuthor,
                    coverUrl = coverUrl,
                    lastPage = safePage,
                    totalPages = totalPages
                )
            } catch (e: Exception) {
                Log.e("ViewerViewModel", "Failed to record reading history", e)
            }
        }
    }
}
