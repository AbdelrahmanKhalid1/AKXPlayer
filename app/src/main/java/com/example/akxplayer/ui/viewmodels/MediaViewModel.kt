package com.example.akxplayer.ui.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.akxplayer.db.entity.QueueEntity
import com.example.akxplayer.model.Song
import com.example.akxplayer.repository.*
import com.example.akxplayer.services.MediaPlayerService
import com.example.akxplayer.ui.listeners.MediaControls
import com.google.android.exoplayer2.IllegalSeekPositionException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

private const val TAG = "MediaViewModel"

class MediaViewModel(application: Application) : AndroidViewModel(application), ServiceConnection,
    MediaControls {

    private val intent: Intent
    private lateinit var player: SimpleExoPlayer
    private lateinit var service: MediaPlayerService
    var queue = emptyList<Song>()
    val rootSong = MutableLiveData<Int>()
    val seekPosition = MutableLiveData<Long>()
    val repeatMode = MutableLiveData<Int>()
    val shuffleMode = MutableLiveData<Boolean>()
    val title = MutableLiveData<String>()
    val showQueue = MutableLiveData<Char>()
    val isPlaying = MutableLiveData<Boolean>().apply {
        value = false
    }

    init {
        SongRepository.init(application)
        QueueRepository.init(application)
        intent = Intent(application, MediaPlayerService::class.java)
        application.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    @Synchronized
    fun playMedia(position: Int, songList: List<Song>, title: String): Completable =
        Completable.create {
            if (title == this.title.value) {
                player.seekTo(position, 0)
                player.playWhenReady = true
            } else {
                queue = songList
                this.title.postValue(title)
                intent.putParcelableArrayListExtra(
                    "queue",
                    queue as java.util.ArrayList<out Parcelable>
                )
                intent.putExtra("position", position)
                Util.startForegroundService(getApplication(), intent)
                SongRepository.updateSongs(songList).subscribe()
                QueueRepository.updateTitle(title).subscribe()
            }
            it.onComplete()
        }.subscribeOn(Schedulers.computation())

    @Synchronized
    fun playMedia(position: Int): Completable = Completable.create {
        player.seekTo(position, 0)
        it.onComplete()
    }.subscribeOn(Schedulers.computation())

    fun playOrPauseSong() {
        if (!service.isConnected) {
            intent.putParcelableArrayListExtra(
                "queue",
                queue as java.util.ArrayList<out Parcelable>
            )
            intent.putExtra("position", 0)
            Util.startForegroundService(getApplication(), intent)
        }

        player.playWhenReady = !player.playWhenReady
    }

    fun previousSong() {
        Completable.create {
            try {
                player.seekTo(player.previousWindowIndex, 0)
            } catch (ignore: IllegalSeekPositionException) {

            }
            it.onComplete()
        }.subscribeOn(Schedulers.computation()).subscribe()
    }

    fun nextSong() {
        Completable.create {
            try {
                player.seekTo(player.nextWindowIndex, 0)
            } catch (ignore: IllegalSeekPositionException) {

            }
            it.onComplete()
        }.subscribeOn(Schedulers.computation()).subscribe()
    }

    fun setShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
        shuffleMode.value = player.shuffleModeEnabled
        QueueRepository.updateShuffleMode(player.shuffleModeEnabled).subscribe()
    }

    fun setRepeatMode() {
        when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                repeatMode.value = Player.REPEAT_MODE_ALL
            }
            Player.REPEAT_MODE_ALL -> {
                player.repeatMode = Player.REPEAT_MODE_ONE
                repeatMode.value = Player.REPEAT_MODE_ONE
            }
            Player.REPEAT_MODE_ONE -> {
                player.repeatMode = Player.REPEAT_MODE_OFF
                repeatMode.value = Player.REPEAT_MODE_OFF
            }
        }
        QueueRepository.updateRepeatMode(player.repeatMode).subscribe()
    }

    fun shareSong() {
        val path2 = Uri.withAppendedPath(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            queue[player.currentWindowIndex].id.toString()
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, path2)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.type = "audio/*"
        getApplication<Application>().startActivity(
            Intent.createChooser(
                shareIntent,
                "Share Via....."
            )
        )
    }

    fun addToFavorites(view: View) {
        //TODO no yet implemented
        Snackbar.make(view, "Added Successfully", Snackbar.LENGTH_SHORT).show()
    }

    fun showQueue() {
        showQueue.value = 'o'
    }

    fun addToQueue(songId: Long) {
        SongRepository.getSongForId(songId).subscribeOn(Schedulers.io()).subscribe { song ->
            (queue as ArrayList).add(song)
            service.addToQueue(song)
            rootSong.postValue(rootSong.value)
        }
    }

    fun getNextSong(): Song = queue[player.nextWindowIndex]

    fun setSeekPosition(progress: Long) {
        player.seekTo(player.currentWindowIndex, progress)
    }

    override fun onSongChange(songIndex: Int) {
        rootSong.postValue(songIndex)
        QueueRepository.updateCurrentSong(player.currentWindowIndex).subscribe()
    }

    override fun onSeekPositionChange(seekPosition: Long) {
        this.seekPosition.postValue(seekPosition)
        QueueRepository.updateSeekPosition(seekPosition).subscribe()
    }

    override fun onQueueEnds() {

    }

    override fun isPlaying(isPlaying: Boolean) {
        this.isPlaying.postValue(isPlaying)
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder: MediaPlayerService.MediaServiceBinder =
            p1 as MediaPlayerService.MediaServiceBinder
        binder.mediaControls = this
        service = binder.getService()
        player = binder.getPlayer()
        service.isConnected = true
        loadDataFromDatabase()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }

    private fun loadDataFromDatabase() {
        QueueRepository.getQueueInfo().subscribeOn(Schedulers.io())
            .subscribe { pair -> setControls(pair.first, pair.second) }
    }

    private fun setControls(queueEntity: QueueEntity, songIds: LongArray?) {
        title.postValue(queueEntity.title)
        seekPosition.postValue(queueEntity.seekPosition)
        shuffleMode.postValue(queueEntity.shuffleMode)
        repeatMode.postValue(queueEntity.repeatMode)
        isPlaying.postValue(player.playWhenReady)
        if (songIds != null)
            SongRepository.getSongsForIds(songIds)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe { songList ->
                    queue = songList
                    rootSong.postValue(queueEntity.currentSong)
                    if (!player.playWhenReady) {
                        service.setPlayerFromDatabase(queueEntity, queue)
                        Util.startForegroundService(getApplication() as Context, intent)
                    }
                }
    }

    fun getQueue(): Single<Pair<Int, List<Int>>> = Single.create { emitter ->
        val songIndexList = ArrayList<Int>()
        var songPosition = -1
        var index = player.currentTimeline.getFirstWindowIndex(player.shuffleModeEnabled)
        for (i in queue.indices) {
            if (index == player.currentWindowIndex)
                songPosition = i
            songIndexList.add(index)
            index = player.currentTimeline.getNextWindowIndex(
                index,
                player.repeatMode,
                player.shuffleModeEnabled
            )
        }
        emitter.onSuccess(Pair(songPosition, songIndexList))
    }

    override fun onCleared() {
        service.isConnected = false
        service.stopService()
        getApplication<Application>().baseContext.unbindService(this)
        super.onCleared()
    }
}