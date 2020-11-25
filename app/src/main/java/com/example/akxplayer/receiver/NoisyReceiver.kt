package com.example.akxplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.akxplayer.util.MediaSession

/**
 * Receiver for when unplugging HeadPhone
 */
class NoisyReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        MediaSession.pause()
    }
}