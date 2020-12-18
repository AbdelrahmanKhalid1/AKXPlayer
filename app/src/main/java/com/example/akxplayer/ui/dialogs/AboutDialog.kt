package com.example.akxplayer.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.example.akxplayer.R

class AboutDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = context!!.getString(R.string.about_message)
        val reference = "<br/><br/> Designed and developed by <b>Abdelrahman Khalid</b>."
//        val link = "<br/> Check out my<a href=\"https://github.com/AbdelrahmanKhalid1\">GitHub!</a>"
        val dialogBuilder = AlertDialog.Builder(activity).apply {
            setTitle(R.string.app_name)
            setView(view)
            setMessage(HtmlCompat.fromHtml("$message$reference", HtmlCompat.FROM_HTML_MODE_LEGACY))
            setPositiveButton("dismiss", null)
        }
        return dialogBuilder.create()
    }
}