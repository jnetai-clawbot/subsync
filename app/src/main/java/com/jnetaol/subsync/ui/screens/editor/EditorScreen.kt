package com.jnetaol.subsync.ui.screens.editor

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jnetaol.subsync.engine.SubtitleEngine
import com.jnetaol.subsync.engine.Translator
import com.jnetaol.subsync.ui.components.*
import com.jnetaol.subsync.ui.screens.AppViewModel
import com.jnetaol.subsync.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val entries by viewModel.currentEntries.collectAsStateWithLifecycle()
    val showTranslation by viewModel.showTranslation.collectAsStateWithLifecycle()
    val targetLanguage by viewModel.targetLanguage.collectAsStateWithLifecycle()
    val editingEntry by viewModel.editingEntry.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var offsetText by remember { mutableStateOf("") }
    var scaleText by remember { mutableStateOf("1.0") }
    var showLangPicker by remember { mutableStateOf(false) }
    var showEntryEditor by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    var editTranslated by remember { mutableStateOf("") }

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
                text = "Subtitle Editor",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${entries.size} entries",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlowButton(
                text = "← 200ms",
                icon = Icons.Filled.FastRewind,
                onClick = { viewModel.applySyncToCurrentEntries(-200) }
            )
            GlowButton(
                text = "→ 200ms",
                icon = Icons.Filled.FastForward,
                onClick = { viewModel.applySyncToCurrentEntries(200) }
            )
            GlowButton(
                text = "Translate",
                icon = Icons.Filled.Translate,
                onClick = { viewModel.translateCurrentEntries() },
                variant = "secondary"
            )
            GlowButton(
                text = "Export",
                icon = Icons.Filled.Share,
                onClick = {
                    val subtitles = viewModel.subtitlesForVideo.value.firstOrNull()
                    if (subtitles != null) {
                        val content = viewModel.exportSrt(subtitles.id)
                        if (content != null) {
                            exportSrtFile(context, subtitles.fileName, content)
                        } else {
                            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                variant = "secondary"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = showTranslation,
                        onCheckedChange = { viewModel.setShowTranslation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreenDim.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show Translation",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
                FilterChip(
                    selected = true,
                    onClick = { showLangPicker = true },
                    label = {
                        val lang = Translator.getSupportedLanguages().find { it.first == targetLanguage }
                        Text(
                            text = lang?.second ?: targetLanguage,
                            color = NeonTeal,
                            fontSize = 12.sp
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            null,
                            tint = NeonTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkCard,
                        selectedContainerColor = DarkCard
                    )
                )
            }
        }

        if (entries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = offsetText,
                onValueChange = { offsetText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Custom offset (ms, +/-)", color = TextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    TextButton(onClick = {
                        val offset = offsetText.toLongOrNull() ?: 0
                        viewModel.applySyncToCurrentEntries(offset)
                        offsetText = ""
                    }) {
                        Text("Apply", color = NeonGreen)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                    cursorColor = NeonGreen,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = scaleText,
                    onValueChange = { scaleText = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Scale factor", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                        cursorColor = NeonGreen,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
                GlowButton(
                    text = "Scale",
                    onClick = {
                        val factor = scaleText.toFloatOrNull() ?: 1.0f
                        if (factor > 0) viewModel.scaleCurrentEntries(factor)
                    },
                    variant = "secondary"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (entries.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Edit,
                title = "No Entries Loaded",
                subtitle = "Select a subtitle track to edit"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    SubtitleChip(entry = entry, showTranslation = showTranslation)
                }
            }
        }
    }

    if (showLangPicker) {
        AlertDialog(
            onDismissRequest = { showLangPicker = false },
            title = { Text("Target Language", color = NeonGreen) },
            text = {
                Column {
                    Translator.getSupportedLanguages().forEach { (code, name) ->
                        TextButton(
                            onClick = {
                                viewModel.setTargetLanguage(code)
                                showLangPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = name,
                                color = if (code == targetLanguage) NeonGreen else TextPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLangPicker = false }) {
                    Text("Cancel", color = NeonTeal)
                }
            }
        )
    }
}

private fun exportSrtFile(context: Context, fileName: String, content: String) {
    val exportDir = File(context.getExternalFilesDir(null), "exports")
    exportDir.mkdirs()
    val file = File(exportDir, fileName)
    file.writeText(content)
    Toast.makeText(context, "Exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
}
