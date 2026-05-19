package com.jnetaol.subsync.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jnetaol.subsync.data.model.SubtitleEntry
import com.jnetaol.subsync.data.model.SubtitleTrack
import com.jnetaol.subsync.data.model.VideoFile
import com.jnetaol.subsync.logger.DebugLogger

@Database(
    entities = [VideoFile::class, SubtitleTrack::class, SubtitleEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoFileDao(): VideoFileDao
    abstract fun subtitleTrackDao(): SubtitleTrackDao
    abstract fun subtitleEntryDao(): SubtitleEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                DebugLogger.i("SS-010", "Creating Room database")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "subsync.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
