package com.example.akxplayer.ui.dialogs.delete

import com.example.akxplayer.repository.PlaylistRepository
import com.example.akxplayer.ui.dialogs.delete.DeleteDialog
import com.example.akxplayer.ui.listeners.OnDialogClickListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

class DeletePlaylistDialog(playlistId: Long, listener: OnDialogClickListener) :
    DeleteDialog(playlistId, listener) {

    override fun deleteData(id: Long): Completable = Completable.create {
        PlaylistRepository.deletePlaylist(id, context!!.contentResolver)
        it.onComplete()
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    override fun getName(id: Long): String =
        PlaylistRepository.getPlaylistName(id, context!!.contentResolver)
}