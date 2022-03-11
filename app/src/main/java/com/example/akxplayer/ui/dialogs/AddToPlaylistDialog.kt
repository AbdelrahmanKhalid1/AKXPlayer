package com.example.akxplayer.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.akxplayer.repository.PlaylistRepository
import com.example.akxplayer.ui.listeners.OnDialogClickListener
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.disposables.Disposable

class AddToPlaylistDialog(private val songId: Long, private val listener: OnDialogClickListener) :
    DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlists = PlaylistRepository.getPlaylists(context!!.contentResolver)
        val array = Array(playlists.size) { "n = $it" }
        for (i in playlists.indices) {
            array[i] = playlists[i].name
        }

        val dialog = AlertDialog.Builder(activity)
        dialog.setTitle("Add To Playlist")
            .setPositiveButton("new playlist") { _, _ ->
                listener.onDialogClick(songId)
            }
            .setNegativeButton("cancel", null)
        if (playlists.isNotEmpty())
            dialog.setItems(array) { _, i ->
                PlaylistRepository.addSongToPlaylist(
                    songId,
                    playlists[i].id,
                    context!!.contentResolver
                ).subscribe(observer)
            }
        else
            dialog.setMessage("There is no Playlists")
        return dialog.create()
    }

    private lateinit var observer: CompletableObserver

    override fun onAttach(context: Context) {
        super.onAttach(context)
        observer = object : CompletableObserver {
            override fun onComplete() {
                Toast.makeText(context, "Successfully added to Playlist", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onSubscribe(d: Disposable?) {
            }

            override fun onError(e: Throwable?) {
                Toast.makeText(context, "Song is Already in Playlist", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
