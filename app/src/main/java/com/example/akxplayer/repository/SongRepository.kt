package com.example.akxplayer.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.Audio.Media.IS_MUSIC
import android.provider.MediaStore.Audio.Media.DEFAULT_SORT_ORDER
import android.provider.MediaStore.Audio.Media._ID
import android.provider.MediaStore.Audio.Media.ALBUM_ID
import android.provider.MediaStore.Audio.Media.ARTIST_ID
import android.util.Log
import com.example.akxplayer.db.AkxDatabase
import com.example.akxplayer.db.dao.SongDao
import com.example.akxplayer.db.entity.SongEntity
import com.example.akxplayer.model.Song
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

private const val TAG = "SongRepository"

object SongRepository {

    private lateinit var contentResolver: ContentResolver
    private lateinit var songDao: SongDao

    fun init(context: Context) {
        songDao = AkxDatabase.getInstance(context).songDao()
        contentResolver = context.contentResolver
    }

    fun loadSongs(
        albumId: Long,
        artistId: Long
    ): Single<List<Song>> =
        Single.create { emitter ->
            val songs = ArrayList<Song>()
            val cursor: Cursor?
            var selection = "($IS_MUSIC !=0)"
            selection += if (albumId.compareTo(-1) != 0) " AND ($ALBUM_ID = $albumId)" else ""
            selection += if (artistId.compareTo(-1) != 0) " AND ($ARTIST_ID = $artistId)" else ""
            cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                null,
                selection,
                null,
                DEFAULT_SORT_ORDER
            )

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val song = Song.fetchFromCursor(cursor)
                    Log.d(TAG, "loadSongs: $song")
                    songs.add(song)
                } while (cursor.moveToNext())
                cursor.close()
            }
            emitter.onSuccess(songs)
        }

    fun getSongForId(songId: Long): Single<Song> =
        Single.create { emitter ->
            val cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                null,
                "$IS_MUSIC !=0 AND $_ID = $songId",
                null,
                DEFAULT_SORT_ORDER
            )

            if (cursor != null && cursor.moveToFirst()) {
                emitter.onSuccess(Song.fetchFromCursor(cursor))
                cursor.close()
            }
        }

    fun getSongsForIds(ids: LongArray): Single<List<Song>> =
        Single.create { emitter ->
            val songs = ArrayList<Song>()
            var selection = "_id IN ("
            for (id in ids) {
                selection += "$id,"
            }
            selection = selection.removeRange(selection.length - 1, selection.length)
            selection += ")"

            val cursor = contentResolver.query(
                EXTERNAL_CONTENT_URI,
                null,
                "$IS_MUSIC !=0 AND $selection",
                null,
                DEFAULT_SORT_ORDER
            )

            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    songs.add(Song.fetchFromCursor(cursor))
                    cursor.moveToNext()
                }
                cursor.close()
            }
            Log.d(TAG, "setControls: ${songs.size} $songs")
            emitter.onSuccess(songs)
        }

    fun getSongIds(): LongArray? {
        val songEntityList = songDao.getAllSongs()
        if (songEntityList.isEmpty())
            return null
        val longArray = LongArray(songEntityList.size)
        for (i in songEntityList.indices)
            longArray[i] = songEntityList[i].songId
        return longArray
    }

    fun updateSongs(songList: List<Song>): Completable = Completable.create {
        val songEntityList = ArrayList<SongEntity>()
        for (song in songList) {
            songEntityList.add(SongEntity(song.id, 0))
        }
        songDao.deleteAll()
        songDao.insertSongs(songEntityList)
    }.subscribeOn(Schedulers.io())

    fun deleteSong(songId: Long) {
        contentResolver.delete(EXTERNAL_CONTENT_URI, "$_ID = $songId", null)
    }

    fun getSongTitle(songId: Long): String {
        var title = ""
        contentResolver.query(
            EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.TITLE),
            "$_ID = $songId",
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                title = it.getString(0)
            }
        }
        return title
    }
}