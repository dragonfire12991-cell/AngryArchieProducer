package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BackgroundTheme
import com.example.data.model.CharacterType
import com.example.data.model.DialogueBeat
import com.example.data.model.MediaAssetEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.data.model.SubtitleStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Database(
    entities = [ProjectEntity::class, MediaAssetEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun mediaAssetDao(): MediaAssetDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "angry_archie_producer.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()

                INSTANCE = instance
                instance
            }
        }

        fun getSampleDialogueForShort(): String {

            val beats = listOf(
                DialogueBeat(
                    "b1",
                    CharacterType.ARCHIE,
                    "Jarvis! Explain to me why my render took THREE HOURS!",
                    "angry",
                    3.5f,
                    "Dramatic Hit"
                ),
                DialogueBeat(
                    "b2",
                    CharacterType.JARVIS,
                    "Archie, you attempted to export 8K at 240 frames per second on a toaster.",
                    "sarcastic",
                    4.0f,
                    "Cyber Chime"
                ),
                DialogueBeat(
                    "b3",
                    CharacterType.ARCHIE,
                    "It is NOT a toaster! It is an overclocked dual-fan beast!",
                    "shouting",
                    3.2f,
                    "Error Buzzer"
                ),
                DialogueBeat(
                    "b4",
                    CharacterType.JARVIS,
                    "Technically, it is currently warm enough to make sourdough toast.",
                    "visor_glow",
                    3.8f,
                    "None"
                ),
                DialogueBeat(
                    "b5",
                    CharacterType.ARCHIE,
                    "UGH! Just hit the optimize button before I throw it out the window!",
                    "facepalm",
                    4.0f,
                    "Mic Thud"
                )
            )

            return serializeBeats(beats)
        }

        fun getSampleDialogueForPodcast(): String {

            val beats = listOf(
                DialogueBeat(
                    "p1",
                    CharacterType.JARVIS,
                    "Welcome back to Episode 42 of The Archie & Jarvis Show.",
                    "calm",
                    3.5f,
                    "Intro Sting"
                ),
                DialogueBeat(
                    "p2",
                    CharacterType.ARCHIE,
                    "Today we are addressing people who leave 100 tabs open in Chrome!",
                    "shouting",
                    4.0f,
                    "None"
                ),
                DialogueBeat(
                    "p3",
                    CharacterType.JARVIS,
                    "Archie, you currently have 142 tabs open, including five on 'how to calm down'.",
                    "sarcastic",
                    4.5f,
                    "Cyber Chime"
                ),
                DialogueBeat(
                    "p4",
                    CharacterType.ARCHIE,
                    "Those are for RESEARCH, Jarvis! High priority tabs!",
                    "angry",
                    3.5f,
                    "None"
                ),
                DialogueBeat(
                    "p5",
                    CharacterType.JARVIS,
                    "One tab is a 10-hour loop of cat sneezing sounds.",
                    "visor_glow",
                    3.5f,
                    "Record Scratch"
                ),
                DialogueBeat(
                    "p6",
                    CharacterType.ARCHIE,
                    "It keeps my creative flow stimulated! Don't judge me!",
                    "talking",
                    3.5f,
                    "None"
                ),
                DialogueBeat(
                    "p7",
                    CharacterType.JARVIS,
                    "Of course. My thermal sensors indicate pure inspiration.",
                    "coffee",
                    3.5f,
                    "None"
                ),
                DialogueBeat(
                    "p8",
                    CharacterType.ARCHIE,
                    "Cut the sarcasm, let's roll the intro!",
                    "laughing",
                    3.0f,
                    "Dramatic Hit"
                )
            )

            return serializeBeats(beats)
        }

        fun serializeBeats(beats: List<DialogueBeat>): String {

            val jsonArray = JSONArray()

            for (beat in beats) {

                val obj = JSONObject()

                obj.put("id", beat.id)
                obj.put("speaker", beat.speaker.name)
                obj.put("text", beat.text)
                obj.put("poseTag", beat.poseTag)
                obj.put("durationSec", beat.durationSec.toDouble())
                obj.put("sfxCue", beat.sfxCue)

                jsonArray.put(obj)
            }

            return jsonArray.toString()
        }

        fun deserializeBeats(json: String): List<DialogueBeat> {

            if (json.isBlank()) return emptyList()

            val list = mutableListOf<DialogueBeat>()

            try {

                val array = JSONArray(json)

                for (i in 0 until array.length()) {

                    val obj = array.getJSONObject(i)

                    val speakerName =
                        obj.optString(
                            "speaker",
                            CharacterType.ARCHIE.name
                        )

                    val speaker =
                        try {
                            CharacterType.valueOf(speakerName)
                        } catch (_: Exception) {
                            CharacterType.ARCHIE
                        }

                    list.add(
                        DialogueBeat(
                            id = obj.optString("id", "beat_$i"),
                            speaker = speaker,
                            text = obj.optString("text", ""),
                            poseTag = obj.optString("poseTag", "idle"),
                            durationSec =
                                obj.optDouble(
                                    "durationSec",
                                    3.0
                                ).toFloat(),
                            sfxCue =
                                obj.optString(
                                    "sfxCue",
                                    "None"
                                )
                        )
                    )
                }

            } catch (_: Exception) {
                // Return fallback
            }

            return list
        }

        private class DatabaseCallback : Callback() {

            override fun onCreate(
                db: SupportSQLiteDatabase
            ) {

                super.onCreate(db)

                INSTANCE?.let { database ->

                    CoroutineScope(
                        Dispatchers.IO
                    ).launch {

                        populateInitialData(
                            database
                        )
                    }
                }
            }
        }

        suspend fun populateInitialData(
            database: AppDatabase
        ) {

            val projectDao =
                database.projectDao()

            val mediaDao =
                database.mediaAssetDao()

            // Seed Sample Short Project
            projectDao.insertProject(
                ProjectEntity(
                    id = 1L,
                    title = "Archie Rants: Render Crash",
                    projectType = ProjectType.SHORT.name,
                    durationSeconds = 45,
                    aspectRatio = "9:16",
                    backgroundTheme =
                        BackgroundTheme.STUDIO_NEON.name,
                    musicTrack =
                        "Cyber Hype Synth",
                    musicVolume = 0.35f,
                    voiceVolume = 1.0f,
                    audioNormalized = true,
                    duckingEnabled = true,

                    // Captions default OFF
                    captionsEnabled = false,

                    subtitleStyle =
                        SubtitleStyle.PUNCHY_YELLOW.name,
                    dialogueJson =
                        getSampleDialogueForShort(),
                    archiePositionX = 0.28f,
                    archiePositionY = 0.60f,
                    archieScale = 1.05f,
                    archieFlip = false,
                    jarvisPositionX = 0.72f,
                    jarvisPositionY = 0.58f,
                    jarvisScale = 0.95f,
                    jarvisFlip = false
                )
            )

            // Seed Sample Podcast Project
            projectDao.insertProject(
                ProjectEntity(
                    id = 2L,
                    title = "The Archie & Jarvis Show #42",
                    projectType =
                        ProjectType.PODCAST.name,
                    durationSeconds = 180,
                    aspectRatio = "9:16",
                    backgroundTheme =
                        BackgroundTheme.PODCAST_DESK.name,
                    musicTrack =
                        "Lo-Fi Argument",
                    musicVolume = 0.25f,
                    voiceVolume = 1.0f,
                    audioNormalized = true,
                    duckingEnabled = true,

                    // Captions default OFF
                    captionsEnabled = false,

                    subtitleStyle =
                        SubtitleStyle.NEON_CYAN.name,
                    dialogueJson =
                        getSampleDialogueForPodcast(),
                    archiePositionX = 0.30f,
                    archiePositionY = 0.58f,
                    archieScale = 1.0f,
                    archieFlip = false,
                    jarvisPositionX = 0.70f,
                    jarvisPositionY = 0.58f,
                    jarvisScale = 1.0f,
                    jarvisFlip = false
                )
            )

            // Seed Media Assets
            val initialAssets = listOf(

                MediaAssetEntity(
                    name = "Archie - Furious Rage",
                    category = "CHARACTER_POSE",
                    character = "ARCHIE",
                    poseTag = "angry",
                    filePath = "drawable/img_archie_avatar",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Archie - Loud Shouting",
                    category = "CHARACTER_POSE",
                    character = "ARCHIE",
                    poseTag = "shouting",
                    filePath = "drawable/img_archie_avatar",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Archie - Sarcastic Smirk",
                    category = "CHARACTER_POSE",
                    character = "ARCHIE",
                    poseTag = "idle",
                    filePath = "drawable/img_archie_avatar",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Archie - Disbelief Facepalm",
                    category = "CHARACTER_POSE",
                    character = "ARCHIE",
                    poseTag = "facepalm",
                    filePath = "drawable/img_archie_avatar",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Jarvis - Visor Calculating",
                    category = "CHARACTER_POSE",
                    character = "JARVIS",
                    poseTag = "visor_glow",
                    filePath = "drawable/img_jarvis_avatar",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Jarvis - Dry Wit Smirk",
                    category = "CHARACTER_POSE",
                    character = "JARVIS",
                    poseTag = "sarcastic",
                    filePath = "drawable/img_jarvis_avatar",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Jarvis - Calm Neutral",
                    category = "CHARACTER_POSE",
                    character = "JARVIS",
                    poseTag = "calm",
                    filePath = "drawable/img_jarvis_avatar",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Jarvis - Error Shocked",
                    category = "CHARACTER_POSE",
                    character = "JARVIS",
                    poseTag = "shocked",
                    filePath = "drawable/img_jarvis_avatar",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Studio Neon Backdrop",
                    category = "BACKGROUND",
                    filePath = "drawable/img_studio_banner",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Late Night Broadcast Desk",
                    category = "BACKGROUND",
                    filePath = "theme/podcast_desk",
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Cyber Hype Synth (128 BPM)",
                    category = "MUSIC",
                    filePath = "audio/synth_hype",
                    durationSec = 60f,
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Lo-Fi Chill Argument (85 BPM)",
                    category = "MUSIC",
                    filePath = "audio/lofi_argue",
                    durationSec = 180f,
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Dramatic Hit Impact",
                    category = "SFX",
                    filePath = "sfx/dramatic_hit",
                    durationSec = 1.5f,
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Cyber Chime Alert",
                    category = "SFX",
                    filePath = "sfx/cyber_chime",
                    durationSec = 1.2f,
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Error Buzzer Glitch",
                    category = "SFX",
                    filePath = "sfx/error_buzzer",
                    durationSec = 0.8f,
                    isBuiltIn = true
                ),

                MediaAssetEntity(
                    name = "Mic Thud Slap",
                    category = "SFX",
                    filePath = "sfx/mic_thud",
                    durationSec = 0.6f,
                    isBuiltIn = true
                )
            )

            mediaDao.insertAssets(
                initialAssets
            )
        }
    }
}
