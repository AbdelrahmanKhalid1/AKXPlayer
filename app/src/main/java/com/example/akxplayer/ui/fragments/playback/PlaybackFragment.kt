package com.example.akxplayer.ui.fragments.playback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.akxplayer.R
import com.example.akxplayer.databinding.FragmentPlaybackBinding
import com.example.akxplayer.model.Song
import com.example.akxplayer.ui.viewmodels.MediaViewModel
import com.example.akxplayer.util.Util
import com.google.android.exoplayer2.Player

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
        viewDragDown = binding.viewDragDown
        binding.lifecycleOwner = this
        binding.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        parentFragmentManager.beginTransaction()
            .replace(R.id.bottom_controls,
                BottomSongControlsFragment()
            ).commit()
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
            try {
                if (songPosition != -1) {
                    val song = mediaViewModel.queue[songPosition]
                    binding.song = song
                    val nextSong = mediaViewModel.getNextSong()
                    binding.nextSong = nextSong
                }
            } catch (ignore: Exception) {
                binding.nextSong = Song(title = "", artist = "")
            }
        })

        mediaViewModel.isPlaying.observe(viewLifecycleOwner, Observer { isPlaying ->
            if (isPlaying)
                binding.btnPlay.setImageResource(R.drawable.ic_pause)
            else
                binding.btnPlay.setImageResource(R.drawable.ic_play_arrow)
        })

        mediaViewModel.seekPosition.observe(viewLifecycleOwner, Observer { seekPosition ->
            binding.textProcess.text = Util.fetchDuration(seekPosition.toString())
            binding.seekBar.progress = seekPosition.toInt()
        })

        mediaViewModel.shuffleMode.observe(viewLifecycleOwner, Observer { isShuffle ->
            if (isShuffle)
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_active)
            else
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle)
            try {
                binding.nextSong = mediaViewModel.getNextSong()
            } catch (ignore: Exception) {
                binding.nextSong = Song(title = "", artist = "")
            }
        })

        mediaViewModel.repeatMode.observe(viewLifecycleOwner, Observer { repeatMode ->
            when (repeatMode) {
                Player.REPEAT_MODE_ALL -> binding.btnRepeat.setImageResource(R.drawable.ic_repeat_active)
                Player.REPEAT_MODE_ONE -> binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one_active)
                else -> binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
            }
            try {
                binding.nextSong = mediaViewModel.getNextSong()
            } catch (ignore: Exception) {
                binding.nextSong = Song(title = "", artist = "")
            }
        })

        mediaViewModel.title.observe(viewLifecycleOwner, Observer { title ->
            binding.textTitle.text = title
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