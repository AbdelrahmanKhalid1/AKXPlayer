package com.example.akxplayer.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.akxplayer.databinding.ItemArtistBinding
import com.example.akxplayer.ui.listeners.OnItemClickListener
import com.example.akxplayer.model.Artist

class ArtistAdapter(private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    private var artists : List<Artist> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ArtistViewHolder(
            ItemArtistBinding.inflate(inflater, parent, false),
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artists[position])
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    fun setArtists(artists: List<Artist>){
        this.artists = artists
        notifyDataSetChanged()
    }

    class ArtistViewHolder(private val binding: ItemArtistBinding, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener{
                if(adapterPosition != RecyclerView.NO_POSITION)
                    onItemClickListener.onItemClick(adapterPosition, binding.root)
            }
        }
        fun bind(artist: Artist) {
            binding.artist = artist
//            Glide.with(binding.root)
//                .load(R.drawable.ic_person)
//                .into(binding.imageArtist)
        }
    }
}