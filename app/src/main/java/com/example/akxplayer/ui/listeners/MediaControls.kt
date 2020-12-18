package com.example.akxplayer.ui.listeners

interface MediaControls {

    fun onSongChange(songIndex: Int)
    fun onSeekPositionChange(seekPosition: Long)
    fun onQueueEnds()
    fun isPlaying(isPlaying: Boolean)

}