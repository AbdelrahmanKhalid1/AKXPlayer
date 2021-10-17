package com.example.akxplayer.ui.fragments.song

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.akxplayer.R
import com.example.akxplayer.model.Song
import com.example.akxplayer.ui.adapters.SongAdapter
import com.example.akxplayer.ui.dialogs.AddToPlaylistDialog
import com.example.akxplayer.ui.dialogs.CreatePlaylistDialog
import com.example.akxplayer.ui.dialogs.delete.DeleteSongDialog
import com.example.akxplayer.ui.fragments.album.AlbumDetailFragment
import com.example.akxplayer.ui.fragments.album.OnAlbumPlayButtonClick
import com.example.akxplayer.ui.fragments.artist.ArtistDetailFragment
import com.example.akxplayer.ui.fragments.base.BaseFragment
import com.example.akxplayer.ui.listeners.OnDialogClickListener
import com.example.akxplayer.ui.listeners.PopupMenuSongListener
import com.example.akxplayer.ui.viewmodels.SongViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

private const val TAG = "SongFragment"
private const val ALBUM = "ALBUM ID"
private const val ARTIST = "ARTIST ID"
private const val GENRE = "GENRE ID"
private const val PLAYLIST = "PLAYLIST ID"
private const val NAME = "NAME"

class SongFragment : BaseFragment<Song, SongAdapter.SongViewHolder>(), PopupMenuSongListener,
    OnAlbumPlayButtonClick {

    private lateinit var songViewModel: SongViewModel
    private lateinit var adapter: SongAdapter
    private var albumId: Long = -1
    private var artistId: Long = -1
    private var genreId: Long = -1
    private var playlistId: Long = -1
    private var queueName = "All Songs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.albumId = it.getLong(ALBUM)
            this.artistId = it.getLong(ARTIST)
            this.genreId = it.getLong(GENRE)
            this.playlistId = it.getLong(PLAYLIST)
            this.queueName = it.getString(NAME).toString()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = SongAdapter(this, this, playlistId)
        buildRecycler(LinearLayoutManager(context!!), adapter)

        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        songViewModel.init(context!!.contentResolver, albumId, artistId, genreId, playlistId)
        songViewModel.loadSongs(requireContext())
        songViewModel.getSongs().observe(viewLifecycleOwner, Observer { songs ->
            items = songs
            adapter.songs = songs
            adapter.notifyDataSetChanged()
        })
    }

    override fun onItemClick(position: Int, view: View) {
        mediaViewModel.playMedia(position, items, queueName).subscribe()
    }

    override fun addToQueue(songId: Long) {
        mediaViewModel.addToQueue(songId)
    }

    override fun addToPlaylist(songId: Long) {
        val dialog = AddToPlaylistDialog(songId, onAddToPlaylistDialogListener)
        dialog.show(parentFragmentManager, "Add Dialog")
    }

    override fun goToAlbum(songId: Long) {
        Log.d(TAG, "goToAlbum: $albumId")
        if (albumId.compareTo(-1) == 0) {
            songViewModel.getSongAlbum(songId, context!!.contentResolver)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { album ->
                    startFragment(AlbumDetailFragment.newInstance(album, -1), "albumFragment")
                }
        }
    }

    override fun goToArtist(songId: Long) {
        if (artistId.compareTo(-1) == 0) {
            songViewModel.getSongArtist(songId, context!!.contentResolver)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { artist ->
                    startFragment(ArtistDetailFragment.newInstance(artist), "artistFragment")
                }
        }
    }

    override fun deleteSong(songId: Long) {
        if (songId.compareTo(mediaViewModel.rootSong.value!!) == 0) {
            Toast.makeText(requireContext(), "Can't Delete Playing Song!", Toast.LENGTH_SHORT)
                .show()
        } else {
            val dialog = DeleteSongDialog(songId, onDeleteDialogClickListener)
            dialog.show(parentFragmentManager, "Delete Song")
        }
    }

    override fun removeFromPlaylist(songId: Long, playlistId: Long) {
        songViewModel.removeFromPlaylist(songId, playlistId, context!!.contentResolver)
            .subscribe { songViewModel.loadSongs(requireContext()) }
    }

    private fun startFragment(fragment: Fragment, tag: String) {
        parentFragmentManager.beginTransaction()
            .add(
                R.id.container_main,
                fragment,
                tag
            )
            .setCustomAnimations(
                R.anim.slide_up_in,
                R.anim.slide_down_out,
                R.anim.slide_up_in,
                R.anim.slide_down_out
            )
            .addToBackStack(tag).commit()
    }

    private val onDeleteDialogClickListener = object : OnDialogClickListener {
        override fun onDialogClick(songId: Long) {
            songViewModel.loadSongs(requireContext())
            mediaViewModel.removeSongFromQueue(songId)
        }
    }

    private val onAddToPlaylistDialogListener = object : OnDialogClickListener {
        override fun onDialogClick(songId: Long) {
            val dialog = CreatePlaylistDialog(songId, null)
            dialog.show(parentFragmentManager, "Create Dialog")
        }
    }

    override fun onClick() {
        mediaViewModel.playMedia(0, items, queueName).subscribe()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            albumId: Long,
            artistId: Long,
            genreId: Long,
            playlistId: Long,
            queueName: String
        ) =
            SongFragment().apply {
                arguments = Bundle().apply {
                    putLong(ALBUM, albumId)
                    putLong(ARTIST, artistId)
                    putLong(GENRE, genreId)
                    putLong(PLAYLIST, playlistId)
                    putString(NAME, queueName)
                }
            }
    }
}