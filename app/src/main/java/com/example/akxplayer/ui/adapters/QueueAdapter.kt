package com.example.akxplayer.ui.adapters

import android.graphics.Color
import com.example.akxplayer.ui.listeners.OnItemClickListener

class QueueAdapter(
    clickListener: OnItemClickListener
) : SongAdapter(clickListener, null, -1) {

    var songIndexList = emptyList<Int>()
    var songPosition: Int = -1

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[songIndexList[position]]
        holder.bind(song)
        if (songPosition == position) {
            holder.itemView.setBackgroundColor(Color.LTGRAY)
        }else
            holder.itemView.setBackgroundColor(Color.WHITE)
    }

    override fun getItemCount(): Int {
        return songIndexList.size
    }
}