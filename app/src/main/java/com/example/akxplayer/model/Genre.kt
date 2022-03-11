package com.example.akxplayer.model

import android.database.Cursor
import android.os.Parcelable
import android.provider.MediaStore.Audio.Genres.NAME
import android.provider.MediaStore.Audio.Genres._ID
import kotlinx.android.parcel.Parcelize

@Parcelize
class Genre(
    var id: Long = -1,
    var name: String = "",
    var songCount: String = "0"
) : Parcelable {

    companion object {
        fun fetchFromCursor(cursor: Cursor): Genre {
            return Genre(
                cursor.getLong(cursor.getColumnIndex(_ID)),
                cursor.getString(cursor.getColumnIndex(NAME)) ?: "Unknown",
                "0"
            )
        }
    }
}
