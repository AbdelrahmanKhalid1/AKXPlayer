package com.example.akxplayer.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
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
            play_pause_icon: Int
        ): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_headset)
            .setContentTitle(mediaSessionCompat.controller.metadata.description.title)
            .setContentText(mediaSessionCompat.controller.metadata.description.subtitle)
//            .setSubText(mediaSessionCompat.controller.metadata.description.description)
            .setLargeIcon(
                mediaSessionCompat.controller.metadata.description.iconBitmap
            )
            .addAction(R.drawable.ic_repeat, "repeat", null)
            .addAction(
                R.drawable.ic_previous,
                "previous",
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            )
            .addAction(
                play_pause_icon,
                "play",
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            )
            .addAction(R.drawable.ic_next, "next", getActionIntent(context, KeyEvent.KEYCODE_MEDIA_NEXT))
            .addAction(R.drawable.ic_not_favorite, "like", null)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1, 2, 3)
                    .setMediaSession(mediaSessionCompat.sessionToken)
                    .setShowCancelButton(true) //for api < lollipop
//                    .setCancelButtonIntent(MediaStyle)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(mediaSessionCompat.controller.sessionActivity)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .build()

        private fun getActionIntent(context: Context, mediaKeyEvent: Int): PendingIntent {
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
            intent.setPackage(context.packageName)
            intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyEvent))
            return PendingIntent.getBroadcast(context, mediaKeyEvent, intent, 0)
        }
    }
}