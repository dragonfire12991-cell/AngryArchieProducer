package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MediaAssetEntity
import com.example.data.util.MediaStorageManager
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.ArchieCrimson
import com.example.ui.theme.ArchieCrimsonLight
import com.example.ui.theme.BoldPrimary
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardHover
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MediaLibraryViewModel
import java.io.File

@Composable
fun MediaLibraryScreen(
    mediaViewModel: MediaLibraryViewModel,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val state by mediaViewModel.uiState.collectAsStateWithLifecycle()
    var showImportChooserDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            mediaViewModel.stopAudio()
        }
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            mediaViewModel.clearStatusMessage()
        }
    }

    // Native Media Pickers for different asset types
    val imageSingleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            mediaViewModel.importSingleFile(context, uri, "BACKGROUND")
        }
    }

    val archiePosesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            mediaViewModel.importMultipleFiles(context, uris, "CHARACTER_POSE", character = "ARCHIE")
        }
    }

    val jarvisPosesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            mediaViewModel.importMultipleFiles(context, uris, "CHARACTER_POSE", character = "JARVIS")
        }
    }

    val audioVoiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            mediaViewModel.importSingleFile(context, uri, "AUDIO_VOICE", character = "ARCHIE")
        }
    }

    val musicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            mediaViewModel.importSingleFile(context, uri, "MUSIC")
        }
    }

    val sfxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            mediaViewModel.importSingleFile(context, uri, "SFX")
        }
    }

    val categories = listOf(
        "ALL" to "All Assets",
        "ARCHIE_POSE" to "Archie Poses",
        "JARVIS_POSE" to "Jarvis Poses",
        "BACKGROUND" to "Backgrounds",
        "AUDIO_VOICE" to "Voice Audio",
        "MUSIC" to "Music Tracks",
        "SFX" to "Sound Effects"
    )

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "MEDIA ASSET LIBRARY",
                subtitle = "Organize Poses, Backgrounds & Audio on Device",
                onBackClick = onBackClick,
                onSettingsClick = onSettingsClick,
                actions = {
                    Button(
                        onClick = { showImportChooserDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BoldPrimary,
                            contentColor = StudioDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_custom_asset_button")
                    ) {
                        if (state.isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = StudioDarkBg, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importing...", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Media", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("media_library_screen")
        ) {
            // Search Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { mediaViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search media assets by name...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { mediaViewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BoldPrimary,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = StudioCardBg,
                        unfocusedContainerColor = StudioCardBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("media_search_input")
                )
            }

            // Category Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.size) { index ->
                    val (key, label) = categories[index]
                    val isSelected = state.selectedCategory == key
                    Surface(
                        modifier = Modifier
                            .clickable { mediaViewModel.setCategory(key) }
                            .testTag("media_cat_$key"),
                        shape = RoundedCornerShape(100.dp),
                        color = if (isSelected) BoldPrimary else StudioCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BoldPrimary else StudioBorder
                        )
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) StudioDarkBg else TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Assets Count Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.assets.size} ASSETS IN VAULT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Saved locally on Android device",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Assets List
            if (state.assets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PermMedia,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No assets match your selection",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Import Media' above to add PNGs or Audio from your tablet.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.assets, key = { it.id }) { asset ->
                        AssetItemCard(
                            asset = asset,
                            isPlaying = state.currentlyPlayingAssetId == asset.id && state.isPlayingAudio,
                            onToggleAudio = { mediaViewModel.toggleAudioPreview(asset) },
                            onDelete = { mediaViewModel.deleteAsset(asset) }
                        )
                    }
                }
            }
        }

        // Import Asset Chooser Dialog
        if (showImportChooserDialog) {
            ImportChooserDialog(
                onDismiss = { showImportChooserDialog = false },
                onChooseArchiePoses = {
                    showImportChooserDialog = false
                    archiePosesLauncher.launch("image/*")
                },
                onChooseJarvisPoses = {
                    showImportChooserDialog = false
                    jarvisPosesLauncher.launch("image/*")
                },
                onChooseBackground = {
                    showImportChooserDialog = false
                    imageSingleLauncher.launch("image/*")
                },
                onChooseVoiceAudio = {
                    showImportChooserDialog = false
                    audioVoiceLauncher.launch("audio/*")
                },
                onChooseMusic = {
                    showImportChooserDialog = false
                    musicLauncher.launch("audio/*")
                },
                onChooseSfx = {
                    showImportChooserDialog = false
                    sfxLauncher.launch("audio/*")
                }
            )
        }
    }
}

