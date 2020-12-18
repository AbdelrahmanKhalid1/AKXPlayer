package com.example.akxplayer.ui.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.akxplayer.db.AkxDatabase
import com.example.akxplayer.model.Album
import com.example.akxplayer.model.Artist
import com.example.akxplayer.model.Song
import com.example.akxplayer.repository.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.properties.Delegates

private const val TAG = "SongViewModel"

class SongViewModel : ViewModel() {

    private val songMutableLiveData = MutableLiveData<List<Song>>()
    private lateinit var contentResolver: ContentResolver
    private var albumId by Delegates.notNull<Long>()
    private var artistId by Delegates.notNull<Long>()
    private var genreId by Delegates.notNull<Long>()
    private var playlistId by Delegates.notNull<Long>()

    fun init(
        contentResolver: ContentResolver,
        albumId: Long,
        artistId: Long,
        genreId: Long,
        playlistId: Long
    ) {
        this.contentResolver = contentResolver
        this.albumId = albumId
        this.artistId = artistId
        this.genreId = genreId
        this.playlistId = playlistId
    }

    fun getSongs(): LiveData<List<Song>> = songMutableLiveData

    fun loadSongs(context: Context) {
        when {
            genreId.compareTo(-1) != 0 -> GenreRepository.getGenreSongs(genreId, contentResolver) //load genre members (songs for genre)
                .subscribeOn(Schedulers.io())
                .subscribe { songs -> songMutableLiveData.postValue(songs) }

            playlistId.compareTo(-2) == 0 -> //load favorite songs
                Single.create<List<Song>> {
                    val songIds = AkxDatabase.getInstance(context).favoriteDao().getFavoriteSongs()
                    it.onSuccess(SongRepository.getSongsForIds(songIds))
                }.subscribeOn(Schedulers.io())
                    .subscribe { songs -> songMutableLiveData.postValue(songs) }

            playlistId.compareTo(-1) != 0 -> PlaylistRepository.getPlaylistSongs( //load songs from playlist
                playlistId,
                contentResolver
            ).subscribeOn(Schedulers.io())
                .subscribe { songs -> songMutableLiveData.postValue(songs) }

            else -> //load songs from an album or artist or all songs
                SongRepository.loadSongs(albumId, artistId)
                    .subscribeOn(Schedulers.io())
                    .subscribe { songs -> songMutableLiveData.postValue(songs) }

        }
    }

    fun getSongAlbum(songId: Long, contentResolver: ContentResolver): Single<Album> =
        Single.create { emitter ->
            SongRepository.getSongForId(songId).subscribe { song ->
                AlbumRepository.loadAlbumById(song.albumId, contentResolver)
                    .subscribe { album ->
                        emitter.onSuccess(album)
                    }
            }
        }

    fun getSongArtist(songId: Long, contentResolver: ContentResolver): Single<Artist> =
        Single.create { emitter ->
            SongRepository.getSongForId(songId).subscribe { song ->
                ArtistRepository.loadArtistById(song.artistId, contentResolver)
                    .subscribe { artist -> emitter.onSuccess(artist) }
            }
        }

    fun removeFromPlaylist(
        songId: Long,
        playlistId: Long,
        contentResolver: ContentResolver
    ): Completable = Completable.create {
        PlaylistRepository.removeFromPlaylist(playlistId, songId, contentResolver)
        it.onComplete()
    }.subscribeOn(Schedulers.io())

//    fun deleteSong(songId: Long): Completable = Completable.create {
//        SongRepository.deleteSong(songId)
//        it.onComplete()
//    }
}