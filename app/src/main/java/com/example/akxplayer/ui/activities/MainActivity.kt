package com.example.akxplayer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.akxplayer.ui.fragments.playback.PlaybackFragment
import com.example.akxplayer.R
import com.example.akxplayer.ui.fragments.LibraryFragment
import com.example.akxplayer.ui.fragments.song.QueueFragment
import com.example.akxplayer.ui.viewmodels.MediaViewModel
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.*

class MainActivity : AppCompatActivity() {
    private lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    private lateinit var mediaViewModel: MediaViewModel
    private lateinit var playbackFragment: PlaybackFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_main)
        playbackFragment = PlaybackFragment()

        slidingUpPanelLayout = findViewById(R.id.sliding_layout)
        slidingUpPanelLayout.addPanelSlideListener(panelSlideListener)

        mediaViewModel = ViewModelProvider(this).get(MediaViewModel::class.java)

        supportFragmentManager.beginTransaction().replace(
            R.id.container_main,
            LibraryFragment()
        ).commit()

        supportFragmentManager.beginTransaction().replace(
            R.id.container_playback,
            playbackFragment
        ).commit()

        mediaViewModel.goToQueue.observe(this, Observer {show->
            if (show) {
                if (supportFragmentManager.findFragmentByTag("Queue Fragment") == null) {
                    supportFragmentManager.beginTransaction().add(
                        R.id.container_main,
                        QueueFragment(),
                        "Queue Fragment"
                    ).addToBackStack("queue").commit()
                }
                slidingUpPanelLayout.panelState = COLLAPSED
            }
        })
    }

    private fun setTheme(){
        val settings = getSharedPreferences("AppSettingsPref", 0)
        when (settings.getInt("theme", 1)){
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun onStart() {
        super.onStart()
        mediaViewModel.rootSong.observe(this, Observer { songPosition ->
            if (songPosition != -1) {
                if (slidingUpPanelLayout.panelState == HIDDEN)
                    slidingUpPanelLayout.panelState = COLLAPSED
            }
        })
    }

    private val panelSlideListener = object : PanelSlideListener {
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            try {
                playbackFragment.bottomSongControlsView.alpha = 1 - slideOffset
                playbackFragment.viewDragDown.alpha = slideOffset
            } catch (ignore: UninitializedPropertyAccessException) {

            }
        }

        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_setting-> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_about -> openDialogAbout()
            else ->
                return false
        }
        return true
    }

    private fun openDialogAbout(){

    }

    override fun onBackPressed() {
        when (slidingUpPanelLayout.panelState) {
            ANCHORED, EXPANDED -> slidingUpPanelLayout.panelState = COLLAPSED
            else -> {
                super.onBackPressed()
                slidingUpPanelLayout.isTouchEnabled = true
            }
        }
    }
}