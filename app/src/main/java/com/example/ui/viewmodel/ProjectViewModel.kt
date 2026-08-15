package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BackgroundTheme
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.data.model.SubtitleStyle
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectListUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val selectedFilter: String = "ALL", // "ALL", "SHORT", "PODCAST"
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(database.projectDao())

    private val _selectedFilter = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ProjectListUiState> = combine(
        repository.allProjects,
        _selectedFilter,
        _searchQuery
    ) { projects, filter, query ->
        val filtered = projects.filter { project ->
            val matchesFilter = when (filter) {
                "SHORT" -> project.projectType == ProjectType.SHORT.name
                "PODCAST" -> project.projectType == ProjectType.PODCAST.name
                else -> true
            }
            val matchesQuery = query.isBlank() || project.title.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
        ProjectListUiState(
            projects = filtered,
            selectedFilter = filter,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProjectListUiState(isLoading = true)
    )

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createShortProject(
        title: String,
        durationSeconds: Int = 45,
        theme: BackgroundTheme = BackgroundTheme.STUDIO_NEON,
        customBackgroundPath: String? = null,
        customBackgroundName: String? = null,
        archieAudioPath: String? = null,
        archieAudioName: String? = null,
        archieAudioDurationSec: Float = 0f,
        backgroundMusicPath: String? = null,
        backgroundMusicName: String? = null,
        customArchiePosesJson: String = "",
        templateDialogue: String = AppDatabase.getSampleDialogueForShort(),
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val project = ProjectEntity(
                title = if (title.isBlank()) "Archie 9:16 Short #${(10..99).random()}" else title,
                projectType = ProjectType.SHORT.name,
                durationSeconds = if (archieAudioDurationSec > 0) archieAudioDurationSec.toInt().coerceAtLeast(10) else durationSeconds,
                aspectRatio = "9:16",
                resolution = "1080x1920",
                backgroundTheme = theme.name,
                customBackgroundPath = customBackgroundPath,
                customBackgroundName = customBackgroundName,
                archieAudioPath = archieAudioPath,
                archieAudioName = archieAudioName,
                archieAudioDurationSec = archieAudioDurationSec,
                backgroundMusicPath = backgroundMusicPath,
                backgroundMusicName = backgroundMusicName,
                customArchiePosesJson = customArchiePosesJson,
                musicTrack = if (backgroundMusicName != null) backgroundMusicName else "Cyber Hype Synth",
                subtitleStyle = SubtitleStyle.PUNCHY_YELLOW.name,
                dialogueJson = templateDialogue
            )
            val id = repository.insertProject(project)
            onCreated(id)
        }
    }

    fun createPodcastProject(
        title: String,
        durationSeconds: Int = 180,
        theme: BackgroundTheme = BackgroundTheme.PODCAST_DESK,
        templateDialogue: String = AppDatabase.getSampleDialogueForPodcast(),
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val project = ProjectEntity(
                title = if (title.isBlank()) "The Archie & Jarvis Show Ep ${(10..99).random()}" else title,
                projectType = ProjectType.PODCAST.name,
                durationSeconds = durationSeconds,
                aspectRatio = "9:16",
                backgroundTheme = theme.name,
                musicTrack = "Lo-Fi Argument",
                subtitleStyle = SubtitleStyle.NEON_CYAN.name,
                dialogueJson = templateDialogue
            )
            val id = repository.insertProject(project)
            onCreated(id)
        }
    }

    fun duplicateProject(project: ProjectEntity) {
        viewModelScope.launch {
            val duplicate = project.copy(
                id = 0,
                title = "${project.title} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertProject(duplicate)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }
}
