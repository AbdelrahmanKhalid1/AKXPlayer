package com.example.akxplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.example.akxplayer.util.MediaSession

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals("android.intent.action.PHONE_STATE"))
            if (intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
                    .equals(TelephonyManager.EXTRA_STATE_IDLE)
            ) //call ends
                MediaSession.play()
            else //calling ringing
                MediaSession.pause()
    }
}