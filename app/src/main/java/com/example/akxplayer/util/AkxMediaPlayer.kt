package com.example.akxplayer.util
//
//import android.content.ContentResolver
//import android.content.ContentUris
//import android.content.Context
//import android.content.Intent
//import android.media.AudioAttributes
//import android.media.AudioFocusRequest
//import android.media.AudioManager
//import android.media.MediaPlayer
//import android.os.Build
//import android.provider.MediaStore
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.util.Log
//import android.view.KeyEvent
//import com.example.akxplayer.R
//import com.example.akxplayer.constants.PlayingState
//import com.example.akxplayer.constants.RepeatMode
//import com.example.akxplayer.constants.ShuffleMode
//import com.example.akxplayer.model.Song
//import com.example.akxplayer.notification.AKX
//import io.reactivex.rxjava3.core.Single
//import io.reactivex.rxjava3.schedulers.Schedulers
//import java.io.FileNotFoundException
//
//private const val TAG = "MediaSession"
//
//class AkxMediaPlayer(context: Context): MediaSessionCompat.Callback() {
//
////    private lateinit var onControlsChange: OnMediaControlsChange
//    private lateinit var mediaPlayer: MediaPlayer
//    private val contentResolver: ContentResolver
//    var songList = emptyList<Song>()
//    var queue = emptyList<Int>()
//    var currentPosition = -1
//    var repeatMode = RepeatMode.REPEAT_OFF
//    var shuffleMode = ShuffleMode.DISABLE
//    private lateinit var mediaSession: MediaSessionCompat
//    private lateinit var player: MediaPlayer
//    private lateinit var audioManager: AudioManager
//    private lateinit var audioFocusRequest: AudioFocusRequest
////    private var repeatMode = RepeatMode.REPEAT_OFF
////    private var songList = emptyList<Song>()
////    private var queue = emptyList<Int>()
////    private var currentPosition = -1
//    init {
//        contentResolver = context.contentResolver
//        mediaSession = MediaSessionCompat(context, TAG)
//        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
//        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
//            override fun onPause() {
//                pause()
//            }
//
//            override fun onPlay() {
//                play()
//            }
//
//            override fun onSkipToNext() {
//                playNextSong()
//            }
//
//            override fun onSkipToPrevious() {
//                playPreviousSong()
//            }
//
//            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
//                val keyCode = mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)?.keyCode
//                when(keyCode){
//                    KeyEvent.KEYCODE_MEDIA_REWIND -> setRepeatMode()
//                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> addToFavorite()
//                }
//                return super.onMediaButtonEvent(mediaButtonEvent)
//            }
//        })
//
//        player = MediaPlayer()
//        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                .setAudioAttributes(
//                    AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build()
//                )
//                .setAcceptsDelayedFocusGain(true)
//                .setOnAudioFocusChangeListener(this).build()
//        }
//    }
//
//    fun setMediaSession(
//        position: Int,
//        songList: List<Song>
//    ): Boolean {
//        currentPosition = position
//        this.songList = songList
//        setQueue()
//        setMediaPlayer()
//        return true
//    }
//
//    private fun setQueue() {
//        val newQueue = ArrayList<Int>()
//        if (shuffleMode == ShuffleMode.DISABLE) {
//            for (i in songList.indices)
//                newQueue.add(i)
//        } else {
//            //TODO
//        }
//        queue = newQueue
//    }
//
//    fun seekTo(position: Int, seekPosition: Long) {
//        if (position == currentPosition) {
//            mediaPlayer.seekTo(seekPosition.toInt())
//            return
//        }
//        currentPosition = position
////        setMediaPlayer()
//    }
//
//    fun isPlaying(): Boolean = mediaPlayer.isPlaying
//
//    fun setShuffle(shuffleModeMode: ShuffleMode) {
//        this.shuffleMode = shuffleModeMode
//        setQueue()
//    }
//
//    private fun initSeekPosition() {
//        Single.create<Int> {
//            while (mediaPlayer.isPlaying) {
//                try {
////                    onControlsChange.onSeekPositionChange(mediaPlayer.currentPosition)
//                } catch (ignore: InterruptedException) {
//                }
//            }
//        }.subscribeOn(Schedulers.computation()).subscribe()
//    }
//
////    fun getPreviousSong(): Song = try {
////        songList[queue[getPreviousIndex()]]
////    } catch (ignore: java.lang.IndexOutOfBoundsException) {
////        Song()
////    }
//
//    private fun requestAudioFocus() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        audioManager.requestAudioFocus(audioFocusRequest)
//    } else {
////        audioManager.requestAudioFocus(
////            this,
////            AudioManager.STREAM_MUSIC,
////            AudioManager.AUDIOFOCUS_GAIN
////        )
//    }
//
//    private fun stop() {
//        player.stop()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            audioManager.abandonAudioFocusRequest(audioFocusRequest)
//        else
//            audioManager.abandonAudioFocus(this)
//        mediaSession.isActive = false
//        setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_STOPPED, 0.0f)
//        stopForeground(true)
//        stopSelf()
//    }
//
//    fun pause() {
//        player.pause()
//        setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PAUSED, 0.0f)
//        startForeground(
//            1,
//            AKX.createNotification(this, mediaSession, R.drawable.ic_play_arrow, repeatMode)
//        )
//        stopForeground(false)
//        stopSelf()
//    }
//
//    fun play() {
//        val result = requestAudioFocus()
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            player.start()
//            mediaSession.isActive = true
//            mediaSession.setMetadata(
//                Song.buildMediaMetaData(
//                    songList[currentPosition],
//                    contentResolver
//                )
//            )
//            setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
//            startForeground(
//                1,
//                AKX.createNotification(this, mediaSession, R.drawable.ic_pause, repeatMode)
//            )
//        }
//    }
//
//    fun playNextSong() {
//        setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
//        currentPosition = getNextIndex()
//        try {
//            prepareMediaPlayer()
//            play()
//        } catch (ignore: IndexOutOfBoundsException) {
//            Log.d(com.example.akxplayer.services.TAG, "playNextSong: ")
//            currentPosition--
//            setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
//        }
//    }
//
//    fun playPreviousSong() {
//        setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
//        currentPosition = getPreviousIndex()
//        try {
//            prepareMediaPlayer()
//            play()
//        } catch (ignore: IndexOutOfBoundsException) {
//            Log.d(com.example.akxplayer.services.TAG, "playPreviousSong: ")
//            currentPosition = 0
//            setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
//        }
//    }
//
//    private fun getNextIndex() = when (repeatMode) {
//        RepeatMode.REPEAT_ONE -> currentPosition
//        RepeatMode.REPEAT_ALL -> (currentPosition + 1) % songList.size
//        else -> currentPosition + 1
//    }
//
//    private fun getPreviousIndex() = when (repeatMode) {
//        RepeatMode.REPEAT_ONE -> currentPosition
//        RepeatMode.REPEAT_ALL -> if (currentPosition - 1 == -1) songList.size - 1 else currentPosition - 1
//        else -> currentPosition - 1
//    }
//
//    fun setRepeatMode() {
//        val repeat = when (repeatMode) {
//            RepeatMode.REPEAT_OFF -> RepeatMode.REPEAT_ALL
//            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
//            else -> RepeatMode.REPEAT_OFF
//        }
//        repeatMode = repeat
//        startForeground(
//            1,
//            AKX.createNotification(baseContext, mediaSession, R.drawable.ic_pause, repeatMode)
//        )
////        stopForeground(false)
//    }
//
//    private fun addToFavorite() {
//
//    }
//
//    fun getCurrentSong() = songList[queue[currentPosition]]
//
//    fun getNextSong() = try {
//        songList[queue[getNextIndex()]]
//    } catch (ignore: IndexOutOfBoundsException) {
//        Song()
//    }
//}