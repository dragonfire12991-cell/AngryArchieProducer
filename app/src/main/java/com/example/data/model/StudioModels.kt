package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ProjectType(val displayName: String, val badgeColor: Long) {
    SHORT("9:16 Short", 0xFFFF3B30),
    PODCAST("Podcast Episode", 0xFF00E5FF)
}

enum class CharacterType(val displayName: String, val defaultColor: Long) {
    ARCHIE("Angry Archie", 0xFFFF5252),
    JARVIS("Jarvis AI", 0xFF40C4FF),
    NARRATOR("Narrator", 0xFFFFD600)
}

enum class ArchiePose(val title: String, val description: String, val tag: String) {
    IDLE("Normal / Smug", "Calm before the storm, slight smirk", "idle"),
    TALKING("Talking / Explaining", "Active mouth open gesture", "talking"),
    ANGRY("Furious / Red", "Eyes blazing, teeth clenched", "angry"),
    SHOUTING("Shouting / Screaming", "Full volume rant mode", "shouting"),
    FACEPALM("Facepalm / Done", "Hand over face in disbelief", "facepalm"),
    LAUGHING("Laughing / Mocking", "Uncontrollable mocking laugh", "laughing")
}

enum class JarvisPose(val title: String, val description: String, val tag: String) {
    CALM("Calm / Neutral", "Sophisticated AI standby", "calm"),
    TALKING("Explaining / Logic", "Calculating vocal projection", "talking"),
    VISOR_GLOW("Visor Glow / Analyzing", "Deep computation mode", "visor_glow"),
    SARCASTIC("Sarcastic Smirk", "Subtle dry wit expression", "sarcastic"),
    SHOCKED("Shocked / Overheat", "System processing error look", "shocked"),
    COFFEE_SIP("Cool / Composed", "Polished witty detachment", "coffee")
}

enum class BackgroundTheme(val displayName: String, val color1: Long, val color2: Long) {
    STUDIO_NEON("Cyber Studio Neon", 0xFF180A1F, 0xFF0D1B2A),
    PODCAST_DESK("Late Night Broadcast", 0xFF141724, 0xFF0B0D13),
    NEWS_CHAOS("Breaking News Desk", 0xFF2A0808, 0xFF150A10),
    DARK_LOUNGE("Retro Synth Lounge", 0xFF0F172A, 0xFF1E1B4B),
    GRID_MATRIX("Hacker Terminal Grid", 0xFF051C14, 0xFF081215)
}

enum class SubtitleStyle(val displayName: String, val textColor: Long, val highlightColor: Long) {
    PUNCHY_YELLOW("Punchy Yellow (TikTok)", 0xFFFFFFFF, 0xFFFFEB3B),
    NEON_CYAN("Cyber Cyan Glow", 0xFFE0F7FA, 0xFF00E5FF),
    BOLD_RED("Archie Fury Red", 0xFFFFF5F5, 0xFFFF3B30),
    CLASSIC_WHITE("Clean Studio White", 0xFFFFFFFF, 0xFF90CAF9)
}

data class DialogueBeat(
    val id: String,
    val speaker: CharacterType,
    val text: String,
    val poseTag: String,
    val durationSec: Float,
    val sfxCue: String = "None"
) {
    companion object {
        fun detectPose(speaker: CharacterType, text: String): String {
            val upper = text.uppercase()
            return when (speaker) {
                CharacterType.ARCHIE -> when {
                    upper.contains("WHAT?!") || upper.contains("SCREAM") || upper.contains("SHUT UP") || upper.contains("NEVER") -> ArchiePose.SHOUTING.tag
                    upper.contains("IDIOT") || upper.contains("WRONG") || upper.contains("TERRIBLE") || upper.contains("HATE") || upper.contains("ANGRY") || upper.contains("!") -> ArchiePose.ANGRY.tag
                    upper.contains("UGH") || upper.contains("FACEPALM") || upper.contains("CANNOT BELIEVE") || upper.contains("WHY") -> ArchiePose.FACEPALM.tag
                    upper.contains("HAHA") || upper.contains("LOL") || upper.contains("FUNNY") || upper.contains("JOKE") -> ArchiePose.LAUGHING.tag
                    upper.length > 20 -> ArchiePose.TALKING.tag
                    else -> ArchiePose.IDLE.tag
                }
                CharacterType.JARVIS -> when {
                    upper.contains("STATISTIC") || upper.contains("CALCULATING") || upper.contains("DATA") || upper.contains("LOGICAL") || upper.contains("ANALYSIS") -> JarvisPose.VISOR_GLOW.tag
                    upper.contains("OBVIOUSLY") || upper.contains("CLEARLY") || upper.contains("IRONIC") || upper.contains("FASCINATING") -> JarvisPose.SARCASTIC.tag
                    upper.contains("ERROR") || upper.contains("WARNING") || upper.contains("IMPOSSIBLE") || upper.contains("EXPLODE") -> JarvisPose.SHOCKED.tag
                    upper.length > 20 -> JarvisPose.TALKING.tag
                    else -> JarvisPose.CALM.tag
                }
                CharacterType.NARRATOR -> "idle"
            }
        }
    }
}
