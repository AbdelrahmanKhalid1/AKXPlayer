package com.example.akxplayer.ui.dialogs

import android.content.Context
import com.example.akxplayer.repository.SongRepository
import com.example.akxplayer.ui.listeners.OnDialogClickListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

class DeleteSongDialog(songId: Long, listener: OnDialogClickListener) : DeleteDialog(songId, listener) {

    override fun deleteData(id: Long): Completable = Completable.create {
        SongRepository.deleteSong(id)
        it.onComplete()
    }.observeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    override fun getName(id: Long): String = SongRepository.getSongTitle(id)
}