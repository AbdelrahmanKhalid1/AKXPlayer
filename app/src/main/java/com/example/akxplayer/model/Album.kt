package com.example.akxplayer.model

import android.database.Cursor
import android.graphics.Bitmap
import android.os.Parcelable

import android.provider.MediaStore.Audio.Albums._ID
import android.provider.MediaStore.Audio.Albums.ALBUM
import android.provider.MediaStore.Audio.Albums.ARTIST_ID
import android.provider.MediaStore.Audio.Albums.ARTIST
import android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS
import android.provider.MediaStore.Audio.Albums.FIRST_YEAR
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album(
    var id: Long = -1,
    var name: String = "",
    var artistId: Long = -1,
    var artist: String = "",
    var songCount: Int = 0,
    var year: Int = 0
) : Parcelable {

    fun getSongCountString():String{
        return if(songCount == 1) "$songCount Song" else "$songCount Songs"
    }
    companion object {
        fun fetchFromCursor(cursor: Cursor): Album {
            return Album(
                cursor.getLong(cursor.getColumnIndex(_ID)),
                cursor.getString(cursor.getColumnIndex(ALBUM)),
                cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                cursor.getString(cursor.getColumnIndex(ARTIST)),
                cursor.getInt(cursor.getColumnIndex(NUMBER_OF_SONGS)),
                cursor.getInt(cursor.getColumnIndex(FIRST_YEAR))
            )
        }
    }
}