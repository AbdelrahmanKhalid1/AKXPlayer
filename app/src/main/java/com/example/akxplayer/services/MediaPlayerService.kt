package com.example.akxplayer.services

import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.session.MediaButtonReceiver
import com.example.akxplayer.R
import com.example.akxplayer.model.Song
import com.example.akxplayer.notification.AKX
import java.io.FileNotFoundException
import java.util.*

private const val TAG = "MediaPlayerService"

class MediaPlayerService : Service(), AudioManager.OnAudioFocusChangeListener {

    private val binder: IBinder = MediaServiceBinder(this)
    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var player: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest
    private var currentPosition = -1
    private var songList = emptyList<Song>()
    private var queue = emptyList<Int>()

    //    var repeatMode = Repeat.REPEAT_OFF
//    var shuffleMode = Shuffle.DISABLE

    override fun onCreate() {
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(this, TAG)
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSessionCompat.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPause() {
                pause()
            }

            override fun onPlay() {
                play()
            }

            override fun onSkipToNext() {
                Log.d(TAG, "onSkipToNext: ")
                setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
            }

            override fun onSkipToPrevious() {
                Log.d(TAG, "onSkipToPrevious: ")
                setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
            }
        })

        player = MediaPlayer()
        audioManager = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this).build()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val result = requestAudioFocus()
            if (intent.action.equals(Intent.ACTION_MEDIA_BUTTON)) {
                Log.d(TAG, "onStartCommand: ")
                MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
            } else {
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    songList = intent.getParcelableArrayListExtra("queue")!!
                    currentPosition = intent.getIntExtra("position", -1)

                    prepareMediaPlayer()
                    play()
                }
            }
        }
        return START_STICKY
    }

    private fun prepareMediaPlayer() {
        try {
            val uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songList[currentPosition].id
            )
            val pfd = baseContext.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                val fd = pfd.fileDescriptor
                setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                player.reset()
                player.setDataSource(fd)
                player.prepare()
                player.seekTo(0)
//                mediaPlayer.setOnCompletionListener {
//                    playNextSong()
//                }
            }
        } catch (ignore: FileNotFoundException) {
            stop()
        }
    }

    override fun onAudioFocusChange(focusCHange: Int) {
        when (focusCHange) {
            AudioManager.AUDIOFOCUS_LOSS -> stop()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                play()
                val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                Log.d(TAG, "onAudioFocusChange: $volume")
                player.setVolume(volume, volume)
            }
        }
    }

    private fun requestAudioFocus() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        audioManager.requestAudioFocus(audioFocusRequest)
    } else {
        audioManager.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    private fun stop() {
        player.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        else
            audioManager.abandonAudioFocus(this)
        mediaSessionCompat.isActive = false
        stopForeground(true)
    }

    private fun pause() {
        player.pause()
        setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PAUSED, 0.0f)
        startForeground(1, AKX.createNotification(this, mediaSessionCompat, R.drawable.ic_pause))
        stopForeground(false)
    }

    private fun play() {
        player.start()
        mediaSessionCompat.isActive = true
        mediaSessionCompat.setMetadata(
            Song.buildMediaMetaData(
                songList[currentPosition],
                contentResolver
            )
        )
        setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
        startForeground(
            1,
            AKX.createNotification(this, mediaSessionCompat, R.drawable.ic_play_arrow)
        )
    }

    private fun setMediaPlaybackStateWithActions(state: Int, playbackSpeed: Float) {
        mediaSessionCompat.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    state,
                    player.currentPosition.toLong(),
                    playbackSpeed,
                    Calendar.getInstance().timeInMillis
                )
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build()
        )
    }
    private fun setMediaPlaybackState(state: Int){
        mediaSessionCompat.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    state,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    0.0f,
                    Calendar.getInstance().timeInMillis
                )
                .build()
        )
    }

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        mediaSessionCompat.release()
    }

    class MediaServiceBinder(private val mediaPlayerService: MediaPlayerService) : Binder() {
        fun getService(): MediaPlayerService {
            return mediaPlayerService
        }
    }
}