package com.example.akxplayer.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.akxplayer.db.entity.FavoriteEntity

@Dao
interface FavoriteDao {
    @Insert
    fun add(favorite: FavoriteEntity)

    @Delete
    fun remove(favorite: FavoriteEntity)

    @Query("SELECT song_id FROM favorite WHERE song_id=:songId")
    fun isFavorite(songId: Long): Long

    @Query("SELECT song_id FROM favorite")
    fun getFavoriteSongs(): LongArray

    @Query("SELECT COUNT(song_id) FROM favorite")
    fun getNumOfFavoriteSongs(): Int
}
