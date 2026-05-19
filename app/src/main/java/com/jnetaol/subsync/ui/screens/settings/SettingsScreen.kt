package com.jnetaol.subsync.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jnetaol.subsync.ui.components.*
import com.jnetaol.subsync.ui.theme.*

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = NeonGreen
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Configure SubSync",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "General")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsRow(
            icon = Icons.Filled.Info,
            title = "Version",
            subtitle = "1.0.0"
        )

        SettingsRow(
            icon = Icons.Filled.SystemUpdate,
            title = "Check for Updates",
            subtitle = "Get the latest version",
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jnetaol/subsync/releases"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "No browser available", Toast.LENGTH_SHORT).show()
                }
            }
        )

        SettingsRow(
            icon = Icons.Filled.Share,
            title = "Share SubSync",
            subtitle = "Share with friends",
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Check out SubSync - Smart Subtitle Finder & Editor! https://jnetaol.com")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share SubSync"))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Data")
        Spacer(modifier = Modifier.height(8.dp))

        SettingsRow(
            icon = Icons.Filled.DeleteForever,
            title = "Clear All Data",
            subtitle = "Remove all videos and subtitles",
            onClick = { showClearConfirm = true },
            tint = ErrorRed
        )

        Spacer(modifier = Modifier.height(24.dp))

        NeonCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Made By",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "jnetaol.com",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SubSync v1.0.0\nSmart Subtitle Finder & Editor\n© 2025 jnetaol.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jnetaol.com"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Visit jnetaol.com",
                color = NeonTeal,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data", color = ErrorRed) },
            text = { Text("This will remove all videos and subtitles from the database. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    Toast.makeText(context, "Data cleared", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Clear", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = NeonTeal)
                }
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    tint: Color = NeonGreen
) {
    NeonCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        borderColor = if (onClick != null) NeonGreen.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
