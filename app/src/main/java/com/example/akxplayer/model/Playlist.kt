package com.example.akxplayer.model

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore.Audio.Playlists._ID
import android.provider.MediaStore.Audio.Playlists.NAME
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int
):Parcelable {
    fun getSongCount():String = if(songCount == 1) "1 Song" else "$songCount Songs"

    companion object {
        fun fetchFromCursor(cursor: Cursor, songCount: Int): Playlist = Playlist(
            cursor.getLong(cursor.getColumnIndex(_ID)),
            cursor.getString(cursor.getColumnIndex(NAME)),
            songCount
        )
    }
}