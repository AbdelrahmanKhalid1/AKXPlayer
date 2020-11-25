package com.example.akxplayer.ui.viewmodels

import android.app.Application
import android.content.*
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.akxplayer.constants.PlayingState
import com.example.akxplayer.constants.Repeat
import com.example.akxplayer.constants.Shuffle
import com.example.akxplayer.model.Song
import com.example.akxplayer.receiver.NoisyReceiver
import com.example.akxplayer.repository.*
import com.example.akxplayer.services.MediaPlayerService
import com.example.akxplayer.ui.listeners.OnMediaControlsChange
import com.example.akxplayer.util.MediaSession
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

private const val TAG = "MediaViewModel"

class MediaViewModel(application: Application) : AndroidViewModel(application), ServiceConnection,
    OnMediaControlsChange {

    private lateinit var service: MediaPlayerService
    private val audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest
    private val intent: Intent
    val rootSong = MutableLiveData<Int>()
    val title = MutableLiveData<String>()
    val isPlaying = MutableLiveData<Boolean>()
    val shuffleMode = MutableLiveData<Shuffle>()
    val repeatMode = MutableLiveData<Repeat>()
    val seekPosition = MutableLiveData<Int>()
    val goToQueue = MutableLiveData<Boolean>().apply {
        value = false
    }

    init {
        SongRepository.init(application)
        audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (MediaSession.queue.isEmpty()) {
            MediaSession.init(application.contentResolver, this)
//        QueueRepository.init(application)
        } else {
            MediaSession.init(this)
            rootSong.value = MediaSession.currentPosition
            isPlaying.value = true
            shuffleMode.value = MediaSession.shuffleMode
            repeatMode.value = MediaSession.repeatMode
        }

        intent = Intent(application, MediaPlayerService::class.java)
        application.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun playPreviousSong() {
        MediaSession.playPreviousSong()
    }

    fun playOrPauseSong() {
        if (MediaSession.isPlaying()) {
            MediaSession.pause()
            service.createNotification()
        } else {
            requestAudioFocus()
        }
    }

    fun playNextSong() {
        MediaSession.playNextSong()
    }

    fun setRepeatMode() {
        val repeat = when (repeatMode.value) {
            Repeat.REPEAT_OFF -> Repeat.REPEAT_ONE
            Repeat.REPEAT_ONE -> Repeat.REPEAT_ALL
            else -> Repeat.REPEAT_OFF
        }
        MediaSession.repeatMode = repeat
        repeatMode.value = repeat
    }

    fun setShuffleMode() {
        val shuffle = when (shuffleMode.value) {
            Shuffle.ENABLE -> Shuffle.DISABLE
            else -> Shuffle.ENABLE
        }
        shuffleMode.value = shuffle
        MediaSession.setShuffle(shuffle)
    }

    @Synchronized
    fun playMedia(position: Int, songList: List<Song>, title: String): Completable =
        Completable.create {
            this.title.postValue(title)
            MediaSession.setMediaSession(position, songList)
//            mediaSession.play()
            ContextCompat.startForegroundService(getApplication(), intent)
            it.onComplete()
        }.subscribeOn(Schedulers.computation())

    @Synchronized
    fun playMedia(position: Int): Completable = Completable.create {
        MediaSession.seekTo(position, 0)
        it.onComplete()
    }.subscribeOn(Schedulers.computation())

    override fun onSongChange(position: Int) {
        requestAudioFocus()
        rootSong.postValue(position)
    }

    override fun onPlayerStateChanged(playerState: PlayingState) {
        when (playerState) {
            PlayingState.PLAYING -> {
                this.isPlaying.postValue(true)
                getApplication<Application>().registerReceiver(NoisyReceiver(), IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            }
            PlayingState.PAUSED -> {
                this.isPlaying.postValue(false)
//                getApplication<Application>().unregisterReceiver(NoisyReceiver())
                service.stopForeground(false)
            }
            PlayingState.STOPPED -> {
                this.isPlaying.postValue(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    audioManager.abandonAudioFocusRequest(audioFocusRequest)
                else
                    audioManager.abandonAudioFocus(service)
                service.mediaSessionCompat.isActive = false
                service.stopForeground(true)
            }
        }
        service.createNotification()
    }

    override fun onSeekPositionChange(seekPosition: Int) {
        this.seekPosition.postValue(seekPosition)
    }

    fun setSeekPosition(seekPosition: Long) {
        MediaSession.seekTo(rootSong.value!!, seekPosition)
        this.seekPosition.postValue(seekPosition.toInt())
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder = p1 as MediaPlayerService.MediaServiceBinder
        service = binder.getService()
    }

    fun shareSong() {
        val uri = Uri.withAppendedPath(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaSession.getCurrentSong().id.toString()
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.type = "audio/*"
        getApplication<Application>().startActivity(
            Intent.createChooser(
                shareIntent,
                "Share Via....."
            )
        )
    }

    fun showQueue() {
        goToQueue.value = true
    }

    fun getCurrentSong(): Song = MediaSession.getCurrentSong()
    fun getNextSong(): Song = MediaSession.getNextSong()
    fun getQueue(): Pair<List<Song>, List<Int>> = Pair(MediaSession.songList, MediaSession.queue)

    private fun requestAudioFocus():Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(service).build()
            audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            audioManager.requestAudioFocus(
                service,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            MediaSession.play()
            service.mediaSessionCompat.isActive = true
            return true
        }
        return false
    }
}