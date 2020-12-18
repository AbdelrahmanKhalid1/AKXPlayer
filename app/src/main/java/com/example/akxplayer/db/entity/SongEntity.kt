package com.example.akxplayer.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "song")
data class SongEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "song_id")
    var songId: Long = -1,
//    @ForeignKey(entity = QueueEntity::class, parentColumns = )
    @ColumnInfo(name = "queue_id")
    var queueId: Int = -1
)