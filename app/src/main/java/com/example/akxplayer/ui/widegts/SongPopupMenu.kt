package com.example.akxplayer.ui.widegts

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.widget.PopupMenu
import androidx.appcompat.widget.AppCompatImageView
import com.example.akxplayer.R
import com.example.akxplayer.ui.listeners.PopupMenuPlaylistListener
import com.example.akxplayer.ui.listeners.PopupMenuListener
import com.example.akxplayer.ui.listeners.PopupMenuSongListener

class SongPopupMenu(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    private val popupMenu = PopupMenu(ContextThemeWrapper(context, R.style.PopupMenuTheme), this)
    private var menuListener: Any? = null
    var playlistId: Long = -1
    var songId: Long = -1

    init {
        isClickable = true
        isFocusable = true
        scaleType = ScaleType.CENTER_INSIDE
        setImageResource(R.drawable.ic_more_vert)
        setOnClickListener {
//            if (playlistId.toInt() != -1)
//                popupMenu.menu.findItem(R.id.popup_song_remove_playlist).isVisible = true
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener(clickListener)
        }
    }

    private val clickListener = PopupMenu.OnMenuItemClickListener {
        return@OnMenuItemClickListener when (it.itemId) {
            R.id.action_play_next -> {
                (menuListener as PopupMenuSongListener).addToQueue(songId)
                true
            }
            R.id.action_add_to_playlist -> {
                (menuListener as PopupMenuSongListener).addToPlaylist(songId)
                true
            }
            R.id.action_goTo_album -> {
                (menuListener as PopupMenuSongListener).goToAlbum(songId)
                true
            }
            R.id.action_goTo_artist -> {
                (menuListener as PopupMenuSongListener).goToArtist(songId)
                true
            }
            R.id.action_delete_song -> {
                (menuListener as PopupMenuSongListener).deleteSong(songId)
                true
            }
            R.id.action_rename -> {
                (menuListener as PopupMenuPlaylistListener).rename(songId)
                true
            }
            R.id.action_delete_playlist->{
                (menuListener as PopupMenuPlaylistListener).deletePlaylist(songId)
                true
            }
            else -> false
        }
    }

    fun setMenu(menuListener: PopupMenuListener, menu: Int){
        this.menuListener = menuListener
        popupMenu.inflate(menu)
    }
}