package com.example.akxplayer.ui.viewmodels

import android.app.Application
import android.content.*
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.akxplayer.constants.PlayingState
import com.example.akxplayer.constants.RepeatMode
import com.example.akxplayer.db.entity.QueueEntity
import com.example.akxplayer.model.Song
import com.example.akxplayer.repository.*
import com.example.akxplayer.services.MediaPlayerService
import com.example.akxplayer.ui.activities.MainActivity
import com.example.akxplayer.ui.listeners.OnMediaControlsChange
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.ArrayList

private const val TAG = "MediaViewModel"

class MediaViewModel(application: Application) : AndroidViewModel(application), ServiceConnection,
    OnMediaControlsChange {

    private lateinit var playerService: MediaPlayerService
    private val intent: Intent
    val rootSong = MutableLiveData<Int>()
    val title = MutableLiveData<String>()
    val isPlaying = MutableLiveData<Boolean>()
    val shuffleMode = MutableLiveData<Boolean>()
    val repeatMode = MutableLiveData<RepeatMode>()
    val seekPosition = MutableLiveData<Int>()
    val goToQueue = MutableLiveData<Boolean>()
    val isFavorite = MutableLiveData<Boolean>()

    init {
        SongRepository.init(application)
        QueueRepository.init(application)
        intent = Intent(application, MediaPlayerService::class.java)
        application.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun playPreviousSong() {
        playerService.playPreviousSong()
    }

    fun playOrPauseSong() {
        if (isPlaying.value!!)
            playerService.pause()
        else {
            intent.putParcelableArrayListExtra("queue", null)
            ContextCompat.startForegroundService(getApplication(), intent)
        }
    }

    fun playNextSong() {
        playerService.playNextSong()
    }

    fun setRepeatMode() {
        playerService.setRepeatMode()
    }

    fun setShuffleMode() {
        playerService.setShuffleMode()
    }

    fun addToFavorites() {
        playerService.addRemoveSongFavorite()
    }

    @Synchronized
    fun playMedia(position: Int, songList: List<Song>, title: String): Completable =
        Completable.create {
            this.title.postValue(title)
            Log.d(TAG, "playMedia: ${Thread.currentThread().name}")
            intent.putExtra("position", position)
            intent.putParcelableArrayListExtra("queue", songList as ArrayList<out Parcelable>)
            ContextCompat.startForegroundService(getApplication(), intent)
            it.onComplete()
        }.subscribeOn(Schedulers.computation())

    @Synchronized
    fun playMedia(currentSong: Int): Completable = Completable.create {
        playerService.changeSong(currentSong)
        it.onComplete()
    }.subscribeOn(Schedulers.computation())

    override fun onSongChange(position: Int) {
        rootSong.postValue(position)
    }

    override fun onPlayerStateChanged(playerState: PlayingState) {
        when (playerState) {
            PlayingState.PLAYING -> {
                this.isPlaying.postValue(true)
            }
            else -> {
                this.isPlaying.postValue(false)
            }
        }
    }

    override fun onRepeatModeChanged(repeatMode: RepeatMode) {
        this.repeatMode.value = repeatMode
    }

    override fun onAddRemoverSongFavorite(isFavorite: Boolean) {
        this.isFavorite.postValue(isFavorite)
    }

    override fun onShuffleEnabled(shuffleEnabled: Boolean) {
        shuffleMode.postValue(shuffleEnabled)
    }

    override fun onSeekPositionChange(seekPosition: Int) {
        this.seekPosition.postValue(seekPosition)
    }

    fun setSeekPosition(seekPosition: Long) {
        playerService.seekTo(seekPosition.toInt())
        this.seekPosition.postValue(seekPosition.toInt())
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder = p1 as MediaPlayerService.MediaServiceBinder
        playerService = binder.getService()
        playerService.mediaControls = this
        Log.d(TAG, "onServiceConnected: ${playerService.isConnected}")
        if (!playerService.isConnected) {
            fetchQueueDataFromDB()
        } else {
            initControls(playerService.getQueueInfo())
            QueueRepository.getQueueTitle().subscribeOn(Schedulers.io()).subscribe { title ->
                this.title.postValue(title)
            }
        }
    }

    private fun fetchQueueDataFromDB() {
        QueueRepository.loadQueue().subscribeOn(Schedulers.io()).subscribe(object :
            SingleObserver<QueueEntity> {
            override fun onSubscribe(d: Disposable?) {
            }

            override fun onSuccess(queueEntity: QueueEntity) {
                var songList = emptyList<Song>()
                var queue = emptyList<Int>()
                val songIds = SongRepository.getSongIds()
                if (songIds != null) {
                    songList = SongRepository.getSongsForIds(songIds)
                    queue = SongRepository.getSongOrder()
                }
                playerService.initControls(queueEntity, songList, queue)
                initControls(queueEntity)
                title.postValue(queueEntity.title)
            }

            override fun onError(e: Throwable?) {//called first time when Queue table is empty
                val queueEntity = QueueEntity()
                playerService.initControls(queueEntity, emptyList(), emptyList())
                initControls(queueEntity)
                QueueRepository.initializeQueue()
            }
        })

    }

    private fun initControls(queueEntity: QueueEntity) {
        isPlaying.postValue(playerService.isPlaying())
        rootSong.postValue(queueEntity.currentSong)
        repeatMode.postValue(queueEntity.repeatMode)
        shuffleMode.postValue(queueEntity.shuffleEnabled)
        seekPosition.postValue(queueEntity.seekPosition)
    }

    fun showQueue() {
        goToQueue.value = true
    }

    fun getCurrentSong(): Song = playerService.getCurrentSong()
    fun getNextSong(): Song = playerService.getNextSong()
    fun getQueue(): Pair<List<Song>, List<Int>> = Pair(playerService.songList, playerService.queue)

    fun removeSongFromQueue(songId: Long) {
        playerService.removeSongFromQueue(songId)
    }

    override fun onCleared() {
        val queueEntity =
            QueueEntity(
                title = title.value!!,
                shuffleEnabled = shuffleMode.value!!,
                repeatMode = repeatMode.value!!,
                currentSong = rootSong.value!!,
                seekPosition = seekPosition.value!!
            )
        QueueRepository.saveQueue(queueEntity)
            .subscribeOn(Schedulers.io()).subscribe()

        SongRepository.saveSongList(playerService.songList, playerService.queue)
            .subscribeOn(Schedulers.io()).subscribe()
        
        getApplication<Application>().unbindService(this)
        super.onCleared()
    }

    fun addToQueue(songId: Long) {
        SongRepository.getSongForId(songId).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe { song->
                playerService.addToQueue(song)
            }
    }
}