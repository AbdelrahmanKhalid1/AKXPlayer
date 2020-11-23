package com.example.akxplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.akxplayer.R
import com.example.akxplayer.ui.adapters.SectionsPagerAdapter
import com.example.akxplayer.ui.viewmodels.MediaViewModel
import com.google.android.material.tabs.TabLayout

    private const val TAG = "MainFragment"
class MainFragment : Fragment() {
    private lateinit var mediaViewModel: MediaViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)

        val sectionsPagerAdapter =
            SectionsPagerAdapter(
                context!!,
                parentFragmentManager
            )

        val viewPager: ViewPager = view.findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = view.findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mediaViewModel = ViewModelProvider(this).get(MediaViewModel::class.java)
    }
}