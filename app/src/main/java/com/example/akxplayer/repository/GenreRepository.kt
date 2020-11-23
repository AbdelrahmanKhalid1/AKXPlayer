package com.example.akxplayer.repository

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import com.example.akxplayer.model.Genre
import com.example.akxplayer.model.Song
import io.reactivex.rxjava3.core.Single

private const val TAG = "GenreRepository"

object GenreRepository {

    fun loadGenre(contentResolver: ContentResolver):Single<List<Genre>> = Single.create { emitter ->
        val cursor = contentResolver.query(
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Genres.DEFAULT_SORT_ORDER
        )
            val genres = ArrayList<Genre>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val genre = Genre.fetchFromCursor(cursor)
                    val songCount = getGenreSongCount(genre.id, contentResolver)
                    if (songCount > 0) {
                        genre.songCount =
                            if (songCount == 1) "$songCount Song" else "$songCount Songs"
                        genres.add(genre)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
            emitter.onSuccess(genres)
            Log.d(TAG, "eee loadGenre: ${Thread.currentThread().name}")
    }

     private fun getGenreSongCount(id: Long,contentResolver: ContentResolver): Int {
        var songCount = 0
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", id)
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            songCount = cursor.count
            cursor.close()
        }
        return songCount
    }

    fun getGenreSongs(genreId: Long, contentResolver: ContentResolver): Single<List<Song>> = Single.create{ emitter ->
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
        val cursor = contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )

        val songs = ArrayList<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val song = Song.fetchFromCursor(cursor)
                songs.add(song)
            } while (cursor.moveToNext())
            cursor.close()
        }
        emitter.onSuccess(songs)
    }
}