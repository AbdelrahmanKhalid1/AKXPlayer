package com.example.akxplayer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.akxplayer.ui.fragments.playback.PlaybackFragment
import com.example.akxplayer.R
import com.example.akxplayer.ui.dialogs.AboutDialog
import com.example.akxplayer.ui.fragments.LibraryFragment
import com.example.akxplayer.ui.fragments.song.QueueFragment
import com.example.akxplayer.ui.viewmodels.MediaViewModel
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    private lateinit var mediaViewModel: MediaViewModel
    private lateinit var playbackFragment: PlaybackFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    mediaViewModel.goToQueue.value = false
                }
                slidingUpPanelLayout.panelState = COLLAPSED
            }
        })
        mediaViewModel.rootSong.observe(this, Observer { songPosition ->
            Log.d(TAG, "onCreate: $songPosition")
            if (songPosition != -1) {
                if(slidingUpPanelLayout.panelState == HIDDEN)
                    slidingUpPanelLayout.panelState = COLLAPSED
            }else
                slidingUpPanelLayout.panelState = HIDDEN
        })

//        mediaViewModel.isPlaying.observe(this, Observer { isPlaying->
//            if(isPlaying)
//                mediaViewModel.goToQueue.value = true
//        })
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
            if(newState == COLLAPSED){
                Log.d(TAG, "onPanelStateChanged: $newState")
                playbackFragment.bottomSongControlsView.alpha = 1f
            }
        }
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