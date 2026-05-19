package com.jnetaol.subsync

import android.app.Application
import com.jnetaol.subsync.data.db.AppDatabase
import com.jnetaol.subsync.logger.DebugLogger

class SubSyncApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        DebugLogger.i("SS-001", "SubSync application initialized")
        database = AppDatabase.getInstance(this)
    }

    companion object {
        lateinit var instance: SubSyncApp
            private set
    }
}
