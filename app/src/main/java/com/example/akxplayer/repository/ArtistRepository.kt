package com.example.akxplayer.repository

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import com.example.akxplayer.model.Album
import com.example.akxplayer.model.Artist
import com.example.akxplayer.model.Song
import io.reactivex.rxjava3.core.Single

private const val TAG = "ArtistRepository"

object ArtistRepository {

    fun loadArtist(contentResolver: ContentResolver): Single<List<Artist>> = Single.create { emitter ->
        val cursor = contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
        )

        val artists = ArrayList<Artist>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                artists.add(Artist.fetchFromCursor(cursor))
            } while (cursor.moveToNext())
            cursor.close()
        }
        emitter.onSuccess(artists)
        Log.d(TAG, "loadArtist: ${Thread.currentThread().name}")
    }

    fun loadArtistSongs(artistId: Long,contentResolver: ContentResolver): Single<List<Song>> = Single.create { emitter ->
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            "is_music=1 AND artist_id=$artistId",
            null,
            null
        )

        val songs = ArrayList<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(Song.fetchFromCursor(cursor))
            } while (cursor.moveToNext())
            cursor.close()
        }
        emitter.onSuccess(songs)
    }

    fun loadArtistAlbum(artistId: Long,contentResolver: ContentResolver): Single<List<Album>> = Single.create { emitter ->
        val cursor = contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            null,
            "artist_id=$artistId",
            null,
            null
        )

        val albums = ArrayList<Album>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                albums.add(Album.fetchFromCursor(cursor))
            } while (cursor.moveToNext())
            cursor.close()
        }
        emitter.onSuccess(albums)
    }

    fun loadArtistById(artistId: Long,contentResolver: ContentResolver): Single<Artist> = Single.create { emitter->
        val cursor = contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            null,
            "_id=$artistId",
            null,
            null
        )

        if(cursor != null && cursor.moveToFirst()){
            emitter.onSuccess(Artist.fetchFromCursor(cursor))
        }
    }
}