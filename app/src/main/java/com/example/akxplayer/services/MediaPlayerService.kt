package com.example.akxplayer.services

import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver
import com.example.akxplayer.constants.PlayingState
import com.example.akxplayer.constants.RepeatMode
import com.example.akxplayer.db.entity.QueueEntity
import com.example.akxplayer.model.Song
import com.example.akxplayer.notification.AKX
import com.example.akxplayer.receiver.NoisyReceiver
import com.example.akxplayer.repository.QueueRepository.updateCurrentSong
import com.example.akxplayer.repository.QueueRepository.updateRepeatMode
import com.example.akxplayer.repository.QueueRepository.updateSeekPosition
import com.example.akxplayer.repository.SongRepository.addRemoveFavorite
import com.example.akxplayer.repository.SongRepository.isFavorite
import com.example.akxplayer.ui.activities.MainActivity
import com.example.akxplayer.ui.listeners.OnMediaControlsChange
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.FileNotFoundException
import kotlin.random.Random

private const val TAG = "MediaPlayerService"
private const val capabilities: Long =
    PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SEEK_TO

class MediaPlayerService : Service(), AudioManager.OnAudioFocusChangeListener {

    private var isSorted: Boolean = false

    //    private lateinit var mediaServiceHelper: MediaServiceHelper
    lateinit var mediaControls: OnMediaControlsChange
    private val binder: IBinder = MediaServiceBinder(this)
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest
    private var repeatMode = RepeatMode.REPEAT_OFF
    private var shuffleEnabled = false
    private var currentQueueIndex = -1
    private var isFavorite = false
    var songList = emptyList<Song>()
    var queue = emptyList<Int>()
    var isConnected = false
    private lateinit var noisyReceiver: NoisyReceiver
    private var requestAudioResult = -1

    override fun onCreate() {
        super.onCreate()
//        mediaServiceHelper = MediaServiceHelper()
        mediaSession = MediaSessionCompat(this, TAG)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setSessionActivity(
            PendingIntent.getActivity(
                this,
                1,
                Intent(this, MainActivity::class.java),
                0
            )
        )
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPause() {
                pause()
            }

            override fun onPlay() {
                play()
            }

            override fun onStop() {
                stop()
            }

            override fun onSkipToNext() {
                playNextSong()
            }

            override fun onSkipToPrevious() {
                playPreviousSong()
            }

            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                val keyEvent =
                    mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                when (keyEvent?.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_REWIND -> setRepeatMode()
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> addRemoveSongFavorite()
                }
                return super.onMediaButtonEvent(mediaButtonEvent)
            }

