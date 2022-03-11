package com.example.akxplayer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.akxplayer.db.converter.Converters
import com.example.akxplayer.db.dao.FavoriteDao
import com.example.akxplayer.db.dao.QueueDao
import com.example.akxplayer.db.dao.SongDao
import com.example.akxplayer.db.entity.FavoriteEntity
import com.example.akxplayer.db.entity.QueueEntity
import com.example.akxplayer.db.entity.SongEntity

@Database(entities = [QueueEntity::class, SongEntity::class, FavoriteEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AkxDatabase : RoomDatabase() {

    abstract fun queueDao(): QueueDao
    abstract fun songDao(): SongDao
    abstract fun favoriteDao(): FavoriteDao

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
