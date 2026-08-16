package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val projectType: String, // "SHORT" or "PODCAST"
    val durationSeconds: Int,
    val aspectRatio: String = "9:16",
    val resolution: String = "1080x1920",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val backgroundTheme: String = BackgroundTheme.STUDIO_NEON.name,
    val customBackgroundPath: String? = null,
    val customBackgroundName: String? = null,
    val archieAudioPath: String? = null,
    val archieAudioName: String? = null,
    val archieAudioDurationSec: Float = 0f,
    val backgroundMusicPath: String? = null,
    val backgroundMusicName: String? = null,
    val customArchiePosesJson: String = "",
    val musicTrack: String = "Cyber Hype Synth",
    val musicVolume: Float = 0.35f,
    val voiceVolume: Float = 1.0f,
    val audioNormalized: Boolean = true,
    val duckingEnabled: Boolean = true,

    // Captions are OFF by default.
    val captionsEnabled: Boolean = false,

    val subtitleStyle: String = SubtitleStyle.PUNCHY_YELLOW.name,
    val showSafeZoneOverlay: Boolean = false,
    val dialogueJson: String = "",
    val archiePositionX: Float = 0.25f,
    val archiePositionY: Float = 0.55f,
    val archieScale: Float = 1.0f,
    val archieFlip: Boolean = false,
    val jarvisPositionX: Float = 0.75f,
    val jarvisPositionY: Float = 0.55f,
    val jarvisScale: Float = 1.0f,
    val jarvisFlip: Boolean = false
)
