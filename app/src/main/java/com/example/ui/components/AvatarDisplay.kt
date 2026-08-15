package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CharacterType
import com.example.ui.theme.ArchieCrimson
import com.example.ui.theme.ArchieCrimsonLight
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBlack
import kotlin.math.roundToInt

@Composable
fun CharacterStageAvatar(
    character: CharacterType,
    poseTag: String,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_motion")
    
    // Speaking bounce / pulse animation
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.06f else 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 220 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    val verticalBob by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSpeaking) -6f else -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 200 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "verticalBob"
    )

    val isArchie = character == CharacterType.ARCHIE
    val brandColor = if (isArchie) ArchieCrimson else JarvisCyan
    val brandLight = if (isArchie) ArchieCrimsonLight else JarvisCyanLight
    val avatarRes = if (isArchie) R.drawable.img_archie_avatar else R.drawable.img_jarvis_avatar

    val poseBadge = when (poseTag) {
        "angry" -> "😡 FURIOUS"
        "shouting" -> "🔥 SHOUTING"
        "facepalm" -> "🤦 FACEPALM"
        "laughing" -> "🤣 MOCKING"
        "talking" -> "🗣️ TALKING"
        "visor_glow" -> "⚡ ANALYZING"
        "sarcastic" -> "😏 SARCASTIC"
        "shocked" -> "💥 SYSTEM OVERHEAT"
        "coffee" -> "☕ WITTY"
        else -> if (isArchie) "😤 SMUG" else "🤖 STANDBY"
    }

    Column(
        modifier = modifier
            .testTag(if (isArchie) "archie_avatar_stage" else "jarvis_avatar_stage")
            .offset { IntOffset(0, verticalBob.roundToInt()) }
            .scale(scalePulse),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active pose pill above avatar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSpeaking) brandColor else StudioBlack.copy(alpha = 0.8f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSpeaking) brandLight else brandColor.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSpeaking) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speaking",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = poseBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Avatar Frame
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(
                    elevation = if (isSpeaking) 16.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = brandColor,
                    spotColor = brandColor
                )
                .border(
                    width = if (isSpeaking) 3.5.dp else 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            brandColor,
                            brandLight,
                            if (isArchie) StudioAmber else Color.White,
                            brandColor
                        )
                    ),
                    shape = CircleShape
                )
                .padding(4.dp)
                .clip(CircleShape)
                .background(StudioBlack)
        ) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = character.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Pose visual accents
            if (poseTag == "angry" || poseTag == "shouting") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ArchieCrimson.copy(alpha = 0.15f))
                )
            } else if (poseTag == "visor_glow") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JarvisCyan.copy(alpha = 0.18f))
                )
            }
        }

        // Name Tag
        Text(
            text = character.displayName,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            color = if (isSpeaking) brandLight else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
