package com.example.akxplayer.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.akxplayer.db.entity.SongEntity
import com.example.akxplayer.model.Song
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface SongDao {

    @Insert
    fun insertSongs(songList: List<SongEntity>)

    @Insert
    fun insertQueue(queueList: List<SongEntity>)

    @Query("DELETE FROM song WHERE queue_id = 0")
    fun deleteAll()

    @Query("SELECT * FROM song WHERE is_music = 1")
    fun getSongs(): List<SongEntity>

    @Query("SELECT song_id FROM song WHERE is_music = 0")
    fun getQueue(): List<Int>
}