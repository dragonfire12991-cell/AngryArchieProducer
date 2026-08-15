package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.StudioTopBar
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
import com.example.ui.viewmodel.ProjectViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    projectViewModel: ProjectViewModel,
    onCreateShortClick: () -> Unit,
    onCreatePodcastClick: () -> Unit,
    onMediaLibraryClick: () -> Unit,
    onProjectsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenProject: (Long) -> Unit
) {
    val projectState by projectViewModel.uiState.collectAsStateWithLifecycle()
    val projectCount = projectState.projects.size
    val recentProjects = projectState.projects.take(2)

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "ANGRY ARCHIE PRODUCER",
                subtitle = "Private AI-Assisted 9:16 Video & Podcast Studio",
                onSettingsClick = onSettingsClick
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Studio Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_studio_banner),
                            contentDescription = "Production Studio Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        )
                        // Gradient darkening overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            StudioBlack.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Surface(
                                color = ArchieCrimson,
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "AI VIDEO ENGINE READY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "ANGRY ARCHIE PRODUCER",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Automated avatar pose switching, 9:16 vertical canvas, captions, and audio mixer.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Section: Four Primary Action Buttons
            item {
                Text(
                    text = "STUDIO WORKBENCH",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.8.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Button 1: Create Short
                        StudioActionCard(
                            title = "Create Short",
                            subtitle = "30–60s 9:16 Vertical Video with automated Archie rants & viral hooks",
                            icon = Icons.Default.SmartDisplay,
                            accentColor = ArchieCrimson,
                            tag = "create_short_button",
                            badgeText = "9:16 SHORTS",
                            modifier = Modifier.weight(1f),
                            onClick = onCreateShortClick
                        )

                        // Button 2: Create Podcast
                        StudioActionCard(
                            title = "Create Podcast",
                            subtitle = "Longer Archie & Jarvis banter, witty dialogue engine & dual audio tracks",
                            icon = Icons.Default.Mic,
                            accentColor = JarvisCyan,
                            tag = "create_podcast_button",
                            badgeText = "PODCAST",
                            modifier = Modifier.weight(1f),
                            onClick = onCreatePodcastClick
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Button 3: Media Library
                        StudioActionCard(
                            title = "Media Library",
                            subtitle = "Character PNG poses, studio backgrounds, voiceover WAV/MP3 & SFX",
                            icon = Icons.Default.PermMedia,
                            accentColor = StudioAmber,
                            tag = "media_library_button",
                            badgeText = "ASSETS",
                            modifier = Modifier.weight(1f),
                            onClick = onMediaLibraryClick
                        )

                        // Button 4: Projects
                        StudioActionCard(
                            title = "Projects",
                            subtitle = "Browse $projectCount saved production timelines, duplicate or render MP4",
                            icon = Icons.Default.Folder,
                            accentColor = StudioGreen,
                            tag = "projects_button",
                            badgeText = "$projectCount SAVED",
                            modifier = Modifier.weight(1f),
                            onClick = onProjectsClick
                        )
                    }
                }
            }

            // Quick Studio Settings Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSettingsClick)
                        .testTag("home_settings_bar"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(StudioCardHover, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Studio Configuration & Defaults",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "1080x1920 60 FPS • -14 LUFS Auto-Normalizer • Safe Zone Margins",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Settings",
                            tint = TextMuted
                        )
                    }
                }
            }

            // Recent Projects Quick Access
            if (recentProjects.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT PRODUCTIONS",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.5.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "View All ($projectCount)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            modifier = Modifier
                                .clickable(onClick = onProjectsClick)
                                .padding(4.dp)
                        )
                    }
                }

                items(recentProjects.size) { index ->
                    val project = recentProjects[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProject(project.id) }
                            .testTag("recent_project_card_$index"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (project.projectType == "SHORT") ArchieCrimson.copy(alpha = 0.2f)
                                            else JarvisCyan.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (project.projectType == "SHORT") Icons.Default.SmartDisplay else Icons.Default.Mic,
                                        contentDescription = project.projectType,
                                        tint = if (project.projectType == "SHORT") ArchieCrimson else JarvisCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = project.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${if (project.projectType == "SHORT") "9:16 Short" else "Podcast"} • ${project.durationSeconds}s duration • ${project.backgroundTheme}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { onOpenProject(project.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioCardHover,
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(100.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Open Studio",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudioActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    badgeText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(170.dp)
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(100.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    letterSpacing = 0.2.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
            }
        }
    }
}
