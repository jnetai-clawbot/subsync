package com.jnetaol.subsync.ui.screens.video

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jnetaol.subsync.data.model.SubtitleTrack
import com.jnetaol.subsync.engine.SubtitleEngine
import com.jnetaol.subsync.ui.components.*
import com.jnetaol.subsync.ui.screens.AppViewModel
import com.jnetaol.subsync.ui.theme.*

@Composable
fun VideoDetailScreen(
    viewModel: AppViewModel,
    onEditSubtitle: (SubtitleTrack) -> Unit,
    onBack: () -> Unit
) {
    val video by viewModel.selectedVideo.collectAsStateWithLifecycle()
    val subtitles by viewModel.subtitlesForVideo.collectAsStateWithLifecycle()
    val matches by viewModel.subtitleMatches.collectAsStateWithLifecycle()
    var showDownloadDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                text = "Video Details",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            GlowButton(
                text = "Download",
                icon = Icons.Filled.Download,
                onClick = { showDownloadDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        video?.let { v ->
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Movie,
                            null,
                            tint = NeonGreen,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = v.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row {
                            StatusBadge(
                                text = VideoScanner.formatFileSize(v.fileSize),
                                color = NeonTeal
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                text = "${subtitles.size} subs",
                                color = NeonGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(
            title = "Available Subtitles",
            subtitle = "Sync tools and editing"
        )

        if (subtitles.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.ClosedCaption,
                title = "No Subtitles",
                subtitle = "Download or import subtitles for this video"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(subtitles, key = { it.id }) { track ->
                    SubtitleTrackCard(
                        track = track,
                        onEdit = { onEditSubtitle(track) },
                        onDelete = { viewModel.deleteTrack(track) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showDownloadDialog) {
        DownloadDialog(
            matches = matches,
            onDismiss = { showDownloadDialog = false },
            onDownload = { match ->
                viewModel.downloadSubtitle(match)
                showDownloadDialog = false
            }
        )
    }
}

@Composable
fun SubtitleTrackCard(
    track: SubtitleTrack,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: AppViewModel
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    NeonCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ClosedCaption,
                null,
                tint = NeonGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.language,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${track.entryCount} entries • ${track.source}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            StatusBadge(
                text = track.source.uppercase(),
                color = if (track.source == "download") NeonTeal else NeonGreen
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Filled.Delete, "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Subtitle") },
            text = { Text("Delete ${track.language} subtitle? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DownloadDialog(
    matches: List<SubtitleMatch>,
    onDismiss: () -> Unit,
    onDownload: (SubtitleMatch) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Download Subtitles", color = NeonGreen)
        },
        text = {
            if (matches.isEmpty()) {
                Text("No matches found.", color = TextSecondary)
            } else {
                Column {
                    Text(
                        "Found ${matches.size} matches:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    matches.forEach { match ->
                        NeonCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { onDownload(match) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = match.language,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "~${match.entryCount} entries • ${match.matchScore}% match",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                StatusBadge(
                                    text = "${match.matchScore}%",
                                    color = if (match.matchScore >= 80) NeonGreen else WarningOrange
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeonTeal)
            }
        }
    )
}
