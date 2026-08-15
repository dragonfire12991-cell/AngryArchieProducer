package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAmber

@Composable
fun SafeZoneOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            
            // Top Safe Line (TikTok header safe area)
            val topSafeY = size.height * 0.12f
            drawLine(
                color = Color(0x99FFD600),
                start = Offset(0f, topSafeY),
                end = Offset(size.width, topSafeY),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Bottom Safe Line (TikTok captions / sound info safe area)
            val bottomSafeY = size.height * 0.82f
            drawLine(
                color = Color(0x99FFD600),
                start = Offset(0f, bottomSafeY),
                end = Offset(size.width, bottomSafeY),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // Right Safe Line (Engagement buttons safe area)
            val rightSafeX = size.width * 0.82f
            drawLine(
                color = Color(0x77FF5252),
                start = Offset(rightSafeX, topSafeY),
                end = Offset(rightSafeX, bottomSafeY),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )
        }

        // Top Guide Label
        Text(
            text = "9:16 SAFE ZONE (TIKTOK/REELS)",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = StudioAmber,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )

        // Right side engagement hint
        Text(
            text = "LIKE/SHARE SAFE",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF5252),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
