package com.example.akxplayer.model

import android.database.Cursor
import android.os.Parcelable
import android.provider.MediaStore.Audio.Artists._ID
import android.provider.MediaStore.Audio.Artists.ARTIST
import android.provider.MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
import android.provider.MediaStore.Audio.Artists.NUMBER_OF_TRACKS
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Artist(
    var id : Long = -1,
    var name : String = "",
    var albumCount : Int = 0,
    var songCount : Int = 0
) : Parcelable {

    fun getAlbumCountString() = if(albumCount == 1) "$albumCount Album | " else "$albumCount Albums | "
    fun getSongCountString() = if(songCount == 1) "$songCount Song" else "$songCount Songs"

    companion object{
        fun fetchFromCursor(cursor: Cursor):Artist{
            return Artist(
                cursor.getLong(cursor.getColumnIndex(_ID)),
                cursor.getString(cursor.getColumnIndex(ARTIST)) ?: "Unknown",
                cursor.getInt(cursor.getColumnIndex(NUMBER_OF_ALBUMS)),
                cursor.getInt(cursor.getColumnIndex(NUMBER_OF_TRACKS))
            )
        }
    }
}