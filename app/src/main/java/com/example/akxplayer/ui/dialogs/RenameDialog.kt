package com.example.akxplayer.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.akxplayer.R
import com.example.akxplayer.repository.PlaylistRepository
import com.example.akxplayer.ui.listeners.OnDialogClickListener

class RenameDialog(private val playlistId: Long, private val listener: OnDialogClickListener) :
    DialogFragment() {

    private lateinit var editText: EditText
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(context, R.layout.dialog_create_playist, null)

        editText = view.findViewById(R.id.editText_playlist)
        editText.setText(PlaylistRepository.getPlaylistName(playlistId, context!!.contentResolver))

        val dialog = AlertDialog.Builder(activity).apply {
            setTitle("Rename Playlist")
            setView(view)
            setPositiveButton("done") { _, _ ->
                PlaylistRepository.renamePlaylist(
                    playlistId,
                    editText.text.toString(),
                    context.contentResolver
                ).subscribe { listener.onDialogClick(-1) }
            }
            setNegativeButton("cancel", null)
        }
        return dialog.create()
    }
}