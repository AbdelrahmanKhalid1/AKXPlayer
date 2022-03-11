package com.example.akxplayer.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.akxplayer.R
import com.example.akxplayer.databinding.ItemGenreBinding
import com.example.akxplayer.model.Genre
import com.example.akxplayer.ui.listeners.OnItemClickListener

class GenreAdapter(private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    private var genres: List<Genre> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(
            ItemGenreBinding.bind(view),
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(genres[position])
    }

    override fun getItemCount(): Int {
        return genres.size
    }

    fun setGenre(genres: List<Genre>) {
        this.genres = genres
        notifyDataSetChanged()
    }

    class GenreViewHolder(private val binding: ItemGenreBinding, private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    listener.onItemClick(adapterPosition, binding.root)
            }
        }

        fun bind(genre: Genre) {
            binding.genre = genre
//            binding.executePendingBindings()
        }
    }
}
