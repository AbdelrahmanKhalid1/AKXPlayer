package com.example.akxplayer.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.akxplayer.R
import com.example.akxplayer.databinding.ItemAlbumBinding
import com.example.akxplayer.databinding.ItemAlbumSmallBinding
import com.example.akxplayer.model.Album
import com.example.akxplayer.ui.listeners.OnItemClickListener

class AlbumAdapter(private val listener: OnItemClickListener, private val artistId: Long) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var albums: List<Album> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (artistId.compareTo(-1) != 0)
            AlbumSmallViewHolder(
                ItemAlbumSmallBinding.inflate(inflater, parent, false),
                listener
            )
        else
            AlbumViewHolder(
                ItemAlbumBinding.inflate(inflater, parent, false),
                listener
            )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val album = albums[position]
        if (artistId.compareTo(-1) != 0) {
            (holder as AlbumSmallViewHolder).bind(album)
        } else
            (holder as AlbumViewHolder).bind(album)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun setAlbums(albums: List<Album>) {
        this.albums = albums
        notifyDataSetChanged()
    }

    class AlbumViewHolder(private val binding: ItemAlbumBinding, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    listener.onItemClick(adapterPosition, binding.root)
            }
        }

        fun bind(album: Album) {
            binding.album = album
            binding.executePendingBindings()
        }
    }

    class AlbumSmallViewHolder(
        private val binding: ItemAlbumSmallBinding,
        listener: OnItemClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    listener.onItemClick(adapterPosition, binding.root)
            }
        }

        fun bind(album: Album) {
            binding.album = album
            binding.executePendingBindings()
        }
    }
}