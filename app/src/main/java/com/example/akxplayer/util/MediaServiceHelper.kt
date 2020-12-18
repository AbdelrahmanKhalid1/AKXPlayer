package com.example.akxplayer.util
//
//import android.content.Intent
//import android.media.AudioManager
//import android.media.session.MediaSession
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.view.KeyEvent
//import com.example.akxplayer.constants.PlayingState
//import com.example.akxplayer.notification.AKX
//
//class MediaServiceHelper : MediaSessionCompat.Callback() {
//    override fun onPause() {
//        pause()
//    }
//
//    override fun onPlay() {
//        val result = requestAudioFocus()
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            player.start()
//            mediaSession.isActive = true
//            setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
//            mediaControls.onPlayerStateChanged(PlayingState.PLAYING)
//            startSeeker().subscribe()
//            startForeground(
//                1,
//                AKX.createNotification(
//                    baseContext,
//                    mediaSession,
//                    player.isPlaying,
//                    isFavorite,
//                    repeatMode
//                )
//            )
//        }
//    }
//
//    override fun onStop() {
//        stop()
//    }
//
//    override fun onSkipToNext() {
//        playNextSong()
//    }
//
//    override fun onSkipToPrevious() {
//        playPreviousSong()
//    }
//
//    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
//        val keyEvent =
//            mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
//        when (keyEvent?.keyCode) {
//            KeyEvent.KEYCODE_MEDIA_REWIND -> setRepeatMode()
//            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> addRemoveSongFavorite()
//        }
//        return super.onMediaButtonEvent(mediaButtonEvent)
//    }
//
//    override fun onSeekTo(pos: Long) {
//        val position = pos.toInt()
//        seekTo(position)
//        mediaControls.onSeekPositionChange(position)
//    }
//
//}