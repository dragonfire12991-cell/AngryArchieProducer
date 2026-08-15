package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_assets")
data class MediaAssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "CHARACTER_POSE", "BACKGROUND", "AUDIO_VOICE", "MUSIC", "SFX"
    val character: String? = null, // "ARCHIE", "JARVIS", "CUSTOM"
    val poseTag: String? = null,
    val filePath: String,
    val fileSize: Long = 0L,
    val durationSec: Float = 0f,
    val isBuiltIn: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
