package com.example.akxplayer.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.akxplayer.R
import com.example.akxplayer.repository.PlaylistRepository
import com.example.akxplayer.ui.listeners.OnDialogClickListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class CreatePlaylistDialog(private val songId: Long, private val listener: OnDialogClickListener?) :
    DialogFragment() {

    private lateinit var editText: EditText
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.dialog_create_playist, null)
        editText = view.findViewById(R.id.editText_playlist)

        val dialog = AlertDialog.Builder(activity).apply {
            setTitle("New Playlist")
            setView(view)
            setPositiveButton("create playlist") { _, _ ->
                PlaylistRepository.createPlaylist(editText.text.toString(), context.contentResolver)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer)
            }
            setNegativeButton("cancel", null)
        }

        return dialog.create()
    }

    private lateinit var observer: SingleObserver<Long>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        observer = object : SingleObserver<Long> {
            override fun onSuccess(playlistId: Long) {
                if (songId > -1) {
                    PlaylistRepository.addSongToPlaylist(songId, playlistId, context.contentResolver)
                        .subscribe { Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show() }
                } else {
                    listener?.onDialogClick(songId)
                }
            }

            override fun onSubscribe(d: Disposable?) {
            }

            override fun onError(e: Throwable?) {
                Toast.makeText(context, "Playlist Already Exist", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
