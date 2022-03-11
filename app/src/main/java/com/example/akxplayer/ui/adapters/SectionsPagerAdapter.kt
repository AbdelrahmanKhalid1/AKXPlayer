package com.example.akxplayer.ui.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.akxplayer.R
import com.example.akxplayer.ui.fragments.album.AlbumFragment
import com.example.akxplayer.ui.fragments.artist.ArtistFragment
import com.example.akxplayer.ui.fragments.genre.GenreFragment
import com.example.akxplayer.ui.fragments.playlist.PlaylistFragment
import com.example.akxplayer.ui.fragments.song.SongFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

private val TAB_TITLES = arrayOf(
    R.string.tab_text_3,
    R.string.tab_text_2,
    R.string.tab_text_1,
    R.string.tab_text_5,
    R.string.tab_text_4
)

class SectionsPagerAdapter(private val context: Context, fa: FragmentActivity) :
    FragmentStateAdapter(fa) {

    fun connectTabWitPager(viewPager: ViewPager2, tabLayout: TabLayout) {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = context.getString(TAB_TITLES[position])
        }.attach()
    }

    override fun getItemCount(): Int {
        return TAB_TITLES.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PlaylistFragment()
            1 -> AlbumFragment()
            2 -> SongFragment()
            3 -> ArtistFragment()
            else -> GenreFragment()
        }
    }
}
