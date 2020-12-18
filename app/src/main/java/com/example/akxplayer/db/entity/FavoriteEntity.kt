package com.example.akxplayer.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
data class FavoriteEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "song_id")
    var songId: Long = -1
)