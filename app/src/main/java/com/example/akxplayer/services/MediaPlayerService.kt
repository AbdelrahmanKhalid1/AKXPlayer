package com.example.akxplayer.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.example.akxplayer.notification.AKX
import com.example.akxplayer.ui.activities.MainActivity
import com.example.akxplayer.util.MediaSession

private const val TAG = "MediaPlayerService"

class MediaPlayerService : Service() {

    private lateinit var mediaSessionCompat: MediaSessionCompat
    private val binder: IBinder = MediaServiceBinder(this)
    override fun onCreate() {
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(this, "tag")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            MediaSession.play()
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
        startForeground(1, notification)
//        if (MediaSession.isPlaying())
//            startForeground(1, notification)
//        else
            //TODO create notification without starting service
    }

    override fun onBind(p0: Intent?): IBinder? = binder

    class MediaServiceBinder(private val mediaPlayerService: MediaPlayerService) : Binder() {
        fun getService(): MediaPlayerService {
            return mediaPlayerService
        }
    }
}