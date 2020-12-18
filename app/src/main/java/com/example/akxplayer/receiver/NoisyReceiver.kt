package com.example.akxplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver for when unplugging HeadPhone
 */
private const val TAG = "NoisyReceiver"
class NoisyReceiver(private val pausePlayer: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d(TAG, "onReceive: ")
        pausePlayer()
    }
}