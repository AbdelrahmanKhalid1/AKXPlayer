package com.example.akxplayer.ui.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.akxplayer.R
import com.example.akxplayer.ui.fragments.playlist.PlaylistFragment
import com.example.akxplayer.ui.fragments.album.AlbumFragment
import com.example.akxplayer.ui.fragments.artist.ArtistFragment
import com.example.akxplayer.ui.fragments.genre.GenreFragment
import com.example.akxplayer.ui.fragments.SongFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_text_3,
    R.string.tab_text_2,
    R.string.tab_text_1,
    R.string.tab_text_5,
    R.string.tab_text_4
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PlaylistFragment()
            1 -> AlbumFragment()
            2 -> SongFragment()
            3 -> ArtistFragment()
            else -> GenreFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}