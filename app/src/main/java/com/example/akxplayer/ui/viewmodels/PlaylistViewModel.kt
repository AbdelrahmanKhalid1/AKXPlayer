package com.example.akxplayer.ui.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.akxplayer.db.AkxDatabase
import com.example.akxplayer.model.Playlist
import com.example.akxplayer.repository.PlaylistRepository
import io.reactivex.rxjava3.schedulers.Schedulers

class PlaylistViewModel : ViewModel() {

    private val playlistLiveData = MutableLiveData<List<Playlist>>()

    fun getPlaylists(): LiveData<List<Playlist>> = playlistLiveData

    fun loadPlaylist(context: Context) {
        PlaylistRepository.loadPlaylists(context.contentResolver).subscribeOn(Schedulers.io())
            .subscribe { playlists ->
                val favoriteDao = AkxDatabase.getInstance(context).favoriteDao()
                Log.d("testDatabase", "loadPlaylist: ${favoriteDao.getNumOfFavoriteSongs()}")
                val favoriteCount = favoriteDao.getNumOfFavoriteSongs()
                if(favoriteCount > 0)
                    (playlists as ArrayList).add(0, Playlist(-2, "Favorites", favoriteCount))
                playlistLiveData.postValue(playlists)
            }
    }
}