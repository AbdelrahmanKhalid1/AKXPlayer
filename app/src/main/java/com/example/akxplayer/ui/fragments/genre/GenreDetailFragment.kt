package com.example.akxplayer.ui.fragments.genre

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.akxplayer.R
import com.example.akxplayer.databinding.FragmentGenreDetailBinding
import com.example.akxplayer.model.Genre
import com.example.akxplayer.ui.adapters.SongAdapter
import com.example.akxplayer.ui.fragments.SongFragment
import com.example.akxplayer.ui.listeners.OnItemClickListener
import com.example.akxplayer.ui.viewmodels.GenreViewModel
import com.example.akxplayer.ui.viewmodels.MediaViewModel


private const val GENRE = "genre"

class GenreDetailFragment : Fragment() {
    private var _binding: FragmentGenreDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var genre: Genre

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            genre = it.getParcelable(GENRE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGenreDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.genre = genre
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        parentFragmentManager.beginTransaction()
            .replace(
                R.id.container_song,
                SongFragment.newInstance(-1, -1, genre.id, -1, genre.name),
                null
            ).commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(genre: Genre) =
            GenreDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(GENRE, genre)
                }
            }
    }
}