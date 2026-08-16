package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.io.File
import com.example.data.model.ArchiePose
import com.example.data.model.BackgroundTheme
import com.example.data.model.CharacterType
import com.example.data.model.DialogueBeat
import com.example.data.model.JarvisPose
import com.example.data.model.SubtitleStyle
import com.example.ui.components.CharacterStageAvatar
import com.example.ui.components.SafeZoneOverlay
import com.example.ui.components.StudioTopBar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.ArchieCrimson
import com.example.ui.theme.ArchieCrimsonLight
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderGlow
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.StudioEditorUiState
import com.example.ui.viewmodel.StudioEditorViewModel
import kotlin.math.roundToInt

@Composable
fun StudioEditorScreen(
    projectId: Long,
    editorViewModel: StudioEditorViewModel,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val state by editorViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        editorViewModel.loadProject(projectId)
    }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = state.project?.title ?: "STUDIO TIMELINE",
                subtitle = "${state.project?.projectType ?: "9:16"} • ${String.format("%.1f", state.currentTimeSec)}s / ${String.format("%.1f", state.totalDurationSec)}s",
                onBackClick = onBackClick,
                onSettingsClick = onSettingsClick,
                actions = {
                    Button(
                        onClick = { editorViewModel.setActiveTab(4) }, // Open Export Tab
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArchieCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("top_export_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export MP4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("studio_editor_root")
        ) {
            val isLandscapeTablet = maxWidth >= 840.dp

            if (isLandscapeTablet) {
                // Dual Pane Studio Workbench for Tablets in Landscape
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Pane: 9:16 Canvas Monitor & Transport Controls
                    Box(
                        modifier = Modifier
                            .weight(1.6f)
                            .fillMaxHeight()
                            .background(StudioBlack)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CanvasWorkbenchColumn(
                            state = state,
                            viewModel = editorViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(StudioBorder)
                    )

                    // Right Pane: Studio Tabs & Workstation Controls
                    Column(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                            .background(StudioDarkBg)
                    ) {
                        StudioTabControl(
                            activeTab = state.activeTab,
                            onTabSelected = { editorViewModel.setActiveTab(it) }
                        )

                        StudioTabContent(
                            state = state,
                            viewModel = editorViewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
            } else {
                // Stacked Layout for Portrait
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(520.dp)
                            .background(StudioBlack)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CanvasWorkbenchColumn(
                            state = state,
                            viewModel = editorViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    StudioTabControl(
                        activeTab = state.activeTab,
                        onTabSelected = { editorViewModel.setActiveTab(it) }
                    )

                    StudioTabContent(
                        state = state,
                        viewModel = editorViewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        // MP4 Render Progress Dialog
        if (state.renderState.isRendering || state.renderState.isComplete) {
            RenderProgressModal(
                renderState = state.renderState,
                onDismiss = { editorViewModel.dismissRenderModal() }
            )
        }
    }
}

@Composable
fun CanvasWorkbenchColumn(
    state: StudioEditorUiState,
    viewModel: StudioEditorViewModel,
    modifier: Modifier = Modifier
) {
    val project = state.project
    val theme = try {
        BackgroundTheme.valueOf(project?.backgroundTheme ?: BackgroundTheme.STUDIO_NEON.name)
    } catch (_: Exception) {
        BackgroundTheme.STUDIO_NEON
    }
    val subStyle = try {
        SubtitleStyle.valueOf(project?.subtitleStyle ?: SubtitleStyle.PUNCHY_YELLOW.name)
    } catch (_: Exception) {
        SubtitleStyle.PUNCHY_YELLOW
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 9:16 Vertical Video Screen Frame
        Card(
            modifier = Modifier
                .weight(1f, fill = false)
                .aspectRatio(9f / 16f)
                .shadow(16.dp, RoundedCornerShape(18.dp))
                .testTag("vertical_canvas_screen"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = StudioBlack),
            border = androidx.compose.foundation.BorderStroke(2.dp, StudioBorderGlow)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(theme.color1), Color(theme.color2))
                        )
                    )
            ) {
                // Custom Background Image if present
                if (!project?.customBackgroundPath.isNullOrBlank()) {
                    AsyncImage(
                        model = File(project!!.customBackgroundPath!!),
                        contentDescription = "Custom Background Scene",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Character Avatars on Stage
                // Archie (Left host)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        CharacterStageAvatar(
                            character = CharacterType.ARCHIE,
                            poseTag = state.activeArchiePose,
                            isSpeaking = state.isPlaying && state.activeSpeaker == CharacterType.ARCHIE,
                            modifier = Modifier
                                .scale(project?.archieScale ?: 1.0f)
                                .offset(x = 0.dp, y = 0.dp)
                        )

                        CharacterStageAvatar(
                            character = CharacterType.JARVIS,
                            poseTag = state.activeJarvisPose,
                            isSpeaking = state.isPlaying && state.activeSpeaker == CharacterType.JARVIS,
                            modifier = Modifier
                                .scale(project?.jarvisScale ?: 1.0f)
                                .offset(x = 0.dp, y = 0.dp)
                        )
                    }
                }

                // Dynamic Captions / Subtitle Overlay (TikTok / Reels Style)
                if (project?.captionsEnabled == true && state.activeCaptionText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                            .offset(y = 50.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(subStyle.highlightColor).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = state.activeCaptionText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = Color(subStyle.textColor),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Safe Zone Overlays (TikTok/Reels Guides)
                if (project?.showSafeZoneOverlay == true) {
                    SafeZoneOverlay()
                }

                // SFX Flash Cue
                if (state.activeSfxCue != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = ArchieCrimson,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "⚡ ${state.activeSfxCue}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transport Controls & Live Audio Waveform
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Waveform meter
                WaveformVisualizer(
                    isPlaying = state.isPlaying,
                    meterLevel = state.audioMeterLevel,
                    isArchieSpeaking = state.activeSpeaker == CharacterType.ARCHIE
                )

                // Seek Scrubber
                Slider(
                    value = state.currentTimeSec,
                    onValueChange = { viewModel.seekTo(it) },
                    valueRange = 0f..state.totalDurationSec.coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = if (state.activeSpeaker == CharacterType.ARCHIE) ArchieCrimson else JarvisCyan,
                        activeTrackColor = if (state.activeSpeaker == CharacterType.ARCHIE) ArchieCrimson else JarvisCyan,
                        inactiveTrackColor = StudioBorder
                    ),
                    modifier = Modifier.height(24.dp)
                )

                // Playback Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.1f", state.currentTimeSec)}s / ${String.format("%.1f", state.totalDurationSec)}s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.seekTo(0f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "Restart", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = { viewModel.togglePlayback() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (state.activeSpeaker == CharacterType.ARCHIE) ArchieCrimson else JarvisCyan,
                                    CircleShape
                                )
                                .testTag("transport_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = StudioBlack,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val nextIndex = (state.currentBeatIndex + 1).coerceAtMost(state.beats.lastIndex)
                                viewModel.jumpToBeat(nextIndex)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Next Beat", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Safe Zone Toggle Button
                    IconButton(
                        onClick = {
                            viewModel.updateProjectSettings(
                                showSafeZoneOverlay = !(project?.showSafeZoneOverlay ?: false)
                            )
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Safe Zones",
                            tint = if (project?.showSafeZoneOverlay == true) StudioAmber else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioTabControl(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        "TIMELINE / SCRIPT" to Icons.Default.ViewCarousel,
        "STAGE / POSES" to Icons.Default.Movie,
        "AUDIO MIXER" to Icons.Default.GraphicEq,
        "CAPTIONS" to Icons.Default.Subtitles,
        "EXPORT MP4" to Icons.Default.Download
    )

    ScrollableTabRow(
        selectedTabIndex = activeTab,
        containerColor = StudioBlack,
        contentColor = TextPrimary,
        edgePadding = 12.dp,
        indicator = {},
        divider = {}
    ) {
        tabs.forEachIndexed { index, (title, icon) ->
            val isSelected = activeTab == index
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .clickable { onTabSelected(index) }
                    .testTag("studio_tab_$index"),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) ArchieCrimson else StudioCardBg,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) ArchieCrimsonLight else StudioBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun StudioTabContent(
    state: StudioEditorUiState,
    viewModel: StudioEditorViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state.activeTab) {
            0 -> TimelineTabContent(state = state, viewModel = viewModel)
            1 -> StagePosesTabContent(state = state, viewModel = viewModel)
            2 -> AudioMixerTabContent(state = state, viewModel = viewModel)
            3 -> CaptionsTabContent(state = state, viewModel = viewModel)
            4 -> ExportStudioTabContent(state = state, viewModel = viewModel)
        }
    }
}

@Composable
fun TimelineTabContent(
    state: StudioEditorUiState,
    viewModel: StudioEditorViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("timeline_tab_content"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // AI Auto-Analyze Pose Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUTOMATIC DIALOGUE POSE ENGINE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = StudioAmber
                            )
                        }
                        Text(
                            text = "Auto-assigns Archie & Jarvis emotion poses from script keywords.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = { viewModel.autoAnalyzeAllPoses() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioAmber,
                            contentColor = StudioBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("auto_analyze_poses_button")
                    ) {
                        Text("Auto-Pose", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Dialogue Beat Items
        items(state.beats.size) { index ->
            val beat = state.beats[index]
            val isCurrent = state.currentBeatIndex == index

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.jumpToBeat(index) }
                    .testTag("timeline_beat_card_$index"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) StudioCardHover else StudioCardBg
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (isCurrent) 2.dp else 1.dp,
                    if (isCurrent) (if (beat.speaker == CharacterType.ARCHIE) ArchieCrimson else JarvisCyan) else StudioBorder
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header: Speaker Selector & Duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (beat.speaker == CharacterType.ARCHIE) ArchieCrimson else JarvisCyan,
                                modifier = Modifier.clickable {
                                    val nextSpeaker = if (beat.speaker == CharacterType.ARCHIE) CharacterType.JARVIS else CharacterType.ARCHIE
                                    viewModel.updateBeat(index, beat.copy(speaker = nextSpeaker))
                                }
                            ) {
                                Text(
                                    text = beat.speaker.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (beat.speaker == CharacterType.ARCHIE) Color.White else StudioBlack,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pose: ${beat.poseTag.uppercase()}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${beat.durationSec}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            if (state.beats.size > 1) {
                                IconButton(
                                    onClick = { viewModel.deleteBeat(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Beat", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text Input Editor
                    OutlinedTextField(
                        value = beat.text,
                        onValueChange = { updatedText ->
                            viewModel.updateBeat(index, beat.copy(text = updatedText))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("beat_text_input_$index"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (beat.speaker == CharacterType.ARCHIE) ArchieCrimson else JarvisCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pose Chips for this beat
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (beat.speaker == CharacterType.ARCHIE) {
                            items(ArchiePose.values().size) { pIndex ->
                                val pose = ArchiePose.values()[pIndex]
                                val selected = beat.poseTag == pose.tag
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selected) ArchieCrimson else StudioBlack,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selected) ArchieCrimsonLight else StudioBorder
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.updateBeat(index, beat.copy(poseTag = pose.tag))
                                    }
                                ) {
                                    Text(
                                        text = pose.title.split("/").first().trim(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            items(JarvisPose.values().size) { pIndex ->
                                val pose = JarvisPose.values()[pIndex]
                                val selected = beat.poseTag == pose.tag
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selected) JarvisCyan else StudioBlack,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selected) JarvisCyanLight else StudioBorder
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.updateBeat(index, beat.copy(poseTag = pose.tag))
                                    }
                                ) {
                                    Text(
                                        text = pose.title.split("/").first().trim(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) StudioBlack else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Dialogue Beat Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.addBeat(CharacterType.ARCHIE, "Archie: Outrageous point!") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArchieCrimson.copy(alpha = 0.2f),
                        contentColor = ArchieCrimson
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArchieCrimson),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Archie Beat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.addBeat(CharacterType.JARVIS, "Jarvis: Logical counterpoint.") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan.copy(alpha = 0.2f),
                        contentColor = JarvisCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Jarvis Beat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StagePosesTabContent(
    state: StudioEditorUiState,
    viewModel: StudioEditorViewModel
) {
    val project = state.project

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("stage_poses_tab_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Background Scene Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STUDIO BACKGROUND THEME",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BackgroundTheme.values().forEach { theme ->
                            val isSelected = project?.backgroundTheme == theme.name
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.updateProjectSettings(theme = theme) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) JarvisCyan.copy(alpha = 0.2f) else StudioCardHover,
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) JarvisCyan else StudioBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(theme.color1), Color(theme.color2))
                                                ),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = theme.displayName.split(" ").first(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) JarvisCyan else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Archie Stage Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ARCHIE STAGE SCALE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArchieCrimson
                        )
                        Text(
                            text = "${((project?.archieScale ?: 1.0f) * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                    Slider(
                        value = project?.archieScale ?: 1.0f,
                        onValueChange = {
                            viewModel.updateCharacterPosition(
                                isArchie = true,
                                posX = project?.archiePositionX ?: 0.25f,
                                posY = project?.archiePositionY ?: 0.55f,
                                scale = it,
                                flip = project?.archieFlip ?: false
                            )
                        },
                        valueRange = 0.6f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArchieCrimson,
                            activeTrackColor = ArchieCrimson,
                            inactiveTrackColor = StudioBorder
                        )
                    )
                }
            }
        }

        // Jarvis Stage Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JARVIS STAGE SCALE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan
                        )
                        Text(
                            text = "${((project?.jarvisScale ?: 1.0f) * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                    Slider(
                        value = project?.jarvisScale ?: 1.0f,
                        onValueChange = {
                            viewModel.updateCharacterPosition(
                                isArchie = false,
                                posX = project?.jarvisPositionX ?: 0.75f,
                                posY = project?.jarvisPositionY ?: 0.55f,
                                scale = it,
                                flip = project?.jarvisFlip ?: false
                            )
                        },
                        valueRange = 0.6f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = StudioBorder
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AudioMixerTabContent(
    state: StudioEditorUiState,
    viewModel: StudioEditorViewModel
) {
    val project = state.project

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("audio_mixer_tab_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE AUDIO MIXER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioAmber
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Voice / Dialogue",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = project?.archieAudioName ?: "No dialogue audio imported",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VOICE VOLUME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArchieCrimson
                        )
                        Text(
                            text = "${(((project?.voiceVolume ?: 1.0f) * 100f).roundToInt())}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    Slider(
                        value = project?.voiceVolume ?: 1.0f,
                        onValueChange = { viewModel.updateProjectSettings(voiceVolume = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArchieCrimson,
                            activeTrackColor = ArchieCrimson,
                            inactiveTrackColor = StudioBorder
                        ),
                        modifier = Modifier.testTag("voice_volume_slider")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Background Music",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = project?.backgroundMusicName ?: "No background music imported",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MUSIC VOLUME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan
                        )
                        Text(
                            text = "${(((project?.musicVolume ?: 0.35f) * 100f).roundToInt())}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    Slider(
                        value = project?.musicVolume ?: 0.35f,
                        onValueChange = { viewModel.updateProjectSettings(musicVolume = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = StudioBorder
                        ),
                        modifier = Modifier.testTag("music_volume_slider")
                    )

                    Text(
                        text = "The preview players use these levels immediately during playback.",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AUDIO PROCESSING ENGINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Audio Normalization (-14 LUFS)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Saved as an export preference. Real normalization will be applied when the export engine is implemented.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = project?.audioNormalized ?: true,
                            onCheckedChange = { viewModel.updateProjectSettings(normalized = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StudioGreen,
                                checkedTrackColor = StudioGreen.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Ducking",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Saved as an export preference. Preview currently uses the manual music-volume level above.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = project?.duckingEnabled ?: true,
                            onCheckedChange = { viewModel.updateProjectSettings(ducking = it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StudioGreen,
                                checkedTrackColor = StudioGreen.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaptionsTabContent(
    state: StudioEditorUiState,
    viewModel: StudioEditorViewModel
) {
    val project = state.project
    val captionsEnabled = project?.captionsEnabled ?: false

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("captions_tab_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (captionsEnabled) StudioAmber else StudioBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CAPTIONS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (captionsEnabled) StudioAmber else TextPrimary
                        )
                        Text(
                            text = if (captionsEnabled) {
                                "Captions are ON and will appear in the preview."
                            } else {
                                "Captions are OFF. The preview stays completely clear."
                            },
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = captionsEnabled,
                        onCheckedChange = {
                            viewModel.updateProjectSettings(captionsEnabled = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StudioBlack,
                            checkedTrackColor = StudioAmber,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = StudioBorder
                        ),
                        modifier = Modifier.testTag("captions_enabled_switch")
                    )
                }
            }
        }

        if (captionsEnabled) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SUBTITLE CAPTION STYLE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioAmber
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SubtitleStyle.values().forEach { style ->
                            val isSelected = project?.subtitleStyle == style.name

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.updateProjectSettings(
                                            subtitleStyle = style
                                        )
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color =
                                    if (isSelected) {
                                        StudioAmber.copy(alpha = 0.15f)
                                    } else {
                                        StudioCardHover
                                    },
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) StudioAmber else StudioBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(
                                                    Color(style.highlightColor),
                                                    CircleShape
                                                )
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Text(
                                            text = style.displayName,
                                            fontSize = 13.sp,
                                            fontWeight =
                                                if (isSelected) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                },
                                            color =
                                                if (isSelected) {
                                                    StudioAmber
                                                } else {
                                                    TextPrimary
                                                }
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = StudioAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CAPTION LAYOUT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Caption size and vertical-position controls are the next caption upgrade. For this build the important fix is that captions can be completely disabled and no longer block the preview.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportStudioTabContent(
    state: StudioEditorUiState,
    viewModel: StudioEditorViewModel
) {
    var selectedRes by remember { mutableStateOf("1080x1920 (FHD 9:16)") }
    var selectedFps by remember { mutableStateOf(60) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("export_studio_tab_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MP4 VIDEO ENCODING SETTINGS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArchieCrimson
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Target Resolution:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1080x1920 (FHD 9:16)", "720x1280 (Fast)").forEach { res ->
                            val isSelected = selectedRes == res
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRes = res },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) ArchieCrimson.copy(alpha = 0.2f) else StudioCardHover,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ArchieCrimson else StudioBorder
                                )
                            ) {
                                Text(
                                    text = res,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) ArchieCrimson else TextPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Frame Rate:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60).forEach { fps ->
                            val isSelected = selectedFps == fps
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFps = fps },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) ArchieCrimson.copy(alpha = 0.2f) else StudioCardHover,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ArchieCrimson else StudioBorder
                                )
                            ) {
                                Text(
                                    text = "$fps FPS (Smooth)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) ArchieCrimson else TextPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.startMp4Export(resolution = selectedRes.split(" ").first(), fps = selectedFps) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_mp4_render_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArchieCrimson,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RENDER & EXPORT MP4 VIDEO",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun RenderProgressModal(
    renderState: com.example.ui.viewmodel.RenderState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (renderState.isComplete) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (renderState.isComplete) Icons.Default.Check else Icons.Default.Movie,
                    contentDescription = null,
                    tint = if (renderState.isComplete) StudioGreen else ArchieCrimson
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (renderState.isComplete) "EXPORT SUCCESSFUL" else "RENDERING 9:16 MP4",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!renderState.isComplete) {
                    LinearProgressIndicator(
                        progress = { renderState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = ArchieCrimson,
                        trackColor = StudioBorder
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${(renderState.progress * 100).toInt()}% • Frame ${renderState.currentFrame} / ${renderState.totalFrames}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = renderState.currentStage,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                } else {
                    Text(
                        text = "Video exported: ${renderState.exportedFileName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The MP4 file has been stored in local app storage. Ready for sharing to TikTok, Shorts, and Reels.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            if (renderState.isComplete) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioGreen, contentColor = StudioBlack)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = StudioCardBg,
        textContentColor = TextPrimary
    )
}
