package com.example.akxplayer.ui.fragments.album

import android.content.ContentUris
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.akxplayer.R
import com.example.akxplayer.databinding.FragmentAlbumDetailBinding
import com.example.akxplayer.model.Album
import com.example.akxplayer.ui.fragments.song.SongFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout


private const val ALBUM = "album"
private const val ARTIST_ID = "artist_id"

class AlbumDetailFragment : Fragment() {

    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var playButtonClick: OnAlbumPlayButtonClick
    private lateinit var album: Album
    private var artistId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.album = it.getParcelable(ALBUM)!!
            this.artistId = it.getLong(ARTIST_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        _binding = FragmentAlbumDetailBinding.inflate(
            inflater,
            container,
            false
        )
        ViewCompat.setTransitionName(binding.imageAlbumArt, getString(R.string.transition_image))
        postponeEnterTransition()
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.transition_fragment)

        binding.imageAlbumArt.viewTreeObserver
            .addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Tell the framework to start.
                    binding.imageAlbumArt.viewTreeObserver.removeOnPreDrawListener(this)
                    startPostponedEnterTransition()
                    return true
                }
            })

        binding.lifecycleOwner = this
        binding.album = album
        loadImage()
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar as Toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.collapsingToolbar.title = album.name
        setHasOptionsMenu(true)
        binding.collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val songFragment = SongFragment.newInstance(album.id, artistId, -1, -1, album.name)
        playButtonClick = songFragment
        childFragmentManager.beginTransaction().replace(
            R.id.container_song,
            songFragment,
            null
        ).commit()
        binding.btnPlay.setOnClickListener { playButtonClick.onClick() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            parentFragmentManager.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadImage() {
        val uriAlbumArt = Uri.parse("content://media/external/audio/albumart");
        val uriCurrentAlbum = ContentUris.withAppendedId(uriAlbumArt, album.id)
        Glide.with(binding.imageAlbumArt)
            .load(uriCurrentAlbum)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.ic_album)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
//                    startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
//                    startPostponedEnterTransition()
                    return false
                }
            })
            .into(binding.imageAlbumArt)
    }

    companion object {
        @JvmStatic
        fun newInstance(album: Album, artistId: Long) =
            AlbumDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ALBUM, album)
                    putLong(ARTIST_ID, artistId)
                }
            }
    }
}

interface OnAlbumPlayButtonClick {
    fun onClick()
}