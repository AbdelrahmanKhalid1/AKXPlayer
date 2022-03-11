package com.example.akxplayer.repository

import android.content.ContentResolver
import android.provider.MediaStore.Audio.Albums.ARTIST_ID
import android.provider.MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import android.util.Log
import com.example.akxplayer.model.Album
import io.reactivex.rxjava3.core.Single

private const val TAG = "AlbumRepository"

object AlbumRepository {

    fun loadAlbums(artistId: Long, contentResolver: ContentResolver): Single<List<Album>> =
        Single.create { emitter ->
            val selection = if (artistId.compareTo(-1) == 0) null else "($ARTIST_ID = $artistId)"
            val cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                null,
                selection,
                null,
                DEFAULT_SORT_ORDER
            )

            val albums = ArrayList<Album>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    albums.add(Album.fetchFromCursor(cursor))
                } while (cursor.moveToNext())
                cursor.close()
            }
            emitter.onSuccess(albums)
            Log.d(TAG, "loadAlbums: ${Thread.currentThread().name}")
        }

    fun loadAlbumById(albumId: Long, contentResolver: ContentResolver): Single<Album> =
        Single.create { emitter ->
            Log.d(TAG, "loadAlbumById: $albumId")
            val cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                null,
                "_id=$albumId",
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val album = Album.fetchFromCursor(cursor)
                emitter.onSuccess(album)
                cursor.close()
            }
        }
}
