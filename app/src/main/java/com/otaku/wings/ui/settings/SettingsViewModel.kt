package com.otaku.wings.ui.settings

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.otaku.wings.data.local.ReadingDirection
import com.otaku.wings.data.local.SettingsRepository
import com.otaku.wings.data.local.ThemeMode
import com.otaku.wings.data.model.ViewerMode
import com.otaku.wings.data.repository.MangaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Results the screen turns into localised snackbars. */
enum class SettingsMessage {
    EXPORT_SUCCESS, EXPORT_FAILURE,
    IMPORT_SUCCESS, IMPORT_FAILURE,
    DELETE_SUCCESS,
    CACHE_SUCCESS, CACHE_FAILURE
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val viewerMode: ViewerMode = ViewerMode.WEBTOON,
    val readingDirection: ReadingDirection = ReadingDirection.LTR,
    val twoPageMode: Boolean = false,
    val firstPageSingle: Boolean = true,
    val isBusy: Boolean = false,
    val message: SettingsMessage? = null
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app)
    private val repo = MangaRepository(app)

    private val _state = MutableStateFlow(
        SettingsUiState(
            themeMode = settingsRepo.getThemeMode(),
            viewerMode = settingsRepo.getDefaultViewerMode(),
            readingDirection = settingsRepo.getReadingDirection(),
            twoPageMode = settingsRepo.getTwoPageMode(),
            firstPageSingle = settingsRepo.getFirstPageSingle()
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        settingsRepo.setThemeMode(mode)
        _state.update { it.copy(themeMode = mode) }
    }

    // Reader defaults write the exact keys the viewer reads, so both stay in sync.
    fun setViewerMode(mode: ViewerMode) {
        settingsRepo.setDefaultViewerMode(mode)
        _state.update { it.copy(viewerMode = mode) }
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

    @OptIn(ExperimentalCoilApi::class)
    fun clearImageCache() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            val success = try {
                val loader = getApplication<Application>().imageLoader
                loader.memoryCache?.clear()
                loader.diskCache?.clear()
                true
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to clear image cache", e)
                false
            }
            _state.update {
                it.copy(
                    isBusy = false,
                    message = if (success) SettingsMessage.CACHE_SUCCESS else SettingsMessage.CACHE_FAILURE
                )
            }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            val success = withContext(Dispatchers.IO) {
                try {
                    val json = repo.exportBackupJson()
                    val stream = getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException("No output stream for backup")
                    stream.use { out -> out.write(json.toByteArray(Charsets.UTF_8)) }
                    true
                } catch (e: Exception) {
                    Log.e("SettingsViewModel", "Failed to export backup", e)
                    false
                }
            }
            _state.update {
                it.copy(
                    isBusy = false,
                    message = if (success) SettingsMessage.EXPORT_SUCCESS else SettingsMessage.EXPORT_FAILURE
                )
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            val success = withContext(Dispatchers.IO) {
                try {
                    val json = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?.use { it.readBytes().toString(Charsets.UTF_8) }
                        ?: throw IllegalStateException("Empty backup stream")
                    repo.importBackupJson(json)
                    true
                } catch (e: Exception) {
                    Log.e("SettingsViewModel", "Failed to import backup", e)
                    false
                }
            }
            _state.update {
                it.copy(
                    isBusy = false,
                    message = if (success) SettingsMessage.IMPORT_SUCCESS else SettingsMessage.IMPORT_FAILURE
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            try {
                withContext(Dispatchers.IO) { repo.clearAllData() }
                _state.update { it.copy(isBusy = false, message = SettingsMessage.DELETE_SUCCESS) }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to clear data", e)
                _state.update { it.copy(isBusy = false) }
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
