package com.example.akxplayer.ui.listeners

interface PopupMenuListener {

}

interface PopupMenuSongListener : PopupMenuListener{
    fun addToQueue(songId: Long)

    fun addToPlaylist(songId: Long)

    fun goToAlbum(songId: Long)

    fun goToArtist(songId: Long)

    fun deleteSong(songId: Long)

    fun removeFromPlaylist(songId: Long, playlistId: Long)
}

interface PopupMenuPlaylistListener: PopupMenuListener {

    fun rename(playlistId: Long)

    fun deletePlaylist(playlistId: Long)
}