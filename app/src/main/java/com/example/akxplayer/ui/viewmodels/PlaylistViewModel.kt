package com.example.akxplayer.ui.viewmodels

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.akxplayer.model.Playlist
import com.example.akxplayer.repository.PlaylistRepository
import io.reactivex.rxjava3.schedulers.Schedulers

class PlaylistViewModel : ViewModel() {

    private lateinit var contentResolver: ContentResolver
    private val playlistLiveData = MutableLiveData<List<Playlist>>()

    fun init(contentResolver: ContentResolver) {
        this.contentResolver = contentResolver
    }

    fun getPlaylists(): LiveData<List<Playlist>> = playlistLiveData

    fun loadPlaylist() {
        PlaylistRepository.loadPlaylists(contentResolver).subscribeOn(Schedulers.io())
            .subscribe { playlists -> playlistLiveData.postValue(playlists) }
    }

//    fun getPlaylistSongs(playlistId: Long): LiveData<List<Song>> {
//        val liveData = MutableLiveData<List<Song>>()
//        PlaylistRepository.getPlaylistSongs(playlistId, contentResolver)
//            .subscribeOn(Schedulers.io()).subscribe { songs -> liveData.postValue(songs) }
//        return liveData
//    }
}