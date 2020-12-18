package com.example.akxplayer.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.akxplayer.ui.adapters.QueueAdapter
import com.example.akxplayer.ui.adapters.SongAdapter
import com.example.akxplayer.ui.fragments.base.BaseFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

private const val TAG = "QueueFragment"
class QueueFragment : BaseFragment<SongAdapter, SongAdapter.SongViewHolder>() {
    private lateinit var queueAdapter: QueueAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        queueAdapter = QueueAdapter(this)
        buildRecycler(LinearLayoutManager(context!!), queueAdapter)

        queueAdapter.songs = mediaViewModel.queue

        mediaViewModel.shuffleMode.observe(viewLifecycleOwner, Observer {
            mediaViewModel.getQueue().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { pair ->
                    queueAdapter.songIndexList = pair.second
                    queueAdapter.songPosition = pair.first
                    queueAdapter.notifyDataSetChanged()
                }
        })
    }

    override fun onItemClick(position: Int, view: View) {
        val songIndex = queueAdapter.songIndexList[position] // Get The Index of the song in the queue List
        queueAdapter.notifyItemChanged(queueAdapter.songPosition) //Reset ViewHolder by it's position
        queueAdapter.songPosition = position //Change the current song position by the new position
        queueAdapter.notifyItemChanged(position)
        mediaViewModel.playMedia(songIndex).subscribe()//Play Song of Index
    }
}