@Composable
fun AssetItemCard(
    asset: MediaAssetEntity,
    isPlaying: Boolean,
    onToggleAudio: () -> Unit,
    onDelete: () -> Unit
) {
    val isCharacter = asset.category == "CHARACTER_POSE"
    val isAudio = asset.category == "AUDIO_VOICE" || asset.category == "MUSIC" || asset.category == "SFX"
    val isArchie = asset.character == "ARCHIE"
    val isJarvis = asset.character == "JARVIS"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("asset_card_${asset.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
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
                // Thumbnail / Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioBlack),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCharacter) {
                        if (!asset.isBuiltIn) {
                            AsyncImage(
                                model = File(asset.filePath),
                                contentDescription = asset.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(
                                    id = if (isArchie) R.drawable.img_archie_avatar else R.drawable.img_jarvis_avatar
                                ),
                                contentDescription = asset.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (asset.category == "BACKGROUND") {
                        if (!asset.isBuiltIn) {
                            AsyncImage(
                                model = File(asset.filePath),
                                contentDescription = asset.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_studio_banner),
                                contentDescription = asset.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (asset.category == "AUDIO_VOICE") ArchieCrimson.copy(alpha = 0.2f)
                                    else if (asset.category == "MUSIC") StudioGreen.copy(alpha = 0.2f)
                                    else StudioAmber.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (asset.category) {
                                    "AUDIO_VOICE" -> Icons.Default.Mic
                                    "MUSIC" -> Icons.Default.MusicNote
                                    else -> Icons.Default.GraphicEq
                                },
                                contentDescription = null,
                                tint = when (asset.category) {
                                    "AUDIO_VOICE" -> ArchieCrimson
                                    "MUSIC" -> StudioGreen
                                    else -> StudioAmber
                                },
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = StudioCardHover,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when (asset.category) {
                                    "CHARACTER_POSE" -> if (isArchie) "ARCHIE POSE" else if (isJarvis) "JARVIS POSE" else "POSE"
                                    "BACKGROUND" -> "BACKGROUND"
                                    "AUDIO_VOICE" -> "VOICE TRACK"
                                    "MUSIC" -> "MUSIC"
                                    "SFX" -> "SFX"
                                    else -> asset.category
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = when (asset.category) {
                                    "CHARACTER_POSE" -> if (isArchie) ArchieCrimson else JarvisCyan
                                    "AUDIO_VOICE" -> ArchieCrimsonLight
                                    "MUSIC" -> StudioGreen
                                    "BACKGROUND" -> StudioAmber
                                    else -> TextSecondary
                                },
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        if (asset.fileSize > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = MediaStorageManager.formatFileSize(asset.fileSize),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        if (asset.durationSec > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${String.format("%.1f", asset.durationSec)}s",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAudio && !asset.isBuiltIn) {
                    IconButton(
                        onClick = onToggleAudio,
                        modifier = Modifier.testTag("play_asset_${asset.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play preview",
                            tint = BoldPrimary
                        )
                    }
                }

                if (!asset.isBuiltIn) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_asset_${asset.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                    }
                } else {
                    Surface(
                        color = StudioBlack,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Text(
                            text = "BUILT-IN",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImportChooserDialog(
    onDismiss: () -> Unit,
    onChooseArchiePoses: () -> Unit,
    onChooseJarvisPoses: () -> Unit,
    onChooseBackground: () -> Unit,
    onChooseVoiceAudio: () -> Unit,
    onChooseMusic: () -> Unit,
    onChooseSfx: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BoldPrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BoldPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("IMPORT MEDIA ASSETS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                    Text("Select asset category to open tablet file picker", fontSize = 11.sp, color = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImportOptionButton(
                    title = "Archie Poses (Multiple PNG/WebP)",
                    subtitle = "Transparent character sprites for rage & shouts",
                    color = ArchieCrimson,
                    icon = Icons.Default.Collections,
                    onClick = onChooseArchiePoses
                )

                ImportOptionButton(
                    title = "Jarvis Poses (Multiple PNG/WebP)",
                    subtitle = "AI calculating & sarcastic pose sprites",
                    color = JarvisCyan,
                    icon = Icons.Default.Collections,
                    onClick = onChooseJarvisPoses
                )

                ImportOptionButton(
                    title = "Background Scenes (PNG, JPG, WebP)",
                    subtitle = "Stage wallpapers, news desks, neon rooms",
                    color = StudioAmber,
                    icon = Icons.Default.Wallpaper,
                    onClick = onChooseBackground
                )

                ImportOptionButton(
                    title = "Voice Audio (WAV, MP3, M4A, AAC)",
                    subtitle = "Dialogue takes and character recordings",
                    color = ArchieCrimsonLight,
                    icon = Icons.Default.Mic,
                    onClick = onChooseVoiceAudio
                )

                ImportOptionButton(
                    title = "Music Tracks (MP3, WAV, M4A)",
                    subtitle = "Background loops and backing tracks",
                    color = StudioGreen,
                    icon = Icons.Default.MusicNote,
                    onClick = onChooseMusic
                )

                ImportOptionButton(
                    title = "Sound Effects SFX (WAV, MP3)",
                    subtitle = "Hits, buzzers, chimes, records",
                    color = StudioAmber,
                    icon = Icons.Default.GraphicEq,
                    onClick = onChooseSfx
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = StudioDarkBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ImportOptionButton(
    title: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = StudioCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text(subtitle, fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}
