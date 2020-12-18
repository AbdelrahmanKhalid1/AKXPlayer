package com.example.akxplayer.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.akxplayer.R
import com.example.akxplayer.databinding.ItemPlaylistBinding
import com.example.akxplayer.model.Playlist
import com.example.akxplayer.ui.listeners.OnItemClickListener
import com.example.akxplayer.ui.listeners.PopupMenuPlaylistListener

class PlaylistAdapter(
    var playlists: List<Playlist>,
    private val clickListener: OnItemClickListener,
    private val popupMenuListener: PopupMenuPlaylistListener
) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(ItemPlaylistBinding.bind(view), clickListener, popupMenuListener)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding,
        clickListener: OnItemClickListener,
        popupMenuListener: PopupMenuPlaylistListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    clickListener.onItemClick(adapterPosition, binding.root)
            }
            binding.btnPlaylistOption.setMenu(popupMenuListener, R.menu.menu_playlist)
        }

        fun bind(playlist: Playlist) {
            binding.playlist = playlist
            binding.btnPlaylistOption.songId = playlist.id
        }
    }
}