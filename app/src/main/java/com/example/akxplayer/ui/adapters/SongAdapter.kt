package com.example.akxplayer.ui.adapters

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.akxplayer.R
import com.example.akxplayer.databinding.ItemSongBinding
import com.example.akxplayer.model.Song
import com.example.akxplayer.ui.listeners.OnItemClickListener
import com.example.akxplayer.ui.listeners.PopupMenuListener

open class SongAdapter(
    private val clickListener: OnItemClickListener,
    private val popupMenuListener: PopupMenuListener?,
    private val playlistId: Long
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    var songs: List<Song> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        val inflater = LayoutInflater.from(parent.context)
        return SongViewHolder(
            ItemSongBinding.inflate(inflater, parent, false),
            clickListener,
            popupMenuListener,
            playlistId
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    class SongViewHolder(
        private val binding: ItemSongBinding,
        clickListener: OnItemClickListener,
        menuListener: PopupMenuListener?,
        playlistId: Long
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    clickListener.onItemClick(adapterPosition, binding.root)
            }
            if (menuListener != null) {
                binding.btnSongOption.setMenu(menuListener, R.menu.menu_song)
                binding.btnSongOption.playlistId = playlistId
            } else
                binding.btnSongOption.visibility = View.GONE
        }

        fun bind(song: Song) {
            binding.song = song
            binding.btnSongOption.songId = song.id
        }
    }
}
