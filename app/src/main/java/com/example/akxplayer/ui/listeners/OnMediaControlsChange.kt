package com.example.akxplayer.ui.listeners

import com.example.akxplayer.constants.PlayingState

interface OnMediaControlsChange {
    fun onSongChange(position: Int)
    fun onSeekPositionChange(seekPosition: Int)
    fun onPlayerStateChanged(playerState: PlayingState)
}