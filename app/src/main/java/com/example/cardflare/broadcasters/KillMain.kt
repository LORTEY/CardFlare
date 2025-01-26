package com.example.cardflare.broadcasters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class KillMain : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //Broadcast to kill main activity used by OverlayActivity
        if ("com.example.KILL_MAIN_ACTIVITY" == intent.action) {
            Log.d("ActivityA", "Broadcast received: Activity A should finish")
        }
    }
}