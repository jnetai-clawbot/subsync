package com.jnetaol.subsync.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jnetaol.subsync.data.model.VideoFile
import com.jnetaol.subsync.ui.components.*
import com.jnetaol.subsync.ui.screens.AppViewModel
import com.jnetaol.subsync.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onVideoClick: (VideoFile) -> Unit
) {
    val videos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (allVideos.isEmpty()) {
            viewModel.scanVideos()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        Text(
            text = "SubSync",
            style = MaterialTheme.typography.headlineLarge,
            color = NeonGreen
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Smart Subtitle Manager",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search videos...", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Filled.Search, null, tint = NeonGreen)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Filled.Close, "Clear", tint = TextMuted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                cursorColor = NeonGreen,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = MaterialTheme.shapes.large,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${videos.size} videos",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            GlowButton(
                text = if (isScanning) "Scanning..." else "Scan",
                icon = Icons.Filled.Refresh,
                onClick = { viewModel.scanVideos() },
                enabled = !isScanning,
                variant = "secondary"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isScanning) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = NeonGreen,
                trackColor = DarkSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (videos.isEmpty() && !isScanning) {
            EmptyState(
                icon = Icons.Filled.VideoLibrary,
                title = "No Videos Found",
                subtitle = "Tap Scan to search your device for video files",
                actionLabel = "Scan Now",
                onAction = { viewModel.scanVideos() }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(videos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video) }
                    )
                }
            }
        }
    }
}
