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
import com.example.akxplayer.db.dao.FavoriteDao
import com.example.akxplayer.db.dao.SongDao
import com.example.akxplayer.db.entity.FavoriteEntity
import com.example.akxplayer.db.entity.SongEntity
// import com.example.akxplayer.db.AkxDatabase
// import com.example.akxplayer.db.dao.SongDao
// import com.example.akxplayer.db.entity.SongEntity
import com.example.akxplayer.model.Song
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

private const val TAG = "SongRepository"

object SongRepository {

    private lateinit var contentResolver: ContentResolver
    private lateinit var songDao: SongDao
    private lateinit var favoriteDao: FavoriteDao

    fun init(context: Context) {
        val db = AkxDatabase.getInstance(context)
        songDao = db.songDao()
        favoriteDao = db.favoriteDao()
        contentResolver = context.contentResolver
    }

    fun loadSongs(
        albumId: Long,
        artistId: Long
    ): Single<List<Song>> =
        Single.create { emitter ->
            val songs = ArrayList<Song>()
            var selection = "($IS_MUSIC !=0)"
            selection += if (albumId.compareTo(-1) != 0) " AND ($ALBUM_ID = $albumId)" else ""
            selection += if (artistId.compareTo(-1) != 0) " AND ($ARTIST_ID = $artistId)" else ""

            val cursor: Cursor? = contentResolver.query(
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

//    fun updateSongs(songList: List<Song>): Completable = Completable.create {
//        val songEntityList = ArrayList<SongEntity>()
//        for (song in songList) {
//            songEntityList.add(SongEntity(song.id, 0))
//        }
//        songDao.deleteAll()
//        songDao.insertSongs(songEntityList)
// //        return null
//    }
//    .subscribeOn(Schedulers.io())

    fun deleteSong(songId: Long) {
        contentResolver.delete(EXTERNAL_CONTENT_URI, "$_ID = $songId", null)
    }

    fun getSongsForIds(ids: LongArray): List<Song> {
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
        return songs
    }

    fun getSongOrder(): List<Int> = songDao.getQueue()

    fun getSongIds(): LongArray? {
        val songEntityList = songDao.getSongs()
        if (songEntityList.isEmpty())
            return null
        val longArray = LongArray(songEntityList.size)
        for (i in songEntityList.indices)
            longArray[i] = songEntityList[i].songId
        return longArray
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

    fun saveSongList(songList: List<Song>, queue: List<Int>): Completable = Completable.create {
        songDao.deleteAll()
        val songEntityList = ArrayList<SongEntity>()
        val queueEntityList = ArrayList<SongEntity>()

        for (song in songList) {
            songEntityList.add(SongEntity(song.id, 1, 0))
        }
        songDao.insertSongs(songEntityList)

        for (order in queue) {
            queueEntityList.add(SongEntity(order.toLong(), 0, 0))
        }
        songDao.insertSongs(queueEntityList)
        it.onComplete()
    }

    private fun addToFavorite(songId: Long) {
        favoriteDao.add(FavoriteEntity(songId))
    }

    private fun removeFromFavorite(songId: Long) {
        favoriteDao.remove(FavoriteEntity(songId))
    }

    fun isFavorite(songId: Long): Single<Boolean> = Single.create { emitter ->
        val isFavorite = favoriteDao.isFavorite(songId)
        emitter.onSuccess(songId == isFavorite)
    }

    fun addRemoveFavorite(songId: Long): Single<Boolean> = Single.create { emitter ->
        val isFavorite = favoriteDao.isFavorite(songId)
        if (isFavorite == songId) {
            removeFromFavorite(songId)
            emitter.onSuccess(false)
        } else {
            addToFavorite(songId)
            emitter.onSuccess(true)
        }
    }
}
