package com.example.akxplayer.ui.listeners

interface OnMediaControlsChange {
    fun onSongChange(position: Int)
    fun onSeekPositionChange(seekPosition: Int)
    fun isPlaying(isPlaying: Boolean)
}