package com.example.akxplayer.ui.listeners

import com.example.akxplayer.constants.PlayingState
import com.example.akxplayer.constants.RepeatMode

interface OnMediaControlsChange {
    fun onSongChange(position: Int)
    fun onSeekPositionChange(seekPosition: Int)
    fun onPlayerStateChanged(playerState: PlayingState)
    fun onRepeatModeChanged(repeatMode: RepeatMode)
    fun onAddRemoverSongFavorite(isFavorite: Boolean)
    fun onShuffleEnabled(shuffleEnabled: Boolean)
}
