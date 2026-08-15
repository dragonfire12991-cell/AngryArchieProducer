package com.example.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.BackgroundTheme
import com.example.data.model.CharacterType
import com.example.data.model.DialogueBeat
import com.example.data.model.MediaAssetEntity
import com.example.data.util.ImportedFileResult
import com.example.data.util.MediaStorageManager
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.ArchieCrimson
import com.example.ui.theme.ArchieCrimsonLight
import com.example.ui.theme.BoldPrimary
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderGlow
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ProjectViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class PoseItem(
    val originalName: String,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val tag: String
)

@Composable
fun CreateShortScreen(
    projectViewModel: ProjectViewModel,
    onBackClick: () -> Unit,
    onProjectCreated: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form fields
    var title by remember { mutableStateOf("Archie Rant: Short #${(10..99).random()}") }
    var selectedTheme by remember { mutableStateOf(BackgroundTheme.STUDIO_NEON) }
    var isSaving by remember { mutableStateOf(false) }

    // Media states
    var importedAudio by remember { mutableStateOf<ImportedFileResult?>(null) }
    var importedBackground by remember { mutableStateOf<ImportedFileResult?>(null) }
    var importedMusic by remember { mutableStateOf<ImportedFileResult?>(null) }
    val importedPoses = remember { mutableStateListOf<PoseItem>() }

    // Loading states during file copies
    var isProcessingAudio by remember { mutableStateOf(false) }
    var isProcessingBackground by remember { mutableStateOf(false) }
    var isProcessingPoses by remember { mutableStateOf(false) }
    var isProcessingMusic by remember { mutableStateOf(false) }

    // Audio Preview Player
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlayingAudioPreview by remember { mutableStateOf(false) }
    var previewingAudioType by remember { mutableStateOf<String?>(null) } // "VOICE" or "MUSIC"

    fun stopAudioPreview() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
            isPlayingAudioPreview = false
            previewingAudioType = null
        }
    }

    fun playAudioPreview(filePath: String, type: String) {
        if (previewingAudioType == type && isPlayingAudioPreview) {
            stopAudioPreview()
            return
        }
        stopAudioPreview()
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "Audio file not found on storage", Toast.LENGTH_SHORT).show()
                return
            }
            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    start()
                    isPlayingAudioPreview = true
                    previewingAudioType = type
                }
                setOnCompletionListener {
                    stopAudioPreview()
                }
                setOnErrorListener { _, _, _ ->
                    stopAudioPreview()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            stopAudioPreview()
            Toast.makeText(context, "Cannot play audio preview", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopAudioPreview()
        }
    }

    // SAF Native Pickers
    // 1. Audio Picker (WAV, MP3, M4A, AAC)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingAudio = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    MediaStorageManager.copyUriToAppStorage(context, uri, "voice", "archie_voice")
                }
                if (result != null) {
                    importedAudio = result
                    // Auto-save to Media Library as well
                    withContext(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            db.mediaAssetDao().insertAsset(
                                MediaAssetEntity(
                                    name = result.originalName,
                                    category = "AUDIO_VOICE",
                                    character = "ARCHIE",
                                    filePath = result.localFilePath,
                                    fileSize = result.fileSizeBytes,
                                    durationSec = result.durationSeconds,
                                    isBuiltIn = false
                                )
                            )
                        } catch (_: Exception) {}
                    }
                    Toast.makeText(context, "Archie Audio Imported: ${result.originalName}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to load audio file", Toast.LENGTH_SHORT).show()
                }
                isProcessingAudio = false
            }
        }
    }

    // 2. Background Image Picker (PNG, JPG, WebP)
    val backgroundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingBackground = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    MediaStorageManager.copyUriToAppStorage(context, uri, "backgrounds", "custom_bg")
                }
                if (result != null) {
                    importedBackground = result
                    // Auto-save to Media Library
                    withContext(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            db.mediaAssetDao().insertAsset(
                                MediaAssetEntity(
                                    name = result.originalName,
                                    category = "BACKGROUND",
                                    filePath = result.localFilePath,
                                    fileSize = result.fileSizeBytes,
                                    isBuiltIn = false
                                )
                            )
                        } catch (_: Exception) {}
                    }
                    Toast.makeText(context, "Background Imported: ${result.originalName}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to load background image", Toast.LENGTH_SHORT).show()
                }
                isProcessingBackground = false
            }
        }
    }

    // 3. Archie Poses Picker (Multiple PNG / WebP)
    val posesPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isProcessingPoses = true
            scope.launch {
                val newPoses = mutableListOf<PoseItem>()
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(context)
                    for (uri in uris) {
                        val result = MediaStorageManager.copyUriToAppStorage(context, uri, "poses", "archie_pose")
                        if (result != null) {
                            val lower = result.originalName.lowercase()
                            val tag = when {
                                lower.contains("angry") || lower.contains("rage") -> "angry"
                                lower.contains("shout") || lower.contains("scream") -> "shouting"
                                lower.contains("facepalm") -> "facepalm"
                                lower.contains("laugh") -> "laughing"
                                lower.contains("talk") -> "talking"
                                else -> "custom_${importedPoses.size + newPoses.size + 1}"
                            }
                            newPoses.add(
                                PoseItem(
                                    originalName = result.originalName,
                                    localFilePath = result.localFilePath,
                                    fileSizeBytes = result.fileSizeBytes,
                                    tag = tag
                                )
                            )
                            try {
                                db.mediaAssetDao().insertAsset(
                                    MediaAssetEntity(
                                        name = result.originalName,
                                        category = "CHARACTER_POSE",
                                        character = "ARCHIE",
                                        poseTag = tag,
                                        filePath = result.localFilePath,
                                        fileSize = result.fileSizeBytes,
                                        isBuiltIn = false
                                    )
                                )
                            } catch (_: Exception) {}
                        }
                    }
                }
                importedPoses.addAll(newPoses)
                isProcessingPoses = false
                Toast.makeText(context, "Imported ${newPoses.size} Archie Poses", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. Background Music Picker (Optional)
    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingMusic = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    MediaStorageManager.copyUriToAppStorage(context, uri, "music", "bg_music")
                }
                if (result != null) {
                    importedMusic = result
                    // Auto-save to Media Library
                    withContext(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            db.mediaAssetDao().insertAsset(
                                MediaAssetEntity(
                                    name = result.originalName,
                                    category = "MUSIC",
                                    filePath = result.localFilePath,
                                    fileSize = result.fileSizeBytes,
                                    durationSec = result.durationSeconds,
                                    isBuiltIn = false
                                )
                            )
                        } catch (_: Exception) {}
                    }
                    Toast.makeText(context, "Music Track Imported: ${result.originalName}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to load music file", Toast.LENGTH_SHORT).show()
                }
                isProcessingMusic = false
            }
        }
    }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "CREATE 9:16 SHORT",
                subtitle = "Native Media Importer & Setup",
                onBackClick = onBackClick
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("create_short_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = StudioCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ArchieCrimson.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = ArchieCrimson,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NEW SHORT PROJECT SETUP",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Select local audio, backgrounds, and character poses from your Samsung tablet storage.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Field 1: Project Name
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROJECT NAME",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = ArchieCrimson,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "REQUIRED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArchieCrimson
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("project_name_input"),
                            placeholder = { Text("e.g. Archie Rants: Broken Render Queue", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArchieCrimson,
                                unfocusedBorderColor = StudioBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Field 2: Video Format (Fixed at 9:16 / 1080x1920)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VIDEO FORMAT SPECIFICATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = JarvisCyan,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = JarvisCyan.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "FIXED 9:16",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = JarvisCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioDarkBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(JarvisCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = null,
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "9:16 Vertical Video (1080 × 1920 Full HD)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Optimized for YouTube Shorts, TikTok & Instagram Reels",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Field 3: Import Archie Audio (WAV, MP3, M4A, AAC)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = ArchieCrimson, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ARCHIE AUDIO TRACK",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ArchieCrimson,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "WAV, MP3, M4A, AAC",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (importedAudio == null) {
                            Button(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("import_archie_audio_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioCardHover,
                                    contentColor = TextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                            ) {
                                if (isProcessingAudio) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ArchieCrimson, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Importing from Storage...", fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ArchieCrimson)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Import Archie Audio from Device", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Audio Details Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StudioDarkBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArchieCrimson.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .background(ArchieCrimson.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GraphicEq,
                                                    contentDescription = null,
                                                    tint = ArchieCrimson,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = importedAudio!!.originalName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "${MediaStorageManager.formatFileSize(importedAudio!!.fileSizeBytes)}${if (importedAudio!!.durationSeconds > 0) " • ${String.format("%.1f", importedAudio!!.durationSeconds)}s" else ""}",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    playAudioPreview(importedAudio!!.localFilePath, "VOICE")
                                                },
                                                modifier = Modifier.testTag("preview_audio_button")
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlayingAudioPreview && previewingAudioType == "VOICE") Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = "Preview Audio",
                                                    tint = ArchieCrimson
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    stopAudioPreview()
                                                    importedAudio = null
                                                },
                                                modifier = Modifier.testTag("remove_audio_button")
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Field 4: Import Background (PNG, JPG, WebP)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Wallpaper, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BACKGROUND IMAGE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StudioAmber,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "PNG, JPG, WEBP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (importedBackground == null) {
                            Button(
                                onClick = { backgroundPickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("import_background_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioCardHover,
                                    contentColor = TextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                            ) {
                                if (isProcessingBackground) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = StudioAmber, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Importing Image...", fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = StudioAmber)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Import Background from Device", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Background Thumbnail & info
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StudioDarkBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioAmber.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = File(importedBackground!!.localFilePath),
                                            contentDescription = "Background Thumbnail",
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StudioBlack),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = importedBackground!!.originalName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${MediaStorageManager.formatFileSize(importedBackground!!.fileSizeBytes)} • Custom Background",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { importedBackground = null },
                                        modifier = Modifier.testTag("remove_background_button")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Field 5: Import Archie Poses (Multiple PNG / WebP)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Collections, contentDescription = null, tint = BoldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ARCHIE CHARACTER POSES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BoldPrimary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "${importedPoses.size} SELECTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (importedPoses.isNotEmpty()) StudioGreen else TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { posesPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("import_archie_poses_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioCardHover,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                        ) {
                            if (isProcessingPoses) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BoldPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importing Poses...", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BoldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import Archie Poses (Multiple PNG/WebP)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        if (importedPoses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(importedPoses) { index, pose ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = StudioDarkBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                                        modifier = Modifier.width(90.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(modifier = Modifier.size(76.dp)) {
                                                AsyncImage(
                                                    model = File(pose.localFilePath),
                                                    contentDescription = pose.originalName,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(StudioBlack),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .align(Alignment.TopEnd)
                                                        .clip(CircleShape)
                                                        .background(StudioBlack.copy(alpha = 0.8f))
                                                        .clickable { importedPoses.removeAt(index) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove pose",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = pose.tag.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = BoldPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Field 6: Optional Import Background Music
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = StudioGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BACKGROUND MUSIC",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StudioGreen,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "OPTIONAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (importedMusic == null) {
                            Button(
                                onClick = { musicPickerLauncher.launch("audio/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("import_background_music_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioCardHover,
                                    contentColor = TextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                            ) {
                                if (isProcessingMusic) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = StudioGreen, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Importing Music...", fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.Audiotrack, contentDescription = null, tint = StudioGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Import Background Music (Optional)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StudioDarkBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioGreen.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(StudioGreen.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = StudioGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = importedMusic!!.originalName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${MediaStorageManager.formatFileSize(importedMusic!!.fileSizeBytes)}${if (importedMusic!!.durationSeconds > 0) " • ${String.format("%.1f", importedMusic!!.durationSeconds)}s" else ""}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                playAudioPreview(importedMusic!!.localFilePath, "MUSIC")
                                            },
                                            modifier = Modifier.testTag("preview_music_button")
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingAudioPreview && previewingAudioType == "MUSIC") Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Preview Music",
                                                tint = StudioGreen
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                stopAudioPreview()
                                                importedMusic = null
                                            },
                                            modifier = Modifier.testTag("remove_music_button")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Checklist of imported assets
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = StudioDarkBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PROJECT ASSETS SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (importedAudio != null) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (importedAudio != null) StudioGreen else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (importedAudio != null) "Archie Voice: ${importedAudio!!.originalName}" else "Archie Voice: Default voice sample",
                                fontSize = 12.sp,
                                color = if (importedAudio != null) TextPrimary else TextMuted
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (importedBackground != null) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (importedBackground != null) StudioGreen else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (importedBackground != null) "Background: ${importedBackground!!.originalName}" else "Background: Cyber Studio Neon Theme",
                                fontSize = 12.sp,
                                color = if (importedBackground != null) TextPrimary else TextMuted
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (importedPoses.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (importedPoses.isNotEmpty()) StudioGreen else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (importedPoses.isNotEmpty()) "Archie Poses: ${importedPoses.size} Custom Poses Loaded" else "Archie Poses: Built-in Avatar Expression Set",
                                fontSize = 12.sp,
                                color = if (importedPoses.isNotEmpty()) TextPrimary else TextMuted
                            )
                        }
                    }
                }
            }

            // Large SAVE PROJECT Button
            item {
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        isSaving = true
                        stopAudioPreview()

                        // Serialize poses JSON
                        val posesJsonArray = JSONArray()
                        for (pose in importedPoses) {
                            val obj = JSONObject()
                            obj.put("name", pose.originalName)
                            obj.put("path", pose.localFilePath)
                            obj.put("tag", pose.tag)
                            posesJsonArray.put(obj)
                        }

                        val duration = importedAudio?.durationSeconds?.toInt()?.coerceAtLeast(15) ?: 45

                        projectViewModel.createShortProject(
                            title = title.ifBlank { "Archie 9:16 Short" },
                            durationSeconds = duration,
                            theme = selectedTheme,
                            customBackgroundPath = importedBackground?.localFilePath,
                            customBackgroundName = importedBackground?.originalName,
                            archieAudioPath = importedAudio?.localFilePath,
                            archieAudioName = importedAudio?.originalName,
                            archieAudioDurationSec = importedAudio?.durationSeconds ?: 0f,
                            backgroundMusicPath = importedMusic?.localFilePath,
                            backgroundMusicName = importedMusic?.originalName,
                            customArchiePosesJson = posesJsonArray.toString(),
                            templateDialogue = AppDatabase.getSampleDialogueForShort(),
                            onCreated = { newProjectId ->
                                isSaving = false
                                Toast.makeText(context, "Project Saved Successfully!", Toast.LENGTH_SHORT).show()
                                onProjectCreated(newProjectId)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("save_project_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArchieCrimson,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("SAVING TO LOCAL STORAGE...", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SAVE PROJECT",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
