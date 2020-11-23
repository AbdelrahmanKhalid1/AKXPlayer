package com.example.akxplayer.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.akxplayer.R
import com.example.akxplayer.model.Song
import com.example.akxplayer.util.MediaSession

const val CHANNEL_ID = "akxPlayerChannel"

class AKX : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "Media Player Channel",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(notificationChannel)
    }

    companion object {
        fun createNotification(
            context: Context,
            mediaSessionCompat: MediaSessionCompat,
            pendingIntent: PendingIntent,
            song: Song
        ): NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_headset)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setLargeIcon(
                com.example.akxplayer.util.Util.getPic(
                    song.albumId,
                    context.contentResolver
                )
            )
            .addAction(R.drawable.ic_repeat, "like", null)
            .addAction(R.drawable.ic_previous, "previous", null)
            .addAction(
                if (MediaSession.isPlaying()) R.drawable.ic_pause else R.drawable.ic_play_arrow,
                "play",
                null
            )
            .addAction(R.drawable.ic_next, "next", null)
            .addAction(R.drawable.ic_not_favorite, "like", null)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1, 2, 3)
                    .setMediaSession(mediaSessionCompat.sessionToken)
            )
            .setSubText("Sub Text")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
    }
}