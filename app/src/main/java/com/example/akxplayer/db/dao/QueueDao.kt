package com.example.akxplayer.db.dao

import android.text.BoringLayout
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.akxplayer.constants.RepeatMode
import com.example.akxplayer.db.entity.QueueEntity
import com.example.akxplayer.db.entity.SongEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface QueueDao {

    @Insert
    fun initializeTable(queue: QueueEntity)

    @Update
    fun updateQueue(queue: QueueEntity)

    @Query("SELECT * FROM QUEUE WHERE queue_Id = 0")
    fun getQueueData(): QueueEntity

    @Query("UPDATE queue SET queue_title = :title WHERE queue_Id =0")
    fun updateTitle(title: String)

    @Query("UPDATE queue SET song_index = :songIndex WHERE queue_Id = 0")
    fun updateCurrentSong(songIndex: Int)

    @Query("UPDATE queue SET seek_position = :seekPosition WHERE queue_Id = 0")
    fun updateSeekPosition(seekPosition: Long)

    @Query("UPDATE queue SET shuffle_mode = :shuffleMode WHERE queue_Id = 0")
    fun updateShuffleMode(shuffleMode: Boolean)

    @Query("UPDATE queue SET repeat_mode = :repeatMode WHERE queue_Id = 0")
    fun updateRepeatMode(repeatMode: RepeatMode)
}