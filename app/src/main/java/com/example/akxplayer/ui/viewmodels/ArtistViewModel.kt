package com.example.akxplayer.ui.viewmodels

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.akxplayer.model.Album
import com.example.akxplayer.model.Artist
import com.example.akxplayer.model.Song
import com.example.akxplayer.repository.ArtistRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class ArtistViewModel : ViewModel() {

    private val artistMutableLiveData = MutableLiveData<List<Artist>>()
    private lateinit var contentResolver: ContentResolver
    fun init(contentResolver: ContentResolver) {
        this.contentResolver = contentResolver
    }

    fun getArtist(): LiveData<List<Artist>> = artistMutableLiveData

    fun loadArtist() {
        ArtistRepository.loadArtist(contentResolver).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { artists -> artistMutableLiveData.value = artists }
    }

    fun loadArtistSongs(artistId: Long): LiveData<List<Song>> {
        val songLiveData = MutableLiveData<List<Song>>()
        ArtistRepository.loadArtistSongs(artistId, contentResolver).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { songs -> songLiveData.value = songs }
        return songLiveData
    }

    fun loadArtistAlbum(artistId: Long): LiveData<List<Album>> {
        val albumLiveData = MutableLiveData<List<Album>>()
        ArtistRepository.loadArtistAlbum(artistId, contentResolver).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { albums -> albumLiveData.value = albums }
        return albumLiveData
    }
}
