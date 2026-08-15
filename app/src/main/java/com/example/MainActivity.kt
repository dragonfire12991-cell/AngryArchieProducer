package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.CreatePodcastScreen
import com.example.ui.screens.CreateShortScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MediaLibraryScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.StudioEditorScreen
import com.example.ui.theme.BoldDarkCanvas
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MediaLibraryViewModel
import com.example.ui.viewmodel.ProjectViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.StudioEditorViewModel

object StudioRoutes {
    const val HOME = "home"
    const val CREATE_SHORT = "create_short"
    const val CREATE_PODCAST = "create_podcast"
    const val MEDIA_LIBRARY = "media_library"
    const val PROJECTS = "projects"
    const val STUDIO_EDITOR = "studio_editor/{projectId}"

    fun studioEditorRoute(projectId: Long) = "studio_editor/$projectId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AngryArchieApp()
            }
        }
    }
}

@Composable
fun AngryArchieApp() {
    val navController = rememberNavController()
    val projectViewModel: ProjectViewModel = viewModel()
    val mediaViewModel: MediaLibraryViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BoldDarkCanvas)
    ) {
        NavHost(
            navController = navController,
            startDestination = StudioRoutes.HOME,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(StudioRoutes.HOME) {
                HomeScreen(
                    projectViewModel = projectViewModel,
                    onCreateShortClick = { navController.navigate(StudioRoutes.CREATE_SHORT) },
                    onCreatePodcastClick = { navController.navigate(StudioRoutes.CREATE_PODCAST) },
                    onMediaLibraryClick = { navController.navigate(StudioRoutes.MEDIA_LIBRARY) },
                    onProjectsClick = { navController.navigate(StudioRoutes.PROJECTS) },
                    onSettingsClick = { showSettingsDialog = true },
                    onOpenProject = { projectId ->
                        navController.navigate(StudioRoutes.studioEditorRoute(projectId))
                    }
                )
            }

            composable(StudioRoutes.CREATE_SHORT) {
                CreateShortScreen(
                    projectViewModel = projectViewModel,
                    onBackClick = { navController.popBackStack() },
                    onProjectCreated = { newProjectId ->
                        navController.navigate(StudioRoutes.studioEditorRoute(newProjectId)) {
                            popUpTo(StudioRoutes.HOME)
                        }
                    }
                )
            }

            composable(StudioRoutes.CREATE_PODCAST) {
                CreatePodcastScreen(
                    projectViewModel = projectViewModel,
                    onBackClick = { navController.popBackStack() },
                    onProjectCreated = { newProjectId ->
                        navController.navigate(StudioRoutes.studioEditorRoute(newProjectId)) {
                            popUpTo(StudioRoutes.HOME)
                        }
                    }
                )
            }

            composable(StudioRoutes.MEDIA_LIBRARY) {
                MediaLibraryScreen(
                    mediaViewModel = mediaViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { showSettingsDialog = true }
                )
            }

            composable(StudioRoutes.PROJECTS) {
                ProjectsScreen(
                    projectViewModel = projectViewModel,
                    onBackClick = { navController.popBackStack() },
                    onOpenProject = { projectId ->
                        navController.navigate(StudioRoutes.studioEditorRoute(projectId))
                    },
                    onCreateShortClick = { navController.navigate(StudioRoutes.CREATE_SHORT) },
                    onCreatePodcastClick = { navController.navigate(StudioRoutes.CREATE_PODCAST) },
                    onSettingsClick = { showSettingsDialog = true }
                )
            }

            composable(
                route = StudioRoutes.STUDIO_EDITOR,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 1L
                val studioEditorViewModel: StudioEditorViewModel = viewModel()
                StudioEditorScreen(
                    projectId = projectId,
                    editorViewModel = studioEditorViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { showSettingsDialog = true }
                )
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                settingsViewModel = settingsViewModel,
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

