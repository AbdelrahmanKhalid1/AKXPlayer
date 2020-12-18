package com.example.akxplayer.ui.fragments.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.akxplayer.R
import com.example.akxplayer.databinding.FragmentPlaylistBinding
import com.example.akxplayer.model.Playlist
import com.example.akxplayer.ui.adapters.PlaylistAdapter
import com.example.akxplayer.ui.dialogs.CreatePlaylistDialog
import com.example.akxplayer.ui.dialogs.DeletePlaylistDialog
import com.example.akxplayer.ui.dialogs.RenameDialog
import com.example.akxplayer.ui.listeners.OnDialogClickListener
import com.example.akxplayer.ui.listeners.OnItemClickListener
import com.example.akxplayer.ui.listeners.PopupMenuPlaylistListener
import com.example.akxplayer.ui.viewmodels.PlaylistViewModel

class PlaylistFragment : Fragment(), OnItemClickListener, OnDialogClickListener,
    PopupMenuPlaylistListener {

    private lateinit var playlistViewModel: PlaylistViewModel
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private var items = emptyList<Playlist>()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistBinding.inflate(
            inflater,
            container,
            false
        )

        playlistViewModel = ViewModelProvider(this).get(PlaylistViewModel::class.java)
        binding.lifecycleOwner = this
        binding.btnNewPlaylist.setOnClickListener {
            CreatePlaylistDialog(-1, this).show(
                parentFragmentManager,
                "Create Dialog"
            )
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        buildRecycler()
        playlistViewModel.init(context!!.contentResolver)
        playlistViewModel.loadPlaylist()

        playlistViewModel.getPlaylists().observe(viewLifecycleOwner, Observer { playlists ->
            items = playlists
            playlistAdapter.playlists = playlists
            playlistAdapter.notifyDataSetChanged()
        })
    }

    private fun buildRecycler() {
        playlistAdapter = PlaylistAdapter(items, this, this)
        binding.recycler.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    override fun onItemClick(position: Int, view: View) {
        parentFragmentManager.beginTransaction()
            .add(
                R.id.container_main,
                PlaylistDetailsFragment.newInstance(items[position]),
                "playlistFragment"
            )
            .setCustomAnimations(
                R.anim.slide_up_in,
                R.anim.slide_down_out,
                R.anim.slide_up_in,
                R.anim.slide_down_out
            )
            .addToBackStack("playlistFragment")
            .commit()
    }

    override fun onDialogClick(songId: Long) {
        playlistViewModel.loadPlaylist()
        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
    }

    override fun rename(playlistId: Long) {
        val dialog = RenameDialog(playlistId, this)
        dialog.show(parentFragmentManager, "Rename")
    }

    override fun deletePlaylist(playlistId: Long) {
        val dialog = DeletePlaylistDialog(playlistId, this)
        dialog.show(parentFragmentManager, "Delete Playlist")
    }
}