package com.example.akxplayer.ui.fragments.playback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.akxplayer.R
import com.example.akxplayer.constants.RepeatMode
import com.example.akxplayer.databinding.FragmentPlaybackBinding
import com.example.akxplayer.ui.viewmodels.MediaViewModel
import com.example.akxplayer.util.Util

class PlaybackFragment : Fragment() {

    private var _binding: FragmentPlaybackBinding? = null
    private val binding get() = _binding!!
    lateinit var bottomSongControlsView: View
    lateinit var viewDragDown: View
    private lateinit var mediaViewModel: MediaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaybackBinding.inflate(inflater, container, false)
        bottomSongControlsView = binding.bottomControls
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.bottom_controls,
                BottomSongControlsFragment(),
                "Bottom Song Controls"
            ).commit()
        viewDragDown = binding.viewDragDown

        binding.lifecycleOwner = this
        binding.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)

        binding.btnShare.setOnClickListener {
            val uri = Uri.withAppendedPath(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mediaViewModel.getCurrentSong().id.toString()
            )
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.type = "audio/*"
            requireContext().startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share Via....."
                )
            )
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaViewModel = ViewModelProvider(activity!!).get(MediaViewModel::class.java)
        binding.viewModel = mediaViewModel
        updateUI()
    }

    private fun updateUI() {
        mediaViewModel.rootSong.observe(viewLifecycleOwner, Observer { songPosition ->
            if (songPosition != -1) {
                val currentSong = mediaViewModel.getCurrentSong()
                binding.song = currentSong
                binding.nextSong = mediaViewModel.getNextSong()
                binding.seekBar.max = currentSong.getDuration()
            }
        })

        mediaViewModel.isPlaying.observe(viewLifecycleOwner, Observer { isPlaying ->
            if (isPlaying)
                binding.btnPlay.setImageResource(R.drawable.ic_pause_circle_filled)
            else
                binding.btnPlay.setImageResource(R.drawable.ic_play_circle_filled)
        })

        mediaViewModel.shuffleMode.observe(viewLifecycleOwner, Observer { shuffleMode ->
            when (shuffleMode) {
                true -> binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_active)
                else -> binding.btnShuffle.setImageResource(R.drawable.ic_shuffle)
            }
//            binding.nextSong = mediaViewModel.getNextSong()
        })

        mediaViewModel.repeatMode.observe(viewLifecycleOwner, Observer { repeatMode ->
            when (repeatMode) {
                RepeatMode.REPEAT_ALL -> binding.btnRepeat.setImageResource(R.drawable.ic_repeat_active)
                RepeatMode.REPEAT_ONE -> binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one_active)
                else -> binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
            }
            binding.nextSong = mediaViewModel.getNextSong()
        })

        mediaViewModel.title.observe(viewLifecycleOwner, Observer { title ->
            binding.textTitle.text = title
        })

        mediaViewModel.isFavorite.observe(viewLifecycleOwner, Observer { isFavorite ->
            when (isFavorite) {
                true -> binding.btnLike.setImageResource(R.drawable.ic_favorite)
                else -> binding.btnLike.setImageResource(R.drawable.ic_not_favorite)
            }
        })

        mediaViewModel.seekPosition.observe(viewLifecycleOwner, Observer { seekPosition ->
            binding.textProcess.text = Util.fetchDuration(seekPosition)
            binding.seekBar.progress = seekPosition
        })
    }

    private val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser)
                mediaViewModel.setSeekPosition(progress.toLong())
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
        }
    }
}