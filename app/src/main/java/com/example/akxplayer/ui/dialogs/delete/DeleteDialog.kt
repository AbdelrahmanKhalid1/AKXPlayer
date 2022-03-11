package com.example.akxplayer.ui.dialogs.delete

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.akxplayer.ui.listeners.OnDialogClickListener
import io.reactivex.rxjava3.core.Completable

abstract class DeleteDialog(private val id: Long, private val listener: OnDialogClickListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(activity).apply {
            setMessage("Do you want to delete \"${getName(id)}\"?")
            setPositiveButton("ok") { _, _ ->
                deleteData(id).subscribe {
                    listener.onDialogClick(id)
                }
            }
                .setNegativeButton("cancel", null)
        }
        return dialog.create()
    }

    abstract fun deleteData(id: Long): Completable
    abstract fun getName(id: Long): String
}
