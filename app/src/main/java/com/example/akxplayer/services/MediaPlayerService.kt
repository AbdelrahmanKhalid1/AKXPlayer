package com.example.akxplayer.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.session.PlaybackState
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import com.example.akxplayer.notification.AKX
import com.example.akxplayer.ui.activities.MainActivity
import com.example.akxplayer.util.MediaSession

private const val TAG = "MediaPlayerService"

class MediaPlayerService : Service(), AudioManager.OnAudioFocusChangeListener {

    lateinit var mediaSessionCompat: MediaSessionCompat
    private val binder: IBinder = MediaServiceBinder(this)
    override fun onCreate() {
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(this, TAG)
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSessionCompat.setCallback(object : MediaSessionCompat.Callback(){

        })

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            MediaSession.play()
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
            createNotification()
        }
        return START_STICKY
    }

    fun createNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = AKX.createNotification(
            this,
            mediaSessionCompat,
            pendingIntent,
            MediaSession.getCurrentSong()
        ).build()

        mediaSessionCompat.isActive = MediaSession.isPlaying()
//        val state = if(MediaSession.isPlaying()) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
//        mediaSessionCompat.setPlaybackState(PlaybackStateCompat.Builder().setState(state, 0, ))
        startForeground(1, notification)
//        if (MediaSession.isPlaying())
//            startForeground(1, notification)
//        else
        //TODO create notification without starting service
    }

    override fun onAudioFocusChange(focusCHange: Int) {
        when (focusCHange) {
            AudioManager.AUDIOFOCUS_LOSS -> MediaSession.stop()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> MediaSession.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> MediaSession.lowerVolume()
            AudioManager.AUDIOFOCUS_GAIN -> {
                MediaSession.play()
                MediaSession.higherVolume()
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionCompat.release()
        MediaSession.release()
    }

    class MediaServiceBinder(private val mediaPlayerService: MediaPlayerService) : Binder() {
        fun getService(): MediaPlayerService {
            return mediaPlayerService
        }
    }
}