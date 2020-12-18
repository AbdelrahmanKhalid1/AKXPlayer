package com.example.akxplayer.repository

import android.content.Context
import android.util.Log
import com.example.akxplayer.constants.RepeatMode
import com.example.akxplayer.db.AkxDatabase
import com.example.akxplayer.db.dao.QueueDao
import com.example.akxplayer.db.entity.QueueEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

object QueueRepository {

    private lateinit var queueDao: QueueDao

    fun init(context: Context) {
        queueDao = AkxDatabase.getInstance(context).queueDao()
    }

    fun loadQueue(): Single<QueueEntity> =
        Single.create { emitter ->
            val queueEntity: QueueEntity = queueDao.getQueueData()
            emitter.onSuccess(queueEntity)
        }

    fun initializeQueue() {
        queueDao.initializeTable(QueueEntity())
    }

    fun saveQueue(queueEntity: QueueEntity): Completable = Completable.create { emitter ->
        queueDao.updateQueue(queueEntity)
        emitter.onComplete()
    }.subscribeOn(Schedulers.io())

    fun updateCurrentSong(currentSong: Int): Completable = Completable.create {
        queueDao.updateCurrentSong(currentSong)
    }.subscribeOn(Schedulers.io())

    fun updateRepeatMode(repeatMode: RepeatMode): Completable = Completable.create {
        queueDao.updateRepeatMode(repeatMode)
    }.subscribeOn(Schedulers.io())

    fun updateSeekPosition(seekPosition: Long): Completable = Completable.create {
        queueDao.updateSeekPosition(seekPosition)
    }.subscribeOn(Schedulers.io())

    fun getQueueTitle(): Single<String> = Single.create {
        it.onSuccess(queueDao.getQueueData().title)
    }
}