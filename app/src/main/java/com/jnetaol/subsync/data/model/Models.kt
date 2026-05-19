package com.jnetaol.subsync.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "video_files")
data class VideoFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val duration: Long = 0,
    val resolution: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subtitle_tracks",
    indices = [Index("videoFileId")]
)
data class SubtitleTrack(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoFileId: Long,
    val language: String,
    val fileName: String,
    val filePath: String = "",
    val entryCount: Int = 0,
    val source: String = "local",
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subtitle_entries",
    indices = [Index("trackId")]
)
data class SubtitleEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: Long,
    val index: Int,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val translatedText: String = ""
)
