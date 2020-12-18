package com.example.akxplayer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.akxplayer.db.dao.QueueDao
import com.example.akxplayer.db.dao.SongDao
import com.example.akxplayer.db.entity.QueueEntity
import com.example.akxplayer.db.entity.SongEntity

@Database(entities = [QueueEntity::class, SongEntity::class], version = 1, exportSchema = false)
abstract class AkxDatabase : RoomDatabase() {

    abstract fun queueDao(): QueueDao
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var instance: AkxDatabase? = null

        fun getInstance(context: Context): AkxDatabase {
            if (instance != null) {
                return instance as AkxDatabase
            }
            synchronized(this) {
                instance = Room.databaseBuilder(
                    context,
                    AkxDatabase::class.java,
                    "akx_database"
                ).fallbackToDestructiveMigration().build()
                return instance as AkxDatabase
            }
        }
    }
}