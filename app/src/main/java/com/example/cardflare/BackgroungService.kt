package com.example.cardflare

import android.app.Notification
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class AppMonitorService : Service() {
    // This is the foreground service that checks if certain apps are open and starts OverlayActivity
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 2000L // Recheck every 2 seconds

    override fun onCreate() {
        super.onCreate()
        //Starts monitoring
        startMonitoring()
    }

    fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                val currentApp = getForegroundApp(this@AppMonitorService)
                //Starts OverlayService if current runing app is instagram
                if (currentApp == "com.instagram.android") {
                    startOverlay()
                }
              handler.postDelayed(this, checkInterval) // Adds delay between checks
            }
        })
    }

    //returns the name of currently used app
   fun getForegroundApp(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 10 //checks recently used apps

        // Gets android usage stats recently used app
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStats == null || usageStats.isEmpty()) {
            // if result empty return null
            return null
        }
        usageStats?.let {
            //sorts usage stats data by most recently used
            val sortedStats = it.sortedByDescending { stats -> stats.lastTimeUsed }
            // returns the most recently used app name
            return sortedStats.firstOrNull()?.packageName
        }
        return null
    }

    // Starts OverlayService
    private fun startOverlay() {
        // checks if overlay permissions are granted
        if (Settings.canDrawOverlays(this)) {
            val intent = Intent(this, OverlayActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(intent)
        } else {
            Log.e("BackgroundService", "Overlay permission not granted")
        }
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
