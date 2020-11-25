package com.example.akxplayer.util

import android.content.ContentResolver
import android.content.ContentUris
import android.media.MediaPlayer
import android.provider.MediaStore
import com.example.akxplayer.constants.PlayingState
import com.example.akxplayer.constants.Repeat
import com.example.akxplayer.constants.Shuffle
import com.example.akxplayer.model.Song
import com.example.akxplayer.ui.listeners.OnMediaControlsChange
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.FileNotFoundException

private const val TAG = "MediaSession"

object MediaSession {

    private lateinit var contentResolver: ContentResolver
    private lateinit var onControlsChange: OnMediaControlsChange
    private lateinit var mediaPlayer: MediaPlayer
    var repeatMode = Repeat.REPEAT_OFF
    var shuffleMode = Shuffle.DISABLE
    var currentPosition = -1
    var songList = emptyList<Song>()
    var queue = emptyList<Int>()


    fun init(contentResolver: ContentResolver, onControlsChange: OnMediaControlsChange) {
        this.contentResolver = contentResolver
        this.onControlsChange = onControlsChange
    }

    fun init(onControlsChange: OnMediaControlsChange){
        this.onControlsChange = onControlsChange
    }

    fun setMediaSession(
        position: Int,
        songList: List<Song>
    ): Boolean {
        currentPosition = position
        this.songList = songList
        setQueue()
        setMediaPlayer()
        return true
    }

    private fun setQueue() {
        val newQueue = ArrayList<Int>()
        if (shuffleMode == Shuffle.DISABLE) {
            for (i in songList.indices)
                newQueue.add(i)
        } else {
            //TODO
        }
        queue = newQueue
    }

    private fun setMediaPlayer() {
        try {
            mediaPlayer.stop()
        } catch (ignore: UninitializedPropertyAccessException) {

        }

        try {
            val uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                getCurrentSong().id
            )
            val pfd = contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                val fd = pfd.fileDescriptor
                mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(fd)
                mediaPlayer.prepare()
                mediaPlayer.seekTo(0)

                mediaPlayer.setOnCompletionListener {
                    playNextSong()
                }
                onControlsChange.onSongChange(currentPosition)
            }
        } catch (ignore: FileNotFoundException) {
            onControlsChange.onPlayerStateChanged(PlayingState.STOPPED)
        }
    }

    fun seekTo(position: Int, seekPosition: Long) {
        if (position == currentPosition) {
            mediaPlayer.seekTo(seekPosition.toInt())
            return
        }
        currentPosition = position
        setMediaPlayer()
    }

    fun isPlaying(): Boolean = mediaPlayer.isPlaying

    fun playPreviousSong() {
        currentPosition = getPreviousIndex()
        try {
            setMediaPlayer()
        } catch (ignore: IndexOutOfBoundsException) {
            currentPosition += 1
        }
    }

    fun play() {
        mediaPlayer.start()
        initSeekPosition()
        onControlsChange.onPlayerStateChanged(PlayingState.PLAYING)
    }

    fun pause() {
        mediaPlayer.pause()
        onControlsChange.onPlayerStateChanged(PlayingState.PAUSED)
    }

    fun stop() {
        mediaPlayer.stop()
        onControlsChange.onPlayerStateChanged(PlayingState.STOPPED)
    }

    fun playNextSong() {
        currentPosition = getNextIndex()
        try {
            setMediaPlayer()
        } catch (ignore: IndexOutOfBoundsException) {
            currentPosition -= 1
        }
    }

    fun setShuffle(shuffleMode: Shuffle) {
        this.shuffleMode = shuffleMode
        setQueue()
    }

    private fun initSeekPosition() {
        Single.create<Int> {
            while (mediaPlayer.isPlaying) {
                try {
                    onControlsChange.onSeekPositionChange(mediaPlayer.currentPosition)
                } catch (ignore: InterruptedException) {
                }
            }
        }.subscribeOn(Schedulers.computation()).subscribe()
    }

//    fun getPreviousSong(): Song = try {
//        songList[queue[getPreviousIndex()]]
//    } catch (ignore: java.lang.IndexOutOfBoundsException) {
//        Song()
//    }

    fun getCurrentSong(): Song = songList[queue[currentPosition]]

    fun getNextSong(): Song = try {
        songList[queue[getNextIndex()]]
    } catch (ignore: IndexOutOfBoundsException) {
        Song()
    }

    private fun getNextIndex(): Int = when (repeatMode) {
        Repeat.REPEAT_ONE -> currentPosition
        Repeat.REPEAT_ALL -> (currentPosition + 1) % songList.size
        else -> currentPosition + 1
    }

    private fun getPreviousIndex(): Int = when (repeatMode) {
        Repeat.REPEAT_ONE -> currentPosition
        Repeat.REPEAT_ALL -> if (currentPosition - 1 == -1) songList.size - 1 else currentPosition - 1
        else -> currentPosition - 1
    }

    fun lowerVolume() {
        mediaPlayer.setVolume(0.2f, 0.2f)
    }

    fun higherVolume(){
        mediaPlayer.setVolume(1f, 1f)
    }

    fun release() {
        mediaPlayer.release()
    }

}