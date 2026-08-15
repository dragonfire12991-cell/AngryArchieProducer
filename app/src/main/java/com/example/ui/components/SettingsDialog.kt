package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ArchieCrimson
import com.example.ui.theme.BoldPrimary
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsDialog(
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("settings_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = StudioDarkBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = BoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "STUDIO SETTINGS",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Angry Archie Producer v1.0",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.statusMessage != null) {
                    item {
                        Surface(
                            color = StudioGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioGreen.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = state.statusMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioGreen,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Render Specs
                item {
                    Text(
                        text = "VIDEO ENGINE SPECS",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                        color = BoldPrimary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Canvas Output", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = JarvisCyan.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = state.defaultResolution,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = JarvisCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Target Framerate", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = BoldPrimary.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BoldPrimary.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${state.defaultFps} FPS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BoldPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Audio Pipeline
                item {
                    Text(
                        text = "AUDIO NORMALIZER & DUCKING",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                        color = BoldPrimary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("EBU R128 Loudness Target", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text("Auto-normalizes dialogue to standard -14 LUFS", fontSize = 11.sp, color = TextSecondary)
                                }
                                Switch(
                                    checked = state.autoAudioNormalization,
                                    onCheckedChange = { settingsViewModel.toggleAutoAudioNormalization(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = BoldPrimary,
                                        checkedTrackColor = BoldPrimary.copy(alpha = 0.4f),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = StudioCardHover
                                    )
                                )
                            }

                            HorizontalDivider(color = StudioBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dynamic Music Ducking", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text("Lowers BGM by -12dB during speech beats", fontSize = 11.sp, color = TextSecondary)
                                }
                                Switch(
                                    checked = state.autoDucking,
                                    onCheckedChange = { settingsViewModel.toggleAutoDucking(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = JarvisCyan,
                                        checkedTrackColor = JarvisCyan.copy(alpha = 0.4f),
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = StudioCardHover
                                    )
                                )
                            }
                        }
                    }
                }

                // Studio Utilities & Reset
                item {
                    Text(
                        text = "STUDIO ASSET RESET",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                        color = BoldPrimary
                    )
                }

                item {
                    Button(
                        onClick = { settingsViewModel.resetToDefaultSeedData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_sample_data_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioCardHover,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reload Default Poses & Audio Samples", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_settings_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BoldPrimary,
                    contentColor = StudioDarkBg
                ),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    )
}
