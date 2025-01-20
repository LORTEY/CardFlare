package com.example.cardflare.ui.theme

import android.app.Activity
import android.app.Notification
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardflare.OverlayService
import com.example.cardflare.R
import com.example.cardflare.loadData

class AppMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 2000L // Check every 2 seconds

    override fun onCreate() {
        super.onCreate()
        // Start the foreground service with a notification
        startForeground(1, createNotification())
        startMonitoring()
    }

    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                val currentApp = getForegroundApp(applicationContext)  // Use applicationContext here
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

        usageStats?.let {
            val sortedStats = it.sortedByDescending { stats -> stats.lastTimeUsed }
            return sortedStats.firstOrNull()?.packageName
        }
        return null
    }
    private fun startOverlay() {
        // Start the OverlayService with the application context
        val intent = Intent(applicationContext, OverlayService::class.java)
        ContextCompat.startForegroundService(applicationContext, intent)
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
