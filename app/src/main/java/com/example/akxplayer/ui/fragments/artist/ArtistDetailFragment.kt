package com.example.akxplayer.ui.fragments.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.akxplayer.R
import com.example.akxplayer.databinding.FragmentArtistDetailBinding
import com.example.akxplayer.model.Artist
import com.example.akxplayer.ui.fragments.song.SongFragment
import com.example.akxplayer.ui.fragments.album.AlbumFragment

private const val ARTIST = "artist"

class ArtistDetailFragment : Fragment() {

    private lateinit var artist: Artist
    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            artist = it.getParcelable(ARTIST)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentArtistDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.artist = artist
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        parentFragmentManager.beginTransaction().replace(
            R.id.container_song,
            SongFragment.newInstance(-1, artist.id, -1, -1, "${artist.name} Songs")
        ).commit()

        parentFragmentManager.beginTransaction().replace(
            R.id.container_album,
            AlbumFragment.newInstance(artist.id)
        ).commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(artist: Artist) =
            ArtistDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARTIST, artist)
                }
            }
    }
}