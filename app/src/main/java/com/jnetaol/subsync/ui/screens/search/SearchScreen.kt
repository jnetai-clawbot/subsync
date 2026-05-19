package com.jnetaol.subsync.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jnetaol.subsync.ui.components.*
import com.jnetaol.subsync.ui.screens.AppViewModel
import com.jnetaol.subsync.ui.theme.*

@Composable
fun SearchScreen(viewModel: AppViewModel) {
    val query by viewModel.searchTextQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val showTranslation by viewModel.showTranslation.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        Text(
            text = "Search Subtitles",
            style = MaterialTheme.typography.headlineLarge,
            color = NeonGreen
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Find text across all subtitle files",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchText(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search subtitle text...", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Filled.Search, null, tint = NeonGreen)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchText("") }) {
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

        if (query.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "${results.size} results",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                FilterChip(
                    selected = showTranslation,
                    onClick = { viewModel.setShowTranslation(!showTranslation) },
                    label = {
                        Text(
                            text = "Show Translation",
                            color = if (showTranslation) NeonGreen else TextMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkCard,
                        selectedContainerColor = NeonGreen.copy(alpha = 0.1f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (results.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.SearchOff,
                    title = "No Results",
                    subtitle = "Try a different search term"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(results, key = { it.id }) { entry ->
                        NeonCard(modifier = Modifier.fillMaxWidth()) {
                            Row {
                                Text(
                                    text = "#${entry.index}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Column {
                                    Text(
                                        text = entry.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    if (showTranslation && entry.translatedText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = entry.translatedText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NeonTeal
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${SubtitleEngine.formatTimestamp(entry.startMs)} → ${SubtitleEngine.formatTimestamp(entry.endMs)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            EmptyState(
                icon = Icons.Filled.Search,
                title = "Search Subtitles",
                subtitle = "Enter text to search across all subtitle files"
            )
        }
    }
}
