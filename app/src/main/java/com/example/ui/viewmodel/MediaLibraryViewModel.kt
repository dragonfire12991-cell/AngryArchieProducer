package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.MediaAssetEntity
import com.example.data.repository.MediaRepository
import com.example.data.util.MediaStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class MediaLibraryUiState(
    val assets: List<MediaAssetEntity> = emptyList(),
    val selectedCategory: String = "ALL", // "ALL", "ARCHIE_POSE", "JARVIS_POSE", "BACKGROUND", "AUDIO_VOICE", "MUSIC", "SFX"
    val searchQuery: String = "",
    val isImporting: Boolean = false,
    val currentlyPlayingAssetId: Long? = null,
    val isPlayingAudio: Boolean = false,
    val statusMessage: String? = null
)

class MediaLibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = MediaRepository(database.mediaAssetDao())

    private val _selectedCategory = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")
    private val _isImporting = MutableStateFlow(false)
    private val _currentlyPlayingAssetId = MutableStateFlow<Long?>(null)
    private val _isPlayingAudio = MutableStateFlow(false)
    private val _statusMessage = MutableStateFlow<String?>(null)

    private var mediaPlayer: MediaPlayer? = null

    val uiState: StateFlow<MediaLibraryUiState> = combine(
        repository.allAssets,
        _selectedCategory,
        _searchQuery,
        _isImporting,
        _currentlyPlayingAssetId,
        _isPlayingAudio,
        _statusMessage
    ) { params ->
        val assets = params[0] as List<MediaAssetEntity>
        val category = params[1] as String
        val query = params[2] as String
        val isImporting = params[3] as Boolean
        val playingId = params[4] as Long?
        val isPlaying = params[5] as Boolean
        val statusMsg = params[6] as String?

        val filtered = assets.filter { asset ->
            val matchesCategory = when (category) {
                "ALL" -> true
                "ARCHIE_POSE" -> asset.category == "CHARACTER_POSE" && asset.character == "ARCHIE"
                "JARVIS_POSE" -> asset.category == "CHARACTER_POSE" && asset.character == "JARVIS"
                "BACKGROUND" -> asset.category == "BACKGROUND"
                "AUDIO_VOICE" -> asset.category == "AUDIO_VOICE"
                "MUSIC" -> asset.category == "MUSIC"
                "SFX" -> asset.category == "SFX"
                else -> asset.category == category
            }
            val matchesQuery = query.isBlank() || asset.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        MediaLibraryUiState(
            assets = filtered,
            selectedCategory = category,
            searchQuery = query,
            isImporting = isImporting,
            currentlyPlayingAssetId = playingId,
            isPlayingAudio = isPlaying,
            statusMessage = statusMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaLibraryUiState()
    )

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun importSingleFile(
        context: Context,
        uri: Uri,
        category: String,
        character: String? = null,
        poseTag: String? = null,
        customName: String? = null
    ) {
        viewModelScope.launch {
            _isImporting.value = true
            val folder = when (category) {
                "CHARACTER_POSE" -> "poses"
                "BACKGROUND" -> "backgrounds"
                "AUDIO_VOICE" -> "voice"
                "MUSIC" -> "music"
                else -> "sfx"
            }
            val result = withContext(Dispatchers.IO) {
                MediaStorageManager.copyUriToAppStorage(context, uri, folder, prefix = category.lowercase())
            }

            if (result != null) {
                val assetName = if (!customName.isNullOrBlank()) customName else result.originalName
                val entity = MediaAssetEntity(
                    name = assetName,
                    category = category,
                    character = character,
                    poseTag = poseTag ?: if (character == "ARCHIE") "custom_pose" else null,
                    filePath = result.localFilePath,
                    fileSize = result.fileSizeBytes,
                    durationSec = result.durationSeconds,
                    isBuiltIn = false
                )
                repository.insertAsset(entity)
                _statusMessage.value = "Imported \"$assetName\" to Media Library"
            } else {
                _statusMessage.value = "Failed to import media file."
            }
            _isImporting.value = false
        }
    }

    fun importMultipleFiles(
        context: Context,
        uris: List<Uri>,
        category: String,
        character: String? = null
    ) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isImporting.value = true
            var importedCount = 0
            val folder = when (category) {
                "CHARACTER_POSE" -> "poses"
                "BACKGROUND" -> "backgrounds"
                "AUDIO_VOICE" -> "voice"
                "MUSIC" -> "music"
                else -> "sfx"
            }

            withContext(Dispatchers.IO) {
                for (uri in uris) {
                    val result = MediaStorageManager.copyUriToAppStorage(context, uri, folder, prefix = "${character ?: "asset"}_pose")
                    if (result != null) {
                        val poseTag = if (category == "CHARACTER_POSE") {
                            val lower = result.originalName.lowercase()
                            when {
                                lower.contains("angry") || lower.contains("rage") -> "angry"
                                lower.contains("shout") || lower.contains("scream") -> "shouting"
                                lower.contains("facepalm") -> "facepalm"
                                lower.contains("laugh") -> "laughing"
                                lower.contains("talk") -> "talking"
                                lower.contains("glow") || lower.contains("visor") -> "visor_glow"
                                lower.contains("sarcastic") -> "sarcastic"
                                lower.contains("shock") -> "shocked"
                                lower.contains("coffee") -> "coffee"
                                else -> "custom_${(10..99).random()}"
                            }
                        } else null

                        val entity = MediaAssetEntity(
                            name = result.originalName,
                            category = category,
                            character = character,
                            poseTag = poseTag,
                            filePath = result.localFilePath,
                            fileSize = result.fileSizeBytes,
                            durationSec = result.durationSeconds,
                            isBuiltIn = false
                        )
                        repository.insertAsset(entity)
                        importedCount++
                    }
                }
            }
            _statusMessage.value = "Successfully imported $importedCount files to Media Library"
            _isImporting.value = false
        }
    }

    fun toggleAudioPreview(asset: MediaAssetEntity) {
        if (_currentlyPlayingAssetId.value == asset.id && _isPlayingAudio.value) {
            stopAudio()
        } else {
            playAudio(asset)
        }
    }

    private fun playAudio(asset: MediaAssetEntity) {
        stopAudio()
        try {
            val file = File(asset.filePath)
            if (!file.exists()) {
                _statusMessage.value = "Audio file not found on device storage"
                return
            }
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    start()
                    _currentlyPlayingAssetId.value = asset.id
                    _isPlayingAudio.value = true
                }
                setOnCompletionListener {
                    stopAudio()
                }
                setOnErrorListener { _, _, _ ->
                    stopAudio()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("MediaLibraryVM", "Error playing audio", e)
            stopAudio()
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
            _currentlyPlayingAssetId.value = null
            _isPlayingAudio.value = false
        }
    }

    fun deleteAsset(asset: MediaAssetEntity) {
        viewModelScope.launch {
            if (_currentlyPlayingAssetId.value == asset.id) {
                stopAudio()
            }
            if (!asset.isBuiltIn) {
                withContext(Dispatchers.IO) {
                    try {
                        val file = File(asset.filePath)
                        if (file.exists()) file.delete()
                    } catch (_: Exception) {}
                }
            }
            repository.deleteAsset(asset)
            _statusMessage.value = "Removed \"${asset.name}\""
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
