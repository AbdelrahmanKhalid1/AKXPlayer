package com.example.akxplayer.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.multidex.MultiDexApplication
import com.example.akxplayer.R
import com.example.akxplayer.constants.RepeatMode

const val CHANNEL_ID = "akxPlayerChannel"

class AKX : MultiDexApplication() {

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
            isPlaying: Boolean,
            isFavorite: Boolean,
            repeatMode: RepeatMode
        ): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_headset)
            .setContentTitle(mediaSessionCompat.controller.metadata.description.title)
            .setContentText(mediaSessionCompat.controller.metadata.description.subtitle)
//            .setSubText(mediaSessionCompat.controller.metadata.description.description)
            .setLargeIcon(
                mediaSessionCompat.controller.metadata.description.iconBitmap
            )
            .addAction(
                getRepeatModeIcon(repeatMode),
                "repeat",
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_REWIND)
            )
            .addAction(
                R.drawable.ic_previous,
                "previous",
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            )
            .addAction(
                getPlayingIcon(isPlaying),
                "play",
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            )
            .addAction(
                R.drawable.ic_next,
                "next",
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_NEXT)
            )
            .addAction(
                getFavoriteIcon(isFavorite),
                "like",
                getActionIntent(context, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
                    .setShowCancelButton(true) //for api < lollipop
                    .setCancelButtonIntent(getActionIntent(context, KeyEvent.KEYCODE_MEDIA_STOP))
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(mediaSessionCompat.controller.sessionActivity)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setDeleteIntent(getActionIntent(context, KeyEvent.KEYCODE_MEDIA_STOP))
            .build()

        private fun getActionIntent(context: Context, mediaKeyEvent: Int): PendingIntent {
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
            intent.setPackage(context.packageName)
            intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyEvent))
            return PendingIntent.getBroadcast(context, mediaKeyEvent, intent, 0)
        }

        private fun getRepeatModeIcon(repeatMode: RepeatMode) = when (repeatMode) {
            RepeatMode.REPEAT_OFF -> R.drawable.ic_repeat
            RepeatMode.REPEAT_ALL -> R.drawable.ic_repeat_active
            RepeatMode.REPEAT_ONE -> R.drawable.ic_repeat_one_active
        }

        private fun getFavoriteIcon(isFavorite: Boolean) =
            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_not_favorite

        private fun getPlayingIcon(isPlaying: Boolean) =
            if(isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
    }
}