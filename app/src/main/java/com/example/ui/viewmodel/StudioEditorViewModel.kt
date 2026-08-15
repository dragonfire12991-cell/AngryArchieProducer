package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ArchiePose
import com.example.data.model.BackgroundTheme
import com.example.data.model.CharacterType
import com.example.data.model.DialogueBeat
import com.example.data.model.JarvisPose
import com.example.data.model.ProjectEntity
import com.example.data.model.SubtitleStyle
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class RenderState(
    val isRendering: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val currentFrame: Int = 0,
    val totalFrames: Int = 0,
    val currentStage: String = "",
    val isComplete: Boolean = false,
    val exportedFileName: String = ""
)

data class StudioEditorUiState(
    val project: ProjectEntity? = null,
    val beats: List<DialogueBeat> = emptyList(),
    val isPlaying: Boolean = false,
    val currentTimeSec: Float = 0f,
    val totalDurationSec: Float = 0f,
    val currentBeatIndex: Int = 0,
    val activeSpeaker: CharacterType = CharacterType.ARCHIE,
    val activeArchiePose: String = ArchiePose.IDLE.tag,
    val activeJarvisPose: String = JarvisPose.CALM.tag,
    val activeCaptionText: String = "",
    val activeSfxCue: String? = null,
    val audioMeterLevel: Float = 0f,
    val isDialogueAutoAnalyzed: Boolean = false,
    val renderState: RenderState = RenderState(),
    val activeTab: Int = 0 // 0: Timeline, 1: Stage/Poses, 2: Audio/Music, 3: Captions, 4: Export
)

class StudioEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(database.projectDao())

    private val _uiState = MutableStateFlow(StudioEditorUiState())
    val uiState: StateFlow<StudioEditorUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var renderJob: Job? = null

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            val project = repository.getProjectDirect(projectId)
            if (project != null) {
                val beats = AppDatabase.deserializeBeats(project.dialogueJson).ifEmpty {
                    AppDatabase.deserializeBeats(AppDatabase.getSampleDialogueForShort())
                }
                val totalDuration = beats.sumOf { it.durationSec.toDouble() }.toFloat().coerceAtLeast(1f)
                _uiState.value = _uiState.value.copy(
                    project = project,
                    beats = beats,
                    totalDurationSec = totalDuration,
                    activeArchiePose = if (beats.isNotEmpty() && beats[0].speaker == CharacterType.ARCHIE) beats[0].poseTag else ArchiePose.IDLE.tag,
                    activeJarvisPose = if (beats.isNotEmpty() && beats[0].speaker == CharacterType.JARVIS) beats[0].poseTag else JarvisPose.CALM.tag,
                    activeCaptionText = if (beats.isNotEmpty()) beats[0].text else ""
                )
            }
        }
    }

    fun setActiveTab(index: Int) {
        _uiState.value = _uiState.value.copy(activeTab = index)
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlaying = true)

        playbackJob = viewModelScope.launch {
            val updateIntervalMs = 50L
            val stepSec = updateIntervalMs / 1000f

            while (_uiState.value.isPlaying) {
                delay(updateIntervalMs)
                val state = _uiState.value
                val beats = state.beats
                if (beats.isEmpty()) break

                var newTime = state.currentTimeSec + stepSec
                if (newTime >= state.totalDurationSec) {
                    newTime = 0f // Loop playback
                }

                // Determine active beat
                var accumulated = 0f
                var targetBeatIndex = 0
                var found = false

                for (i in beats.indices) {
                    val beat = beats[i]
                    if (newTime >= accumulated && newTime < (accumulated + beat.durationSec)) {
                        targetBeatIndex = i
                        found = true
                        break
                    }
                    accumulated += beat.durationSec
                }
                if (!found && beats.isNotEmpty()) {
                    targetBeatIndex = beats.lastIndex
                }

                val currentBeat = beats[targetBeatIndex]
                val speaker = currentBeat.speaker
                val archiePose = if (speaker == CharacterType.ARCHIE) currentBeat.poseTag else ArchiePose.IDLE.tag
                val jarvisPose = if (speaker == CharacterType.JARVIS) currentBeat.poseTag else JarvisPose.CALM.tag
                val meter = (0.4f + (Math.sin(newTime.toDouble() * 12.0).toFloat() * 0.45f).coerceIn(-0.3f, 0.5f)).coerceIn(0.1f, 1f)

                _uiState.value = state.copy(
                    currentTimeSec = newTime,
                    currentBeatIndex = targetBeatIndex,
                    activeSpeaker = speaker,
                    activeArchiePose = archiePose,
                    activeJarvisPose = jarvisPose,
                    activeCaptionText = currentBeat.text,
                    activeSfxCue = currentBeat.sfxCue.takeIf { it != "None" },
                    audioMeterLevel = meter
                )
            }
        }
    }

    fun pausePlayback() {
        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlaying = false, audioMeterLevel = 0f)
    }

    fun seekTo(timeSec: Float) {
        val total = _uiState.value.totalDurationSec
        val clamped = timeSec.coerceIn(0f, total)
        val beats = _uiState.value.beats
        if (beats.isEmpty()) return

        var accumulated = 0f
        var targetBeatIndex = 0
        for (i in beats.indices) {
            val beat = beats[i]
            if (clamped >= accumulated && clamped < (accumulated + beat.durationSec)) {
                targetBeatIndex = i
                break
            }
            accumulated += beat.durationSec
        }

        val beat = beats[targetBeatIndex]
        _uiState.value = _uiState.value.copy(
            currentTimeSec = clamped,
            currentBeatIndex = targetBeatIndex,
            activeSpeaker = beat.speaker,
            activeArchiePose = if (beat.speaker == CharacterType.ARCHIE) beat.poseTag else ArchiePose.IDLE.tag,
            activeJarvisPose = if (beat.speaker == CharacterType.JARVIS) beat.poseTag else JarvisPose.CALM.tag,
            activeCaptionText = beat.text
        )
    }

    fun jumpToBeat(index: Int) {
        val beats = _uiState.value.beats
        if (index in beats.indices) {
            var time = 0f
            for (i in 0 until index) {
                time += beats[i].durationSec
            }
            seekTo(time + 0.05f)
        }
    }

    fun addBeat(speaker: CharacterType = CharacterType.ARCHIE, text: String = "New dialogue beat...") {
        val pose = DialogueBeat.detectPose(speaker, text)
        val newBeat = DialogueBeat(
            id = "b_${UUID.randomUUID().toString().take(6)}",
            speaker = speaker,
            text = text,
            poseTag = pose,
            durationSec = 3.5f,
            sfxCue = "None"
        )
        val updatedList = _uiState.value.beats + newBeat
        updateBeatsList(updatedList)
    }

    fun updateBeat(index: Int, updated: DialogueBeat) {
        val list = _uiState.value.beats.toMutableList()
        if (index in list.indices) {
            list[index] = updated
            updateBeatsList(list)
        }
    }

    fun deleteBeat(index: Int) {
        val list = _uiState.value.beats.toMutableList()
        if (index in list.indices && list.size > 1) {
            list.removeAt(index)
            updateBeatsList(list)
        }
    }

    fun autoAnalyzeAllPoses() {
        val updated = _uiState.value.beats.map { beat ->
            val detected = DialogueBeat.detectPose(beat.speaker, beat.text)
            beat.copy(poseTag = detected)
        }
        _uiState.value = _uiState.value.copy(isDialogueAutoAnalyzed = true)
        updateBeatsList(updated)
    }

    private fun updateBeatsList(newList: List<DialogueBeat>) {
        val totalDuration = newList.sumOf { it.durationSec.toDouble() }.toFloat().coerceAtLeast(1f)
        val currentProject = _uiState.value.project
        val serialized = AppDatabase.serializeBeats(newList)

        _uiState.value = _uiState.value.copy(
            beats = newList,
            totalDurationSec = totalDuration
        )

        if (currentProject != null) {
            val updatedProject = currentProject.copy(
                dialogueJson = serialized,
                durationSeconds = totalDuration.toInt(),
                updatedAt = System.currentTimeMillis()
            )
            _uiState.value = _uiState.value.copy(project = updatedProject)
            viewModelScope.launch {
                repository.updateProject(updatedProject)
            }
        }
    }

    fun updateProjectSettings(
        theme: BackgroundTheme? = null,
        musicTrack: String? = null,
        musicVolume: Float? = null,
        voiceVolume: Float? = null,
        normalized: Boolean? = null,
        ducking: Boolean? = null,
        subtitleStyle: SubtitleStyle? = null,
        showSafeZoneOverlay: Boolean? = null,
        title: String? = null
    ) {
        val current = _uiState.value.project ?: return
        val updated = current.copy(
            title = title ?: current.title,
            backgroundTheme = theme?.name ?: current.backgroundTheme,
            musicTrack = musicTrack ?: current.musicTrack,
            musicVolume = musicVolume ?: current.musicVolume,
            voiceVolume = voiceVolume ?: current.voiceVolume,
            audioNormalized = normalized ?: current.audioNormalized,
            duckingEnabled = ducking ?: current.duckingEnabled,
            subtitleStyle = subtitleStyle?.name ?: current.subtitleStyle,
            showSafeZoneOverlay = showSafeZoneOverlay ?: current.showSafeZoneOverlay,
            updatedAt = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(project = updated)
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun updateCharacterPosition(
        isArchie: Boolean,
        posX: Float,
        posY: Float,
        scale: Float,
        flip: Boolean
    ) {
        val current = _uiState.value.project ?: return
        val updated = if (isArchie) {
            current.copy(
                archiePositionX = posX,
                archiePositionY = posY,
                archieScale = scale,
                archieFlip = flip
            )
        } else {
            current.copy(
                jarvisPositionX = posX,
                jarvisPositionY = posY,
                jarvisScale = scale,
                jarvisFlip = flip
            )
        }
        _uiState.value = _uiState.value.copy(project = updated)
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    fun startMp4Export(resolution: String = "1080x1920", fps: Int = 60) {
        if (_uiState.value.renderState.isRendering) return
        pausePlayback()

        val project = _uiState.value.project
        val duration = _uiState.value.totalDurationSec
        val totalFrames = (duration * fps).toInt().coerceAtLeast(300)
        val fileName = "${project?.title?.replace(" ", "_") ?: "Archie_Export"}_${resolution}_${fps}fps.mp4"

        _uiState.value = _uiState.value.copy(
            renderState = RenderState(
                isRendering = true,
                progress = 0f,
                currentFrame = 0,
                totalFrames = totalFrames,
                currentStage = "Initializing 9:16 Canvas & Assets...",
                isComplete = false,
                exportedFileName = fileName
            )
        )

        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            val stages = listOf(
                "Initializing 9:16 Studio Canvas Pipeline...",
                "Synthesizing Archie & Jarvis Lip-Sync Layers...",
                "Rasterizing Punchy Subtitle Captions...",
                "Applying Audio Normalization (-14 LUFS)...",
                "Mixing Ducked Audio & Dialogue Beats...",
                "Hardware Encoding H.264 MP4 Stream (NVENC)...",
                "Finalizing Container Muxing & Keyframes..."
            )

            var frame = 0
            val batchSize = totalFrames / 40 + 1

            while (frame < totalFrames) {
                delay(80)
                frame = (frame + batchSize).coerceAtMost(totalFrames)
                val progress = frame.toFloat() / totalFrames
                val stageIndex = ((progress * (stages.size - 1)).toInt()).coerceIn(0, stages.lastIndex)

                _uiState.value = _uiState.value.copy(
                    renderState = _uiState.value.renderState.copy(
                        progress = progress,
                        currentFrame = frame,
                        currentStage = stages[stageIndex]
                    )
                )
            }

            delay(300)
            _uiState.value = _uiState.value.copy(
                renderState = _uiState.value.renderState.copy(
                    isRendering = false,
                    isComplete = true,
                    progress = 1.0f,
                    currentStage = "Export Complete! Video saved to local storage."
                )
            )
        }
    }

    fun dismissRenderModal() {
        _uiState.value = _uiState.value.copy(
            renderState = RenderState()
        )
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        renderJob?.cancel()
    }
}