            override fun onSeekTo(pos: Long) {
                val position = pos.toInt()
                seekTo(position)
                mediaControls.onSeekPositionChange(position)
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
        noisyReceiver = NoisyReceiver { pause() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.action.equals(Intent.ACTION_MEDIA_BUTTON)) {
                MediaButtonReceiver.handleIntent(mediaSession, intent)
            } else {
                try {
                    songList = intent.getParcelableArrayListExtra("queue")!!
                    currentQueueIndex = intent.getIntExtra("position", -1)
                    Log.d(TAG, "onStartCommand: size=${songList.size} position=$currentQueueIndex")
                    prepareMediaPlayer(songList[currentQueueIndex])
                    prepareQueue()
                } catch (ignore: NullPointerException) {
                    Log.d(TAG, "onStartCommand: Exception")
                }
                play()
                isConnected = true
//                updateQueue()
            }
        }
        return START_STICKY
    }

    private fun prepareMediaPlayer(song: Song) {
        try {
            val uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.id
            )
            val pfd = baseContext.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                val fd = pfd.fileDescriptor
                setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                if (player.isPlaying)
                    player.stop()
                player.reset()
                player.setDataSource(fd)
                player.prepare()
                player.seekTo(0)
                mediaSession.setMetadata(
                    Song.buildMediaMetaData(
                        song,
                        contentResolver
                    )
                )
                mediaControls.onSongChange(currentQueueIndex)
            }
        } catch (ignore: FileNotFoundException) {
            stop()
        } catch (ignore: IllegalStateException){
            Log.e(TAG, "prepareMediaPlayer: ", ignore)
        }
    }

    override fun onAudioFocusChange(focusCHange: Int) {
        when (focusCHange) {
            AudioManager.AUDIOFOCUS_LOSS -> stop()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "onAudioFocusChange: ")
                player.setVolume(0.2f, 0.2f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                play()
                player.setVolume(1.0f, 1.0f)
            }
        }
    }

    private fun requestAudioFocus() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            audioManager.requestAudioFocus(audioFocusRequest)
        else {
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
        mediaSession.isActive = false
        setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_STOPPED, 0.0f)
        stopForeground(true)
        stopSelf()
    }

    fun pause() {
        player.pause()
        setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PAUSED, 0.0f)
        startForeground(
            1,
            AKX.createNotification(
                baseContext,
                mediaSession,
                player.isPlaying,
                isFavorite,
                repeatMode
            )
        )
        mediaControls.onPlayerStateChanged(PlayingState.PAUSED)
        stopForeground(false)
        unregisterReceiver(noisyReceiver)
    }

    fun play() {
        if (requestAudioResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            requestAudioResult = requestAudioFocus()
        player.start()
        mediaSession.isActive = true
        setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
        mediaControls.onPlayerStateChanged(PlayingState.PLAYING)
        startSeeker().subscribe()

        isFavorite(songList[currentQueueIndex].id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isFavorite ->
                this.isFavorite = isFavorite
                mediaControls.onAddRemoverSongFavorite(isFavorite)
                startForeground(
                    1,
                    AKX.createNotification(
                        baseContext,
                        mediaSession,
                        player.isPlaying,
                        isFavorite,
                        repeatMode
                    )
                )
            }
        val intent = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, intent)
    }

    fun playNextSong() {
        try {
            currentQueueIndex = getNextIndex()
            setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
            mediaControls.onPlayerStateChanged(PlayingState.PAUSED)
            prepareMediaPlayer(songList[queue[currentQueueIndex]])
            play()
        } catch (ignore: IndexOutOfBoundsException) {//end of queue
            Log.d(TAG, "playNextSong: ")
            mediaControls.onPlayerStateChanged(PlayingState.PLAYING)
            setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
            currentQueueIndex--
        }
    }

    fun playPreviousSong() {
        try {
            currentQueueIndex = getPreviousIndex()
            setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
            mediaControls.onPlayerStateChanged(PlayingState.PAUSED)
            prepareMediaPlayer(songList[queue[currentQueueIndex]])
            play()
        } catch (ignore: IndexOutOfBoundsException) {
            Log.d(TAG, "playPreviousSong: ")
            mediaControls.onPlayerStateChanged(PlayingState.PLAYING)
            setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
            currentQueueIndex = 0
        }
    }

    private fun getNextIndex() = when (repeatMode) {
        RepeatMode.REPEAT_ONE -> currentQueueIndex
        RepeatMode.REPEAT_ALL -> (currentQueueIndex + 1) % songList.size
        else -> {
            val tempIndex = currentQueueIndex + 1
            if (tempIndex >= songList.size) {
                throw IndexOutOfBoundsException("index out of range")
            }
            tempIndex
        }
    }

    private fun getPreviousIndex() = when (repeatMode) {
        RepeatMode.REPEAT_ONE -> currentQueueIndex
        RepeatMode.REPEAT_ALL -> if (currentQueueIndex - 1 == -1) songList.size - 1 else currentQueueIndex - 1
        else -> {
            val tempIndex = currentQueueIndex - 1
            if (tempIndex <= -1) {
                throw IndexOutOfBoundsException("index out of range")
            }
            tempIndex
        }
    }

    fun setRepeatMode() {
        val repeat = when (repeatMode) {
            RepeatMode.REPEAT_OFF -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            else -> RepeatMode.REPEAT_OFF
        }
        repeatMode = repeat
        mediaControls.onRepeatModeChanged(repeatMode)
        startForeground(
            1,
            AKX.createNotification(
                baseContext,
                mediaSession,
                player.isPlaying,
                isFavorite,
                repeatMode
            )
        )
    }

    fun addRemoveSongFavorite() {
        addRemoveFavorite(songList[queue[currentQueueIndex]].id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe { isFavorite ->
                this.isFavorite = isFavorite
                mediaControls.onAddRemoverSongFavorite(isFavorite)
                startForeground(
                    1,
                    AKX.createNotification(
                        baseContext,
                        mediaSession,
                        player.isPlaying,
                        isFavorite,
                        repeatMode
                    )
                )
            }
    }

    fun getCurrentSong() = songList[queue[currentQueueIndex]]

    fun getNextSong() = try {
        songList[queue[getNextIndex()]]
    } catch (ignore: IndexOutOfBoundsException) {
        Song()
    }

    private fun setMediaPlaybackStateWithActions(state: Int, playbackSpeed: Float) {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    state,
                    player.currentPosition.toLong(),
                    playbackSpeed
                )
                .setActions(capabilities)
                .build()
        )
    }

    private fun setMediaPlaybackState(state: Int) {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    state,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    0.0f
                )
                .build()
        )
    }

    fun seekTo(position: Int) {
        player.seekTo(position)
        setMediaPlaybackStateWithActions(PlaybackStateCompat.STATE_PLAYING, 1.0f)
    }

    fun changeSong(newSong: Int) {
        currentQueueIndex = newSong
        prepareMediaPlayer(songList[queue[currentQueueIndex]])
        play()
    }

    fun setShuffleMode() {
        shuffleEnabled = !shuffleEnabled
        prepareQueue()
    }

    private fun prepareQueue() {
        Completable.create {
            if (queue.isEmpty() || queue.size != songList.size)
                initQueue()
            val queueArrayList: ArrayList<Int>
            if (shuffleEnabled) {
                queueArrayList = queue as ArrayList<Int>
                for (i in songList.size - 1 downTo 1) {
                    val randomIndexToSwap = Random.nextInt(i + 1)

                    if (randomIndexToSwap == currentQueueIndex)
                        currentQueueIndex = i
                    else if (i == currentQueueIndex)
                        currentQueueIndex = randomIndexToSwap

                    val temp = queueArrayList[randomIndexToSwap]
                    queueArrayList[randomIndexToSwap] = queueArrayList[i]
                    queueArrayList[i] = temp
                }
                isSorted = false
            } else if (!shuffleEnabled && !isSorted) {
                queueArrayList = ArrayList()
                currentQueueIndex = queue[currentQueueIndex]
                for (i in songList.indices) {
                    queueArrayList.add(i)
                }
                queue = queueArrayList
                isSorted = true
            }
            it.onComplete()
        }.subscribeOn(Schedulers.single()).subscribe {
            mediaControls.onSongChange(currentQueueIndex)
            mediaControls.onShuffleEnabled(shuffleEnabled)
        }
    }

    private fun initQueue() {
        val queueArrayList = ArrayList<Int>()
        for (i in songList.indices) {
            queueArrayList.add(i)
        }
        isSorted = true
        queue = queueArrayList
    }

    fun isPlaying() = player.isPlaying

    fun getQueueInfo() = QueueEntity(
        currentSong = currentQueueIndex,
        repeatMode = this.repeatMode,
        shuffleEnabled = shuffleEnabled,
        seekPosition = player.currentPosition
    )

    fun initControls(queueEntity: QueueEntity, songList: List<Song>, queue: List<Int>) {
        this.currentQueueIndex = queueEntity.currentSong
        this.repeatMode = queueEntity.repeatMode
        this.shuffleEnabled = queueEntity.shuffleEnabled
        this.songList = songList
        this.queue = queue
        if (currentQueueIndex != -1 && queue.isNotEmpty() && songList.isNotEmpty()) {
            prepareMediaPlayer(songList[queue[currentQueueIndex]])
            player.seekTo(queueEntity.seekPosition)
            isFavorite(songList[queue[currentQueueIndex]].id).subscribeOn(Schedulers.io())
                .subscribe { isFavorite ->
                    this.isFavorite = isFavorite
                    mediaControls.onAddRemoverSongFavorite(isFavorite)
                }
        }
    }

    private fun startSeeker(): Completable = Completable.create {
        while (player.isPlaying) {
            mediaControls.onSeekPositionChange(player.currentPosition)
        }
        if (player.currentPosition >= player.duration)
            playNextSong()
    }.subscribeOn(Schedulers.computation())

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onDestroy() {
        updateSeekPosition(player.currentPosition.toLong())
        updateCurrentSong(currentQueueIndex)
        updateRepeatMode(repeatMode)
        player.release()
        mediaSession.release()
        isConnected = false
        super.onDestroy()
    }

    fun removeSongFromQueue(deletedSongId: Long) {
        val newSongList = ArrayList<Song>()
        val newQueue = ArrayList<Int>()
        var deletedPosition: Int = -1
        for (index in queue.indices) {
            if (songList[queue[index]].id == deletedSongId) {
                deletedPosition = queue[index] //real position of song in songList
                break
            }
        }

        for (index in queue.indices) {
            if (songList[queue[index]].id == deletedSongId) {
                continue
            }

            var position = queue[index]
            if (position >= deletedPosition) {
                position--
            }
            newQueue.add(position)
            newSongList.add(songList[queue[index]])
        }
        songList = newSongList
        queue = newQueue

    }

    fun addToQueue(song: Song) {
        (songList as ArrayList).add(song)
        (queue as ArrayList).add(queue.size)
    }

    class MediaServiceBinder(private val mediaPlayerService: MediaPlayerService) : Binder() {
        fun getService(): MediaPlayerService {
            return mediaPlayerService
        }
    }
}