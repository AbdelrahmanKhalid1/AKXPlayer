package com.example.akxplayer.ui.fragments.song

import android.app.Application
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.akxplayer.model.Song
import com.example.akxplayer.receiver.NoisyReceiver
import com.example.akxplayer.ui.adapters.QueueAdapter
import com.example.akxplayer.ui.adapters.SongAdapter
import com.example.akxplayer.ui.fragments.base.BaseFragment

private const val TAG = "QueueFragment"

class QueueFragment : BaseFragment<SongAdapter, SongAdapter.SongViewHolder>() {
    private lateinit var queueAdapter: QueueAdapter
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        queueAdapter = QueueAdapter(this)
        queueAdapter.setQueue(mediaViewModel.getQueue(), mediaViewModel.rootSong.value!!)
        buildRecycler(LinearLayoutManager(requireContext()), queueAdapter)

        mediaViewModel.shuffleMode.observe(viewLifecycleOwner, Observer {
            queueAdapter.setQueue(mediaViewModel.getQueue(), mediaViewModel.rootSong.value!!)
        })
    }

    override fun onItemClick(position: Int, view: View) {
        mediaViewModel.playMedia(position).subscribe{
            requireContext().registerReceiver(
                NoisyReceiver(), IntentFilter(
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            )}
        queueAdapter.songPosition = position
        queueAdapter.notifyDataSetChanged()
    }
}