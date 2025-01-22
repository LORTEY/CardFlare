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

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 2000L // Check every 2 seconds

    override fun onCreate() {
        super.onCreate()
        Log.d("BackgroundService", "started")
        // Start the foreground service with a notification
        //startForeground(1, createNotification())
        startMonitoring()
    }

    fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                val currentApp = getForegroundApp(this@AppMonitorService)  // Use applicationContext here
                Log.d("BackgroundService", currentApp?:"null")
                if (currentApp == "com.instagram.android") {
                    startOverlay()
                }
                handler.postDelayed(this, checkInterval)
            }
        })
    }
    fun getForegroundApp(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 10 // Check for recent app usage

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStats == null || usageStats.isEmpty()) {
            Log.d("getForegroundApp", "No usage stats data available")
            return null
        }
        usageStats?.let {
            val sortedStats = it.sortedByDescending { stats -> stats.lastTimeUsed }
            return sortedStats.firstOrNull()?.packageName
        }
        return null
    }
    private fun startOverlay() {
        // Start the OverlayService with the application context
        val intent = Intent(this, OverlayService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "monitor_channel_id")
            .setContentTitle("Monitoring Apps")
            .setContentText("App monitoring is running")
            .setSmallIcon(R.drawable.bar_chart_2)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
