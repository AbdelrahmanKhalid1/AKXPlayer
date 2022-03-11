package com.example.akxplayer.db.entity

import androidx.room.*

@Entity(tableName = "song")
data class SongEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "song_id")
    var songId: Long,

    @ColumnInfo(name = "is_music")
    var isMusic: Short = 1,

    @ForeignKey(entity = QueueEntity::class, parentColumns = ["queue_id"], childColumns = ["queue_id"])
    @ColumnInfo(name = "queue_id")
    var queueId: Int = -1
)
