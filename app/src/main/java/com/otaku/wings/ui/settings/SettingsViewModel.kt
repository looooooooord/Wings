package com.otaku.wings.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.otaku.wings.data.local.SettingsRepository
import com.otaku.wings.data.local.ThemeMode
import com.otaku.wings.data.local.UrlMode
import com.otaku.wings.data.repository.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val urlMode: UrlMode = UrlMode.AUTO,
    val manualBaseUrl: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val snackbarMessage: String? = null
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app)
    private val repo = MangaRepository(app)

    private val _state = MutableStateFlow(
        SettingsUiState(
            urlMode = settingsRepo.getUrlMode(),
            manualBaseUrl = settingsRepo.getManualBaseUrl(),
            themeMode = settingsRepo.getThemeMode()
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setUrlMode(mode: UrlMode) {
        settingsRepo.setUrlMode(mode)
        _state.update { it.copy(urlMode = mode) }
    }

    fun onManualUrlChange(url: String) {
        _state.update { it.copy(manualBaseUrl = url) }
    }

    fun saveManualUrl() {
        val url = _state.value.manualBaseUrl.trim()
        settingsRepo.setManualBaseUrl(url)
        _state.update { it.copy(manualBaseUrl = url, snackbarMessage = "تم حفظ عنوان URL") }
    }

    fun setThemeMode(mode: ThemeMode) {
        settingsRepo.setThemeMode(mode)
        _state.update { it.copy(themeMode = mode) }
    }

    @OptIn(ExperimentalCoilApi::class)
    fun clearImageCache() {
        val loader = getApplication<Application>().imageLoader
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
        _state.update { it.copy(snackbarMessage = "تم حذف ذاكرة الصور المؤقتة") }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = repo.exportBackupJson()
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                _state.update { it.copy(snackbarMessage = "تم حفظ النسخة الاحتياطية") }
            } catch (e: Exception) {
                _state.update { it.copy(snackbarMessage = "فشل إنشاء النسخة الاحتياطية: ${e.message}") }
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = getApplication<Application>().contentResolver.openInputStream(uri)
                    ?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
                repo.importBackupJson(json)
                _state.update { it.copy(snackbarMessage = "تم استيراد النسخة الاحتياطية") }
            } catch (e: Exception) {
                _state.update { it.copy(snackbarMessage = "فشل التحميل: ${e.message}") }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repo.clearAllData()
            _state.update { it.copy(snackbarMessage = "تم حذف البيانات") }
        }
    }

    fun consumeSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}
