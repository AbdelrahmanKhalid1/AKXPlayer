package com.example.akxplayer.ui.fragments.artist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.akxplayer.R
import com.example.akxplayer.ui.fragments.base.BaseFragment
import com.example.akxplayer.model.Artist
import com.example.akxplayer.ui.activities.MainActivity
import com.example.akxplayer.ui.adapters.ArtistAdapter
import com.example.akxplayer.ui.viewmodels.ArtistViewModel

private const val TAG = "ArtistFragment"

class ArtistFragment : BaseFragment<Artist, ArtistAdapter.ArtistViewHolder>() {
    private lateinit var artistViewModel: ArtistViewModel
    private lateinit var adapter: ArtistAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        artistViewModel = ViewModelProvider(this)[ArtistViewModel::class.java]
        artistViewModel.init(context!!.contentResolver)
        adapter = ArtistAdapter(this)
        buildRecycler(LinearLayoutManager(requireContext()), adapter)
        artistViewModel.getArtist()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { artists ->
                items = artists
                adapter.setArtists(artists)
            })
        artistViewModel.loadArtist()
    }

    override fun onItemClick(position: Int, view: View) {
        parentFragmentManager.beginTransaction()
            .add(
                R.id.container_main,
                ArtistDetailFragment.newInstance(items[position]),
                "artistFragment"
            )
            .addToBackStack("artistFragment")
            .commit()
    }
}