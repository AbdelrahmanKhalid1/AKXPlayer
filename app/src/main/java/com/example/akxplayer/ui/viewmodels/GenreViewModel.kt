package com.example.akxplayer.ui.viewmodels

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.akxplayer.model.Genre
import com.example.akxplayer.model.Song
import com.example.akxplayer.repository.GenreRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class GenreViewModel : ViewModel() {

    private val genreMutableLiveData = MutableLiveData<List<Genre>>()
    private lateinit var contentResolver: ContentResolver

    fun init(contentResolver: ContentResolver) {
        this.contentResolver = contentResolver
    }

    fun getGenre(): LiveData<List<Genre>> = genreMutableLiveData

    fun loadGenres() {
        GenreRepository.loadGenre(contentResolver).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { genres -> genreMutableLiveData.value = genres }
    }

    fun loadGenreSongs(genreId: Long): LiveData<List<Song>> {
        val songsLiveData = MutableLiveData<List<Song>>()
        GenreRepository.getGenreSongs(genreId, contentResolver).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { songs -> songsLiveData.value = songs }
        return songsLiveData
    }
}
