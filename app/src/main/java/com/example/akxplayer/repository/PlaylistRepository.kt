package com.example.akxplayer.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID
import android.provider.MediaStore.Audio.Playlists.Members.PLAY_ORDER
import android.provider.MediaStore.Audio.Playlists.NAME
import android.provider.MediaStore.Audio.Playlists._ID
import com.example.akxplayer.model.Playlist
import com.example.akxplayer.model.Song
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

object PlaylistRepository {

    fun loadPlaylists(contentResolver: ContentResolver): Single<List<Playlist>> =
        Single.create { emitter ->
            val cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                arrayOf(_ID, NAME),
                null,
                null,
                MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER
            )

            val playlists = ArrayList<Playlist>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val playlist = Playlist.fetchFromCursor(
                        cursor,
                        getPlaylistSongCount(
                            cursor.getLong(cursor.getColumnIndex(_ID)),
                            contentResolver
                        )
                    )
                    playlists.add(playlist)
                } while (cursor.moveToNext())
                cursor.close()
            }
            emitter.onSuccess(playlists)
        }

    private fun getPlaylistSongCount(id: Long, contentResolver: ContentResolver): Int {
        var songCount = 0
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id)
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            songCount = cursor.count
            cursor.close()
        }
        return songCount
    }

    fun createPlaylist(name: String, contentResolver: ContentResolver): Single<Long> =
        Single.create { emitter ->
            contentResolver.query(
                EXTERNAL_CONTENT_URI,
                arrayOf(NAME),
                "$NAME = ?",
                arrayOf(name),
                null
            )?.use {
                if (it.count <= 0) {
                    val values = ContentValues(1).apply { put(NAME, name) }
                    emitter.onSuccess(
                        contentResolver.insert(
                            EXTERNAL_CONTENT_URI,
                            values
                        )?.lastPathSegment?.toLong()
                    )
                } else {
                    emitter.onSuccess(-1)
                }
            }
        }

    fun deletePlaylist(playlistId: Long, contentResolver: ContentResolver) {
        val param = "$_ID=$playlistId"
        contentResolver.delete(EXTERNAL_CONTENT_URI, param, null)
    }

    fun renamePlaylist(
        playlistId: Long,
        name: String,
        contentResolver: ContentResolver
    ): Completable = Completable.create { emitter ->
        val where = "_id IN ($playlistId)"
        val values = ContentValues(1).apply { put(NAME, name) }
        contentResolver.update(EXTERNAL_CONTENT_URI, values, where, null)
        emitter.onComplete()
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun getPlaylistName(playlistId: Long, contentResolver: ContentResolver): String {
        var name = ""
        val cursor = contentResolver.query(
            EXTERNAL_CONTENT_URI, arrayOf(NAME),
            "_id = $playlistId", null,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(NAME))
            cursor.close()
        }
        return name
    }

    fun getPlaylists(contentResolver: ContentResolver): List<Playlist> {
        val playlists = ArrayList<Playlist>()
        contentResolver.query(
            EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER
        )?.use {
            it.moveToFirst()
            while (!it.isAfterLast) {
                playlists.add(Playlist.fetchFromCursor(it, 0))
                it.moveToNext()
            }
        }
        return playlists
    }

    fun getPlaylistSongs(playlistId: Long, contentResolver: ContentResolver): Single<List<Song>> =
        Single.create { emitter ->
            val songs = ArrayList<Song>()
            val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val song = Song.fetchFromPlaylistCursor(cursor)
                    songs.add(song)
                    cursor.moveToNext()
                }
            }
            emitter.onSuccess(songs)
        }

    fun addSongToPlaylist(
        songId: Long,
        playlistId: Long,
        contentResolver: ContentResolver
    ): Completable = Completable.create { emitter ->
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        if (isExistInPlaylist(songId, contentResolver, uri)) {
            emitter.onError(null)
        } else {
            val projection = arrayOf("MAX($PLAY_ORDER)")
            contentResolver.query(uri, projection, null, null, null)?.use {
                val playOrder = if (it.moveToFirst()) it.getInt(0) + 1 else 0
                val values = ContentValues().apply {
                    put(PLAY_ORDER, playOrder)
                    put(AUDIO_ID, songId)
                }
                contentResolver.insert(uri, values)
            }
            emitter.onComplete()
        }
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    private fun isExistInPlaylist(
        songId: Long,
        contentResolver: ContentResolver,
        uri: Uri
    ): Boolean {
        var isExist = false
        contentResolver.query(uri, null, "$AUDIO_ID = $songId", null, null)?.use {
            isExist = it.count > 0
        }
        return isExist
    }

    fun removeFromPlaylist(
        playlistId: Long,
        songId: Long,
        contentResolver: ContentResolver
    ) {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        contentResolver.delete(
            uri,
            "$AUDIO_ID = ?",
            arrayOf(songId.toString())
        )
    }
}
