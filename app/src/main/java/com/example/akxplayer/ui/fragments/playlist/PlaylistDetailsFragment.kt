package com.example.akxplayer.ui.fragments.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.akxplayer.R
import com.example.akxplayer.databinding.FragmentPlaylistDetailsBinding
import com.example.akxplayer.model.Playlist
import com.example.akxplayer.ui.fragments.song.SongFragment

private const val PLAYLIST = "genre"

class PlaylistDetailsFragment:Fragment() {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var playlist: Playlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlist = it.getParcelable(PLAYLIST)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.playlist = playlist
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        parentFragmentManager.beginTransaction().replace(
            R.id.container_song,
            SongFragment.newInstance(-1,-1,-1, playlist.id, playlist.name),
            null
        ).commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(playlist: Playlist) =
            PlaylistDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PLAYLIST, playlist)
                }
            }
    }
}