package com.example.akxplayer.ui.fragments.genre

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.akxplayer.R
import com.example.akxplayer.model.Genre
import com.example.akxplayer.ui.adapters.GenreAdapter
import com.example.akxplayer.ui.fragments.base.BaseFragment
import com.example.akxplayer.ui.viewmodels.GenreViewModel

class GenreFragment : BaseFragment<Genre, GenreAdapter.GenreViewHolder>() {
    private lateinit var genreViewModel: GenreViewModel
    private lateinit var adapter: GenreAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        genreViewModel = ViewModelProvider(this)[GenreViewModel::class.java]
        genreViewModel.init(context!!.contentResolver)
        genreViewModel.loadGenres()

        adapter = GenreAdapter(this)
        buildRecycler(LinearLayoutManager(context!!), adapter)
        genreViewModel.getGenre().observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { genres ->
                items = genres
                adapter.setGenre(genres)
            }
        )
    }

    override fun onItemClick(position: Int, view: View) {
        activity!!.supportFragmentManager.beginTransaction()
            .add(
                R.id.container_main,
                GenreDetailFragment.newInstance(items[position]),
                "genreFragment"
            )
            .addToBackStack("genreFragment")
            .commit()
    }
}
