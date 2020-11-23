package com.example.akxplayer.ui.viewmodels

import android.content.ContentResolver
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    fun init(contentResolver: ContentResolver,albumId: Long, artistId: Long, genreId: Long, playlistId: Long) {
        this.contentResolver = contentResolver
        this.albumId = albumId
        this.artistId = artistId
        this.genreId = genreId
        this.playlistId = playlistId
    }

    fun getSongs(): LiveData<List<Song>> = songMutableLiveData

    fun loadSongs() {
        when {
            genreId.compareTo(-1) != 0 -> GenreRepository.getGenreSongs(genreId, contentResolver)
                .subscribeOn(Schedulers.io())
                .subscribe { songs -> songMutableLiveData.postValue(songs) }
            playlistId.compareTo(-1) != 0 -> PlaylistRepository.getPlaylistSongs(
                playlistId,
                contentResolver
            ).subscribeOn(Schedulers.io())
                .subscribe { songs -> songMutableLiveData.postValue(songs) }
            else -> {
                Log.d(TAG, "loadSongs: $albumId $artistId $genreId $playlistId")
                SongRepository.loadSongs(albumId, artistId)
                    .subscribeOn(Schedulers.io())
                    .subscribe { songs -> songMutableLiveData.postValue(songs) }
            }
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