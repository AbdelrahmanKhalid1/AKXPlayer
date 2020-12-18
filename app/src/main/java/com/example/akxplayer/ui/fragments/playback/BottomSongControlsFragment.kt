package com.example.akxplayer.ui.fragments.playback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.akxplayer.R
import com.example.akxplayer.databinding.FragmentSongControlsBinding
import com.example.akxplayer.ui.viewmodels.MediaViewModel

class BottomSongControlsFragment : Fragment() {
    private var _binding: FragmentSongControlsBinding? = null
    private lateinit var mediaViewModel: MediaViewModel
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSongControlsBinding.inflate(
            inflater,
            container,
            false
        )

        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mediaViewModel = ViewModelProvider(activity!!).get(MediaViewModel::class.java)
        binding.viewModel = mediaViewModel
        mediaViewModel.rootSong.observe(viewLifecycleOwner, Observer { songPosition ->
            if (songPosition != -1) {
                try {
                    binding.song = mediaViewModel.queue[songPosition]
                } catch (ignore: Exception) {
                }
            }
        })

        mediaViewModel.isPlaying.observe(viewLifecycleOwner, Observer { isPlaying ->
            if (isPlaying)
                binding.btnPlay.setImageResource(R.drawable.ic_pause)
            else
                binding.btnPlay.setImageResource(R.drawable.ic_play_arrow)
        })
    }
}