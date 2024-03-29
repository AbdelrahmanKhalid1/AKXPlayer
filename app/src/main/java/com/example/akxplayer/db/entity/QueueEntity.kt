package com.example.akxplayer.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.akxplayer.constants.RepeatMode
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "queue")
@Parcelize
data class QueueEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "queue_Id")
    var id: Int = 0,
    @ColumnInfo(name = "queue_title")
    var title: String = "",
    @ColumnInfo(name = "song_index")
    var currentSong: Int = -1,
    @ColumnInfo(name = "seek_position")
    var seekPosition: Int = 0,
    @ColumnInfo(name = "repeat_mode")
    var repeatMode: RepeatMode = RepeatMode.REPEAT_OFF,
    @ColumnInfo(name = "shuffle_mode")
    var shuffleEnabled: Boolean = false
):Parcelable