package com.jnetaol.subsync.engine

import android.os.Environment
import com.jnetaol.subsync.logger.DebugLogger
import java.io.File

object VideoScanner {

    private val videoExtensions = setOf(
        ".mp4", ".mkv", ".avi", ".mov", ".flv", ".wmv", ".webm",
        ".m4v", ".mpg", ".mpeg", ".3gp", ".ogv", ".ts"
    )

    data class FoundVideo(
        val filePath: String,
        val fileName: String,
        val fileSize: Long
    )

    fun scanDirectories(): List<FoundVideo> {
        DebugLogger.i("SS-020", "Scanning for video files")
        val videos = mutableListOf<FoundVideo>()

        val dirsToScan = mutableListOf<File>()

        Environment.getExternalStorageDirectory()?.let { dirsToScan.add(it) }

        val devices = File("/storage")
        if (devices.exists()) {
            devices.listFiles()?.filter { it.isDirectory && it.name != "emulated" && it.name != "self" }
                ?.forEach { dirsToScan.add(it) }
        }

        val moviesDir = File(Environment.getExternalStorageDirectory(), "Movies")
        if (moviesDir.exists()) dirsToScan.add(moviesDir)

        val downloadDir = File(Environment.getExternalStorageDirectory(), "Download")
        if (downloadDir.exists()) dirsToScan.add(downloadDir)

        val dcimDir = File(Environment.getExternalStorageDirectory(), "DCIM")
        if (dcimDir.exists()) dirsToScan.add(dcimDir)

        for (dir in dirsToScan) {
            scanDirectory(dir, videos, maxDepth = 3)
        }

        DebugLogger.i("SS-021", "Found ${videos.size} video files")
        return videos
    }

    private fun scanDirectory(dir: File, results: MutableList<FoundVideo>, maxDepth: Int) {
        if (maxDepth <= 0 || !dir.exists() || !dir.isDirectory) return

        try {
            val files = dir.listFiles() ?: return
            for (file in files) {
                try {
                    if (file.isFile && videoExtensions.any { file.name.lowercase().endsWith(it) }) {
                        results.add(
                            FoundVideo(
                                filePath = file.absolutePath,
                                fileName = file.name,
                                fileSize = file.length()
                            )
                        )
                    } else if (file.isDirectory && !file.name.startsWith(".")) {
                        scanDirectory(file, results, maxDepth - 1)
                    }
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            DebugLogger.w("SS-022", "Error scanning directory: ${dir.absolutePath}")
        }
    }

    fun getVideoCount(): Int {
        return scanDirectories().size
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> "%.2f GB".format(bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> "%.2f MB".format(bytes / 1_000_000.0)
            bytes >= 1_000 -> "%.2f KB".format(bytes / 1_000.0)
            else -> "$bytes B"
        }
    }
}
