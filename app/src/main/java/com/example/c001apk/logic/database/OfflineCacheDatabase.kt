package com.example.c001apk.logic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.OfflineCacheDao
import com.example.c001apk.logic.model.OfflineFeedEntity

@Database(version = 1, entities = [OfflineFeedEntity::class])
abstract class OfflineCacheDatabase : RoomDatabase() {
    abstract fun offlineCacheDao(): OfflineCacheDao
}
