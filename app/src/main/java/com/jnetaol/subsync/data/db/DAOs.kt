package com.jnetaol.subsync.data.db

import androidx.room.*
import com.jnetaol.subsync.data.model.SubtitleEntry
import com.jnetaol.subsync.data.model.SubtitleTrack
import com.jnetaol.subsync.data.model.VideoFile
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoFileDao {
    @Query("SELECT * FROM video_files ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<VideoFile>>

    @Query("SELECT * FROM video_files WHERE fileName LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<VideoFile>>

    @Query("SELECT * FROM video_files WHERE id = :id")
    suspend fun getById(id: Long): VideoFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: VideoFile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<VideoFile>)

    @Delete
    suspend fun delete(video: VideoFile)

    @Query("DELETE FROM video_files")
    suspend fun deleteAll()
}

@Dao
interface SubtitleTrackDao {
    @Query("SELECT * FROM subtitle_tracks WHERE videoFileId = :videoFileId ORDER BY dateAdded DESC")
    fun getByVideoId(videoFileId: Long): Flow<List<SubtitleTrack>>

    @Query("SELECT * FROM subtitle_tracks WHERE id = :id")
    suspend fun getById(id: Long): SubtitleTrack?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: SubtitleTrack): Long

    @Delete
    suspend fun delete(track: SubtitleTrack)

    @Query("DELETE FROM subtitle_tracks WHERE videoFileId = :videoFileId")
    suspend fun deleteByVideoId(videoFileId: Long)
}

@Dao
interface SubtitleEntryDao {
    @Query("SELECT * FROM subtitle_entries WHERE trackId = :trackId ORDER BY `index` ASC")
    fun getByTrackId(trackId: Long): Flow<List<SubtitleEntry>>

    @Query("SELECT * FROM subtitle_entries WHERE trackId = :trackId ORDER BY `index` ASC")
    suspend fun getByTrackIdList(trackId: Long): List<SubtitleEntry>

    @Query("SELECT * FROM subtitle_entries WHERE text LIKE '%' || :query || '%' OR translatedText LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<SubtitleEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SubtitleEntry>)

    @Update
    suspend fun update(entry: SubtitleEntry)

    @Delete
    suspend fun delete(entry: SubtitleEntry)

    @Query("DELETE FROM subtitle_entries WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: Long)
}
