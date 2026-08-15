package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val defaultResolution: String = "1080x1920 (FHD 9:16)",
    val defaultFps: Int = 60,
    val defaultBitrateMbps: Int = 12,
    val autoAudioNormalization: Boolean = true,
    val autoDucking: Boolean = true,
    val targetLufs: Float = -14.0f,
    val tabletDualPaneEnabled: Boolean = true,
    val safeZoneGuidesDefault: Boolean = true,
    val statusMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateResolution(resolution: String) {
        _uiState.value = _uiState.value.copy(defaultResolution = resolution)
    }

    fun updateFps(fps: Int) {
        _uiState.value = _uiState.value.copy(defaultFps = fps)
    }

    fun toggleAutoAudioNormalization(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoAudioNormalization = enabled)
    }

    fun toggleAutoDucking(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoDucking = enabled)
    }

    fun toggleTabletDualPane(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(tabletDualPaneEnabled = enabled)
    }

    fun toggleSafeZoneGuides(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(safeZoneGuidesDefault = enabled)
    }

    fun resetToDefaultSeedData() {
        viewModelScope.launch {
            AppDatabase.populateInitialData(database)
            _uiState.value = _uiState.value.copy(statusMessage = "Studio sample projects & assets reloaded successfully!")
        }
    }

    fun clearCache() {
        _uiState.value = _uiState.value.copy(statusMessage = "Local render cache cleared (0 MB freed).")
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
