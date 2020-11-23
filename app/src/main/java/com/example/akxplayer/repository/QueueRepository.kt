package com.example.akxplayer.repository
//
//import android.content.Context
//import android.util.Log
//import com.example.akxplayer.db.AkxDatabase
//import com.example.akxplayer.db.dao.QueueDao
//import com.example.akxplayer.db.entity.QueueEntity
//import io.reactivex.rxjava3.core.Completable
//import io.reactivex.rxjava3.core.Single
//import io.reactivex.rxjava3.schedulers.Schedulers
//
//private const val TAG = "QueueRepository"
//
//object QueueRepository {
//
//    private lateinit var queueDao: QueueDao
//
//    fun init(context: Context) {
//        queueDao = AkxDatabase.getInstance(context).queueDao()
//    }
//
//    fun getQueueInfo(): Single<Pair<QueueEntity, LongArray?>> =
//        Single.create { emitter ->
//            val queueEntity: QueueEntity? = queueDao.getQueueData()
//            Log.d(TAG, "loadDataFromDatabase: $queueEntity ${Thread.currentThread().name}")
//            if (queueEntity != null) {
//                val songIds = SongRepository.getSongIds()
//                emitter.onSuccess(Pair(queueEntity, songIds))
//            } else {
//                initializeQueue()
////                emitter.onError(null)
//            }
//        }
//
//    private fun initializeQueue() {
//        queueDao.initializeTable(QueueEntity())
//    }
//
//    fun updateTitle(title: String): Completable = Completable.create {
//        queueDao.updateTitle(title)
//    }.subscribeOn(Schedulers.io())
//
//    fun updateCurrentSong(currentSong: Int):Completable = Completable.create{
//        queueDao.updateCurrentSong(currentSong)
//    }.subscribeOn(Schedulers.io())
//
//    fun updateShuffleMode(shuffleMode: Boolean):Completable = Completable.create {
//        queueDao.updateShuffleMode(shuffleMode)
//    }.subscribeOn(Schedulers.io())
//
//    fun updateRepeatMode(repeatMode: Int): Completable = Completable.create {
//        queueDao.updateRepeatMode(repeatMode)
//    }.subscribeOn(Schedulers.io())
//
//    fun updateSeekPosition(seekPosition: Long): Completable = Completable.create {
//        queueDao.updateSeekPosition(seekPosition)
//    }.subscribeOn(Schedulers.io())
//}