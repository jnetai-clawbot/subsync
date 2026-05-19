package com.jnetaol.subsync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jnetaol.subsync.ui.screens.editor.EditorScreen
import com.jnetaol.subsync.ui.screens.home.HomeScreen
import com.jnetaol.subsync.ui.screens.search.SearchScreen
import com.jnetaol.subsync.ui.screens.settings.SettingsScreen
import com.jnetaol.subsync.ui.screens.video.VideoDetailScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onVideoClick = { video ->
                    viewModel.selectVideo(video)
                    navController.navigate("video_detail")
                }
            )
        }
        composable("video_detail") {
            val video by viewModel.selectedVideo.collectAsStateWithLifecycle()
            if (video != null) {
                VideoDetailScreen(
                    viewModel = viewModel,
                    onEditSubtitle = { track ->
                        viewModel.loadEntries(track.id)
                        navController.navigate("editor")
                    },
                    onBack = {
                        viewModel.clearSelectedVideo()
                        navController.popBackStack()
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable("editor") {
            EditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("search") {
            SearchScreen(viewModel = viewModel)
        }
        composable("settings") {
            SettingsScreen()
        }
    }
}
