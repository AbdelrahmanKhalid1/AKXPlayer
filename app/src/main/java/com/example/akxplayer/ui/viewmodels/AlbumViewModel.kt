package com.example.akxplayer.ui.viewmodels

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.akxplayer.model.Album
import com.example.akxplayer.repository.AlbumRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class AlbumViewModel : ViewModel() {

    private val albumMutableLiveData = MutableLiveData<List<Album>>()
    private lateinit var contentResolver: ContentResolver
    private var artistId: Long = -1

    fun init(contentResolver: ContentResolver, artistId: Long) {
        this.contentResolver = contentResolver
        this.artistId = artistId
    }

    fun getAlbums(): LiveData<List<Album>> = albumMutableLiveData

    fun loadAlbums() {
        AlbumRepository.loadAlbums(artistId, contentResolver).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { albums -> albumMutableLiveData.value = albums }
    }
}
