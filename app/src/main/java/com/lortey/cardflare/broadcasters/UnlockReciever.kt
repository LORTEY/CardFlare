package com.lortey.cardflare.broadcasters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.greenrobot.eventbus.EventBus


public class UnlockEvent {
}
class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_USER_PRESENT == intent.action) {
            Log.d("cardflarerec","unlocked")
            EventBus.getDefault().post(UnlockEvent())
        }
    }

}