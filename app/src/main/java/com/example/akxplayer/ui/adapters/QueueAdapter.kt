package com.example.akxplayer.ui.adapters

import android.graphics.Color
import com.example.akxplayer.R
import com.example.akxplayer.model.Song
import com.example.akxplayer.ui.listeners.OnItemClickListener

class QueueAdapter(
    clickListener: OnItemClickListener
) : SongAdapter(clickListener, null, -1) {

    private var songIndexList = emptyList<Int>()
    var songPosition: Int = -1

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[songIndexList[position]]
        holder.bind(song)
        holder.itemView.isPressed = songPosition == position
    }

    override fun getItemCount(): Int {
        return songIndexList.size
    }

    fun setQueue(queue: Pair<List<Song>, List<Int>>, songPosition: Int) {
        songs = queue.first
        songIndexList = queue.second
        this.songPosition = songPosition
        notifyDataSetChanged()
    }
}