package com.example.akxplayer.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.example.akxplayer.R
import com.example.akxplayer.db.entity.QueueEntity
import com.example.akxplayer.model.Song
import com.example.akxplayer.ui.activities.MainActivity
import com.example.akxplayer.ui.listeners.MediaControls
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import kotlin.collections.ArrayList

private const val TAG = "MediaPlayerService"
const val CHANNEL_ID = "akxPlayerChannel"

class MediaPlayerService : Service() {

    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var dataSourceFactory: DefaultDataSourceFactory
    private lateinit var concatenatingMediaSource: ConcatenatingMediaSource
    private lateinit var player: SimpleExoPlayer
    private var queue: List<Song> = ArrayList()
    private val binder = MediaServiceBinder(this)
    private lateinit var timeLineQueueNavigator: TimelineQueueNavigator
    var isConnected = false

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "Media Session")
        mediaSession.isActive = true

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this,
            CHANNEL_ID,
            R.string.channel_name,
            1,
            mediaDescriptionAdapter
        )
        playerNotificationManager.setNotificationListener(notificationListener)
        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession)

        timeLineQueueNavigator = object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(windowIndex: Int): MediaDescriptionCompat {
                return Song.buildMediaDescription(queue[windowIndex], contentResolver)
            }
        }

        dataSourceFactory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )

        concatenatingMediaSource = ConcatenatingMediaSource()

        player = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
        player.addListener(eventListener)

        mediaSessionConnector.setQueueNavigator(timeLineQueueNavigator)
        mediaSessionConnector.setPlayer(player, null)
        playerNotificationManager.setPlayer(player)
    }

    private val mediaDescriptionAdapter =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun createCurrentContentIntent(player: Player?): PendingIntent? {
                val intent = Intent(baseContext, MainActivity::class.java)
                return PendingIntent.getActivity(
                    baseContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            override fun getCurrentContentText(player: Player?): String {
                return try {
                    queue[player!!.currentWindowIndex].artist
                } catch (ignore: IndexOutOfBoundsException) {
                    ""
                }
            }

            override fun getCurrentContentTitle(player: Player?): String {
                return try {
                    queue[player!!.currentWindowIndex].title
                } catch (ignore: IndexOutOfBoundsException) {
                    ""
                }
            }

            override fun getCurrentLargeIcon(
                player: Player?,
                callback: PlayerNotificationManager.BitmapCallback?
            ): Bitmap? {
                return try {
                    com.example.akxplayer.util.Util.getPic(
                        queue[player!!.currentWindowIndex].albumId,
                        contentResolver
                    )
                } catch (ignore: IndexOutOfBoundsException) {
                    null
                }
            }
        }

    private val notificationListener = object : PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int) {
            isConnected = false
            stopForeground(false)
        }

        override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
            startForeground(notificationId, notification)
        }
    }

    private val eventListener = object : Player.DefaultEventListener() {

        override fun onSeekProcessed() {
            super.onSeekProcessed()
            binder.mediaControls.onSeekPositionChange(player.currentPosition)
        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray?,
            trackSelections: TrackSelectionArray?
        ) {
            super.onTracksChanged(trackGroups, trackSelections)
            binder.mediaControls.onSongChange(player.currentWindowIndex)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            when (playbackState) {
                Player.STATE_ENDED -> {
                    Log.d(TAG, "onPlayerStateChanged: $playWhenReady")
                    player.playWhenReady = false
                    player.seekToDefaultPosition(0)
                }
            }

            binder.mediaControls.isPlaying(playWhenReady)
            stopService()
            initSeekPosition()
        }
    }

    fun stopService() {
        if (!isConnected && !player.playWhenReady) {
            stopForeground(false)
            stopSelf()
        }
    }

    private fun initSeekPosition() {
        Single.create<Long> { emitter ->
            while (player.playWhenReady) {
                try {
                    emitter.onSuccess(player.currentPosition)
                    binder.mediaControls.onSeekPositionChange(player.currentPosition)
                    Thread.sleep(1000)
                } catch (ignore: InterruptedException) {
                }
            }
        }.subscribeOn(Schedulers.single()).subscribe()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            queue = intent!!.getParcelableArrayListExtra("queue")!!
            val position = intent.getIntExtra("position", 0)
            setPlayer(position, 0)
            player.playWhenReady = true
            isConnected = true
        } catch (ignore: NullPointerException) {
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun setPlayer(position: Int, seekPosition: Long) {
        concatenatingMediaSource = ConcatenatingMediaSource()
        for (song in queue) {
            val uri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
            val mediaSource =
                ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        player.prepare(concatenatingMediaSource)
        player.seekTo(position, seekPosition)
    }

    fun setPlayerFromDatabase(queueEntity: QueueEntity, queue: List<Song>) {
        this.queue = queue
        player.shuffleModeEnabled = queueEntity.shuffleMode
        player.repeatMode = queueEntity.repeatMode
        setPlayer(queueEntity.currentSong, queueEntity.seekPosition)
    }

    fun addToQueue(song: Song) {
        val songUri =
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
        concatenatingMediaSource.addMediaSource(
            ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(songUri)
        )
    }

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onDestroy() {
        player.stop()
        mediaSession.release()
        mediaSessionConnector.setPlayer(null, null)
        playerNotificationManager.setPlayer(null)
        player.release()
        Log.d(TAG, "onDestroy: Music Player")
        super.onDestroy()
    }

    class MediaServiceBinder(private val mediaPlayerService: MediaPlayerService) : Binder() {
        lateinit var mediaControls: MediaControls
        fun getService(): MediaPlayerService {
            return mediaPlayerService
        }

        fun getPlayer(): SimpleExoPlayer = mediaPlayerService.player
    }
}