package com.example.akxplayer.model

import android.content.ContentResolver
import android.database.Cursor
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore.Audio.Media.ALBUM
import android.provider.MediaStore.Audio.Media.ALBUM_ID
import android.provider.MediaStore.Audio.Media.ARTIST
import android.provider.MediaStore.Audio.Media.ARTIST_ID
import android.provider.MediaStore.Audio.Media.DURATION
import android.provider.MediaStore.Audio.Media.TITLE
import android.provider.MediaStore.Audio.Media._ID
import android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.example.akxplayer.util.Util
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(
    var id: Long = -1,
    var title: String = "",
    var duration: String = "",
    var albumId: Long = -1,
    var album: String = "",
    var artistId: Long = -1,
    var artist: String = ""
) : Parcelable {

    fun fetchDuration():String{
        var fetchedDuration = ""
        var durationInt = duration.toInt() / 1000 // convert from milli to sec
        for (i in 0..1) {
            val numOf = durationInt / 60
            if (numOf == 0) //has no min or hour or both
                break

            val time = durationInt % (numOf * 60)
            fetchedDuration = if (time / 10 == 0)
                ":0$time$fetchedDuration"
            else
                ":$time$fetchedDuration"
            durationInt /= 60
        }
        return "$durationInt$fetchedDuration"
    }

    fun getDuration() = duration.toInt()

    companion object {
        fun fetchFromCursor(cursor: Cursor): Song {
            return Song(
                cursor.getLong(cursor.getColumnIndex(_ID)),
                cursor.getString(cursor.getColumnIndex(TITLE)),
                cursor.getString(cursor.getColumnIndex(DURATION)),
                cursor.getLong(cursor.getColumnIndex(ALBUM_ID)),
                cursor.getString(cursor.getColumnIndex(ALBUM)),
                cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                cursor.getString(cursor.getColumnIndex(ARTIST))
            )
        }

        fun fetchFromPlaylistCursor(cursor: Cursor):Song{
            return Song(
                cursor.getLong(cursor.getColumnIndex(AUDIO_ID)),
                cursor.getString(cursor.getColumnIndex(TITLE)),
                cursor.getString(cursor.getColumnIndex(DURATION)),
                cursor.getLong(cursor.getColumnIndex(ALBUM_ID)),
                cursor.getString(cursor.getColumnIndex(ALBUM)),
                cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                cursor.getString(cursor.getColumnIndex(ARTIST))
            )
        }
        fun buildMediaDescription(song: Song, contentResolver: ContentResolver): MediaDescriptionCompat {
            val extras = Bundle()
            val bitmap = Util.getPic(song.albumId, contentResolver)
            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
            return MediaDescriptionCompat.Builder()
                .setMediaId(song.id.toString())
                .setIconBitmap(bitmap)
                .setTitle(song.title)
                .setDescription(song.artist)
                .setExtras(extras)
                .build()
        }
    }
}


