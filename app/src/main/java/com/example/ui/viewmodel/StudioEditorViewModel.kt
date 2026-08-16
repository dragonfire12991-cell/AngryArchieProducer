package com.example.ui.viewmodel

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
import java.io.File
import java.util.UUID

data class RenderState(
    val isRendering: Boolean = false,
    val progress: Float = 0f,
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
    val activeTab: Int = 0
)

class StudioEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(database.projectDao())

    private val _uiState = MutableStateFlow(StudioEditorUiState())
    val uiState: StateFlow<StudioEditorUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var renderJob: Job? = null

    /*
     * REAL AUDIO PLAYERS
     *
     * voicePlayer = imported Archie/Jarvis recording
     * musicPlayer = imported background music
     */
    private val voicePlayer: ExoPlayer =
        ExoPlayer.Builder(application.applicationContext).build()

    private val musicPlayer: ExoPlayer =
        ExoPlayer.Builder(application.applicationContext).build()

    init {
        voicePlayer.repeatMode = Player.REPEAT_MODE_OFF
        musicPlayer.repeatMode = Player.REPEAT_MODE_ALL

        voicePlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        pausePlayback()
                        seekTo(0f)
                    }
                }
            }
        )
    }

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            val project = repository.getProjectDirect(projectId)

            if (project != null) {
                val beats =
                    AppDatabase.deserializeBeats(project.dialogueJson).ifEmpty {
                        AppDatabase.deserializeBeats(
                            AppDatabase.getSampleDialogueForShort()
                        )
                    }

                val beatDuration =
                    beats.sumOf { it.durationSec.toDouble() }
                        .toFloat()
                        .coerceAtLeast(1f)

                _uiState.value =
                    _uiState.value.copy(
                        project = project,
                        beats = beats,
                        totalDurationSec =
                            if (project.archieAudioDurationSec > 0f) {
                                project.archieAudioDurationSec
                            } else {
                                beatDuration
                            },
                        currentTimeSec = 0f,
                        currentBeatIndex = 0,
                        activeArchiePose =
                            if (
                                beats.isNotEmpty() &&
                                beats[0].speaker == CharacterType.ARCHIE
                            ) {
                                beats[0].poseTag
                            } else {
                                ArchiePose.IDLE.tag
                            },
                        activeJarvisPose =
                            if (
                                beats.isNotEmpty() &&
                                beats[0].speaker == CharacterType.JARVIS
                            ) {
                                beats[0].poseTag
                            } else {
                                JarvisPose.CALM.tag
                            },
                        activeCaptionText =
                            if (beats.isNotEmpty()) beats[0].text else ""
                    )

                prepareAudioPlayers(project)
            }
        }
    }

    /*
     * Loads the actual imported files into ExoPlayer.
     */
    private fun prepareAudioPlayers(project: ProjectEntity) {

        voicePlayer.stop()
        voicePlayer.clearMediaItems()

        musicPlayer.stop()
        musicPlayer.clearMediaItems()

        project.archieAudioPath
            ?.takeIf { it.isNotBlank() }
            ?.let { path ->

                val file = File(path)

                if (file.exists()) {
                    voicePlayer.setMediaItem(
                        MediaItem.fromUri(file.toUri())
                    )

                    voicePlayer.prepare()
                    voicePlayer.volume =
                        project.voiceVolume.coerceIn(0f, 1f)
                }
            }

        project.backgroundMusicPath
            ?.takeIf { it.isNotBlank() }
            ?.let { path ->

                val file = File(path)

                if (file.exists()) {
                    musicPlayer.setMediaItem(
                        MediaItem.fromUri(file.toUri())
                    )

                    musicPlayer.prepare()
                    musicPlayer.volume =
                        project.musicVolume.coerceIn(0f, 1f)
                }
            }
    }

    fun setActiveTab(index: Int) {
        _uiState.value =
            _uiState.value.copy(activeTab = index)
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    /*
     * REAL PLAYBACK
     */
    fun startPlayback() {

        playbackJob?.cancel()

        val project = _uiState.value.project ?: return

        voicePlayer.volume =
            project.voiceVolume.coerceIn(0f, 1f)

        musicPlayer.volume =
            project.musicVolume.coerceIn(0f, 1f)

        val startMs =
            (_uiState.value.currentTimeSec * 1000f).toLong()

        if (voicePlayer.mediaItemCount > 0) {
            voicePlayer.seekTo(startMs)
            voicePlayer.play()
        }

        if (musicPlayer.mediaItemCount > 0) {

            val musicDuration = musicPlayer.duration

            val musicPosition =
                if (musicDuration > 0) {
                    startMs % musicDuration
                } else {
                    startMs
                }

            musicPlayer.seekTo(musicPosition)
            musicPlayer.play()
        }

        _uiState.value =
            _uiState.value.copy(isPlaying = true)

        playbackJob =
            viewModelScope.launch {

                while (_uiState.value.isPlaying) {

                    delay(50)

                    val currentState = _uiState.value
                    val beats = currentState.beats

                    if (beats.isEmpty()) {
                        continue
                    }

                    val newTime =
                        if (voicePlayer.mediaItemCount > 0) {

                            voicePlayer.currentPosition / 1000f

                        } else {

                            val next =
                                currentState.currentTimeSec + 0.05f

                            if (next >= currentState.totalDurationSec) {
                                0f
                            } else {
                                next
                            }
                        }

                    updateTimelineState(newTime)
                }
            }
    }

    fun pausePlayback() {

        playbackJob?.cancel()

        voicePlayer.pause()
        musicPlayer.pause()

        _uiState.value =
            _uiState.value.copy(
                isPlaying = false,
                audioMeterLevel = 0f
            )
    }

    /*
     * Updates poses, captions and speaker based on the current
     * position in the real audio timeline.
     */
    private fun updateTimelineState(timeSec: Float) {

        val state = _uiState.value
        val beats = state.beats

        if (beats.isEmpty()) return

        var accumulated = 0f
        var targetBeatIndex = beats.lastIndex

        for (i in beats.indices) {

            val beat = beats[i]

            if (
                timeSec >= accumulated &&
                timeSec < accumulated + beat.durationSec
            ) {
                targetBeatIndex = i
                break
            }

            accumulated += beat.durationSec
        }

        val beat = beats[targetBeatIndex]
        val speaker = beat.speaker

        /*
         * This meter is only visual for now.
         * The audio itself is REAL.
         */
        val meter =
            if (_uiState.value.isPlaying) {
                (
                    0.45f +
                        Math.sin(timeSec.toDouble() * 12.0)
                            .toFloat() * 0.25f
                    ).coerceIn(0.15f, 0.9f)
            } else {
                0f
            }

        _uiState.value =
            state.copy(
                currentTimeSec =
                    timeSec.coerceIn(
                        0f,
                        state.totalDurationSec
                    ),
                currentBeatIndex = targetBeatIndex,
                activeSpeaker = speaker,
                activeArchiePose =
                    if (speaker == CharacterType.ARCHIE) {
                        beat.poseTag
                    } else {
                        ArchiePose.IDLE.tag
                    },
                activeJarvisPose =
                    if (speaker == CharacterType.JARVIS) {
                        beat.poseTag
                    } else {
                        JarvisPose.CALM.tag
                    },
                activeCaptionText = beat.text,
                activeSfxCue =
                    beat.sfxCue.takeIf { it != "None" },
                audioMeterLevel = meter
            )
    }

    /*
     * REAL AUDIO SEEK
     */
    fun seekTo(timeSec: Float) {

        val total =
            _uiState.value.totalDurationSec

        val clamped =
            timeSec.coerceIn(0f, total)

        val seekMs =
            (clamped * 1000f).toLong()

        if (voicePlayer.mediaItemCount > 0) {
            voicePlayer.seekTo(seekMs)
        }

        if (musicPlayer.mediaItemCount > 0) {

            val musicDuration =
                musicPlayer.duration

            val musicPosition =
                if (musicDuration > 0) {
                    seekMs % musicDuration
                } else {
                    seekMs
                }

            musicPlayer.seekTo(musicPosition)
        }

        updateTimelineState(clamped)
    }

    fun jumpToBeat(index: Int) {

        val beats =
            _uiState.value.beats

        if (index !in beats.indices) return

        var time = 0f

        for (i in 0 until index) {
            time += beats[i].durationSec
        }

        seekTo(time + 0.05f)
    }

    fun addBeat(
        speaker: CharacterType = CharacterType.ARCHIE,
        text: String = "New dialogue beat..."
    ) {

        val pose =
            DialogueBeat.detectPose(
                speaker,
                text
            )

        val newBeat =
            DialogueBeat(
                id =
                    "b_${
                        UUID.randomUUID()
                            .toString()
                            .take(6)
                    }",
                speaker = speaker,
                text = text,
                poseTag = pose,
                durationSec = 3.5f,
                sfxCue = "None"
            )

        updateBeatsList(
            _uiState.value.beats + newBeat
        )
    }

    fun updateBeat(
        index: Int,
        updated: DialogueBeat
    ) {

        val list =
            _uiState.value.beats
                .toMutableList()

        if (index in list.indices) {

            list[index] = updated

            updateBeatsList(list)
        }
    }

    fun deleteBeat(index: Int) {

        val list =
            _uiState.value.beats
                .toMutableList()

        if (
            index in list.indices &&
            list.size > 1
        ) {

            list.removeAt(index)

            updateBeatsList(list)
        }
    }

    fun autoAnalyzeAllPoses() {

        val updated =
            _uiState.value.beats.map { beat ->

                val detected =
                    DialogueBeat.detectPose(
                        beat.speaker,
                        beat.text
                    )

                beat.copy(
                    poseTag = detected
                )
            }

        _uiState.value =
            _uiState.value.copy(
                isDialogueAutoAnalyzed = true
            )

        updateBeatsList(updated)
    }

    private fun updateBeatsList(
        newList: List<DialogueBeat>
    ) {

        val beatDuration =
            newList.sumOf {
                it.durationSec.toDouble()
            }
                .toFloat()
                .coerceAtLeast(1f)

        val currentProject =
            _uiState.value.project

        val serialized =
            AppDatabase.serializeBeats(newList)

        val timelineDuration =
            if (
                currentProject != null &&
                currentProject.archieAudioDurationSec > 0f
            ) {
                currentProject.archieAudioDurationSec
            } else {
                beatDuration
            }

        _uiState.value =
            _uiState.value.copy(
                beats = newList,
                totalDurationSec = timelineDuration
            )

        if (currentProject != null) {

            val updatedProject =
                currentProject.copy(
                    dialogueJson = serialized,
                    durationSeconds =
                        timelineDuration.toInt(),
                    updatedAt =
                        System.currentTimeMillis()
                )

            _uiState.value =
                _uiState.value.copy(
                    project = updatedProject
                )

            viewModelScope.launch {
                repository.updateProject(
                    updatedProject
                )
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
        captionsEnabled: Boolean? = null,
        subtitleStyle: SubtitleStyle? = null,
        showSafeZoneOverlay: Boolean? = null,
        title: String? = null
    ) {

        val current =
            _uiState.value.project ?: return

        val updated =
            current.copy(
                title =
                    title ?: current.title,
                backgroundTheme =
                    theme?.name
                        ?: current.backgroundTheme,
                musicTrack =
                    musicTrack
                        ?: current.musicTrack,
                musicVolume =
                    musicVolume
                        ?: current.musicVolume,
                voiceVolume =
                    voiceVolume
                        ?: current.voiceVolume,
                audioNormalized =
                    normalized
                        ?: current.audioNormalized,
                duckingEnabled =
                    ducking
                        ?: current.duckingEnabled,
                captionsEnabled =
                    captionsEnabled
                        ?: current.captionsEnabled,
                subtitleStyle =
                    subtitleStyle?.name
                        ?: current.subtitleStyle,
                showSafeZoneOverlay =
                    showSafeZoneOverlay
                        ?: current.showSafeZoneOverlay,
                updatedAt =
                    System.currentTimeMillis()
            )

        _uiState.value =
            _uiState.value.copy(
                project = updated
            )

        /*
         * Apply volume changes immediately during preview.
         */
        voicePlayer.volume =
            updated.voiceVolume.coerceIn(0f, 1f)

        musicPlayer.volume =
            updated.musicVolume.coerceIn(0f, 1f)

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

        val current =
            _uiState.value.project ?: return

        val updated =
            if (isArchie) {

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

        _uiState.value =
            _uiState.value.copy(
                project = updated
            )

        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    /*
     * EXPORT IS STILL THE EXISTING PROTOTYPE.
     *
     * We will replace this with the real Media3 Transformer
     * export engine after preview playback is verified.
     */
    fun startMp4Export(
        resolution: String = "1080x1920",
        fps: Int = 60
    ) {

        if (
            _uiState.value
                .renderState
                .isRendering
        ) return

        pausePlayback()

        val project =
            _uiState.value.project

        val duration =
            _uiState.value.totalDurationSec

        val totalFrames =
            (duration * fps)
                .toInt()
                .coerceAtLeast(300)

        val fileName =
            "${
                project?.title
                    ?.replace(" ", "_")
                    ?: "Archie_Export"
            }_${resolution}_${fps}fps.mp4"

        _uiState.value =
            _uiState.value.copy(
                renderState =
                    RenderState(
                        isRendering = true,
                        progress = 0f,
                        currentFrame = 0,
                        totalFrames = totalFrames,
                        currentStage =
                            "Preparing export...",
                        isComplete = false,
                        exportedFileName =
                            fileName
                    )
            )

        renderJob?.cancel()

        renderJob =
            viewModelScope.launch {

                var frame = 0

                val batchSize =
                    totalFrames / 40 + 1

                while (
                    frame < totalFrames
                ) {

                    delay(80)

                    frame =
                        (
                            frame +
                                batchSize
                            )
                            .coerceAtMost(
                                totalFrames
                            )

                    val progress =
                        frame.toFloat() /
                            totalFrames

                    _uiState.value =
                        _uiState.value.copy(
                            renderState =
                                _uiState.value
                                    .renderState
                                    .copy(
                                        progress =
                                            progress,
                                        currentFrame =
                                            frame,
                                        currentStage =
                                            "Export prototype..."
                                    )
                        )
                }

                delay(300)

                _uiState.value =
                    _uiState.value.copy(
                        renderState =
                            _uiState.value
                                .renderState
                                .copy(
                                    isRendering =
                                        false,
                                    isComplete =
                                        true,
                                    progress =
                                        1f,
                                    currentStage =
                                        "Prototype export complete."
                                )
                    )
            }
    }

    fun dismissRenderModal() {

        _uiState.value =
            _uiState.value.copy(
                renderState =
                    RenderState()
            )
    }

    override fun onCleared() {

        playbackJob?.cancel()
        renderJob?.cancel()

        voicePlayer.release()
        musicPlayer.release()

        super.onCleared()
    }
}
