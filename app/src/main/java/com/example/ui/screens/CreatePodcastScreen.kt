package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.model.BackgroundTheme
import com.example.data.model.CharacterType
import com.example.data.model.DialogueBeat
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.ArchieCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ProjectViewModel

@Composable
fun CreatePodcastScreen(
    projectViewModel: ProjectViewModel,
    onBackClick: () -> Unit,
    onProjectCreated: (Long) -> Unit
) {
    var episodeTitle by remember { mutableStateOf("The Archie & Jarvis Show: Episode 43") }
    var selectedDuration by remember { mutableIntStateOf(180) }
    var selectedTheme by remember { mutableStateOf(BackgroundTheme.PODCAST_DESK) }
    var cohostBalance by remember { mutableStateOf("50/50 Equal Banter") }

    val podcastDialogue = listOf(
        DialogueBeat("p1", CharacterType.JARVIS, "Good evening listeners. Welcome to The Archie & Jarvis Broadcast.", "calm", 4.0f, "Intro Sting"),
        DialogueBeat("p2", CharacterType.ARCHIE, "Let's skip the pleasantries! Did you see the latest developer announcement?!", "shouting", 4.5f, "None"),
        DialogueBeat("p3", CharacterType.JARVIS, "I processed the 400-page release notes in 12 milliseconds, Archie.", "sarcastic", 4.0f, "Cyber Chime"),
        DialogueBeat("p4", CharacterType.ARCHIE, "And did you see page 42 where they deprecated my FAVORITE feature?!", "angry", 4.2f, "None"),
        DialogueBeat("p5", CharacterType.JARVIS, "That feature had not been updated since 2008, Archie.", "coffee", 3.8f, "None"),
        DialogueBeat("p6", CharacterType.ARCHIE, "It had CHARACTER, Jarvis! It had soul!", "facepalm", 3.5f, "Mic Thud"),
        DialogueBeat("p7", CharacterType.JARVIS, "It had twelve severe security vulnerabilities and memory leaks.", "visor_glow", 4.2f, "Record Scratch"),
        DialogueBeat("p8", CharacterType.ARCHIE, "Okay fine! But other than that, it was flawless!", "laughing", 3.8f, "None")
    )

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "CREATE PODCAST EPISODE",
                subtitle = "Archie & Jarvis Dual-Host Studio Setup",
                onBackClick = onBackClick
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("create_podcast_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Episode Title Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EPISODE TITLE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = episodeTitle,
                            onValueChange = { episodeTitle = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("podcast_title_input"),
                            placeholder = { Text("e.g. Episode 43: Autonomous Robot Baristas", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = StudioBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Duration Selection (2 min, 3 min, 5 min)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PODCAST TARGET LENGTH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(120 to "2 Minutes\n(Lightning)", 180 to "3 Minutes\n(Standard)", 300 to "5 Minutes\n(Deep Dive)").forEach { (sec, label) ->
                                val selected = selectedDuration == sec
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDuration = sec }
                                        .testTag("podcast_duration_${sec}s"),
                                    color = if (selected) JarvisCyan.copy(alpha = 0.2f) else StudioCardHover,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) JarvisCyan else StudioBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${sec / 60}m",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (selected) JarvisCyan else TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Co-Host Dynamics Mode
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "HOST DYNAMICS & TURNS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioAmber,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "50/50 Equal Banter (Archie Rant vs Jarvis Logic)",
                            "Archie Heavy (70% Archie Outbursts / 30% Jarvis Reality Checks)",
                            "Jarvis Technical Breakdown (Archie Interjections)"
                        ).forEach { mode ->
                            val selected = cohostBalance == mode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { cohostBalance = mode },
                                color = if (selected) StudioAmber.copy(alpha = 0.15f) else StudioCardHover,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (selected) 1.5.dp else 1.dp,
                                    if (selected) StudioAmber else StudioBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) StudioAmber else TextPrimary
                                    )
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = StudioAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Launch Studio
            item {
                Button(
                    onClick = {
                        val dialogueJson = AppDatabase.serializeBeats(podcastDialogue)
                        projectViewModel.createPodcastProject(
                            title = episodeTitle,
                            durationSeconds = selectedDuration,
                            theme = selectedTheme,
                            templateDialogue = dialogueJson,
                            onCreated = onProjectCreated
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("launch_podcast_studio_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LAUNCH PODCAST TIMELINE STUDIO",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
