package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArchieCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.StudioAmber
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    meterLevel: Float,
    isArchieSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val primaryColor = if (isArchieSpeaking) ArchieCrimson else JarvisCyan
    val secondaryColor = if (isArchieSpeaking) StudioAmber else JarvisCyan

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val barCount = 32
        val barWidth = size.width / (barCount * 1.5f)
        val gap = barWidth * 0.5f

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount
            val wave = if (isPlaying) {
                val s1 = sin((normalizedIndex * 4f * Math.PI + phase).toDouble()).toFloat()
                val s2 = sin((normalizedIndex * 8f * Math.PI - phase * 1.5).toDouble()).toFloat()
                ((s1 + s2) * 0.5f + 1f) * 0.5f * meterLevel
            } else {
                0.08f
            }

            val barHeight = (size.height * wave.coerceIn(0.08f, 0.95f))
            val left = i * (barWidth + gap)
            val top = (size.height - barHeight) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(primaryColor, secondaryColor)
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
