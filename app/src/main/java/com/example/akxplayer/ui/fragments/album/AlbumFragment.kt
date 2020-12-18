package com.example.akxplayer.ui.fragments.album

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akxplayer.R
import com.example.akxplayer.ui.fragments.base.BaseFragment
import com.example.akxplayer.model.Album
import com.example.akxplayer.ui.adapters.AlbumAdapter
import com.example.akxplayer.ui.viewmodels.AlbumViewModel

private const val ARTIST_ID = "artistId"

class AlbumFragment : BaseFragment<Album, RecyclerView.ViewHolder>() {
    private lateinit var albumViewModel: AlbumViewModel
    private lateinit var adapter: AlbumAdapter
    private var artistId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getLong(ARTIST_ID).let { artistId ->
                this.artistId = artistId
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = AlbumAdapter(this, artistId)
        val layoutManager = if (artistId.compareTo(-1) != 0) LinearLayoutManager(
            context!!,
            LinearLayoutManager.HORIZONTAL,
            false
        ) else GridLayoutManager(context, 2)
        buildRecycler(layoutManager, adapter)

        albumViewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
        albumViewModel.init(context!!.contentResolver, artistId)
        albumViewModel.getAlbums().observe(viewLifecycleOwner, Observer { albums ->
        Log.d("text theme change", "onActivityCreated: ${albums.size}")
            items = albums
            adapter.setAlbums(albums)
        })
        albumViewModel.loadAlbums()
    }

    override fun onItemClick(position: Int, view: View) {
//        val albumImage = view as ImageView
        val albumImage = view.findViewById<ImageView>(R.id.image_album_art)
        val parent = albumImage.parent as ViewGroup
        parent.viewTreeObserver
            .addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Tell the framework to start.
                    parent.viewTreeObserver.removeOnPreDrawListener(this)
//                    startPostponedEnterTransition()
                    return true
                }
            })
        ViewCompat.setTransitionName(albumImage, getString(R.string.transition_image))
        parentFragmentManager.beginTransaction().add(
            R.id.container_main,
            AlbumDetailFragment.newInstance(items[position], artistId)
        ).addToBackStack(null)
            .setReorderingAllowed(true)
            .addSharedElement(albumImage, getString(R.string.transition_image))
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(artistId: Long) = AlbumFragment().apply {
            arguments = Bundle().apply {
                putLong(ARTIST_ID, artistId)
            }
        }
    }
}

