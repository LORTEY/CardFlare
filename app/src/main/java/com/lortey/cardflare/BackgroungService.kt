package com.lortey.cardflare

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lortey.cardflare.broadcasters.UnlockEvent
import com.lortey.cardflare.broadcasters.UnlockReceiver
import com.lortey.cardflare.uiRender.CardsToLearn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


public var currentService:AppMonitorService? = null
class AppMonitorService : Service() {
    // This is the foreground service that checks if certain apps are open and starts OverlayActivity
    // I dont know why it does not show a notification but it seems not to be closing automatically so its good
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 2000L // Recheck every 2 seconds
    private var currentlyBlockedApps:HashSet<String> = hashSetOf()
    private var overwriteDecisionToLearn = false
    private var unlockReceiver: BroadcastReceiver? = null
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        unlockReceiver = UnlockReceiver()
        currentService = this
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)
        createNotificationChannel()
        startForeground(1, createNotification())
        //Starts monitoring
        val updateApps = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                updateBlockedApps()
                delay(30_000L)
            }
        }
        startMonitoring()
    }


    fun startMonitoring() {
        var previousApp = ""
        handler.post(object : Runnable {
            override fun run() {
                val currentApp = getForegroundApp(this@AppMonitorService)
                previousApp?.let { Log.d("cardflareBackgroundprevious", it) }
                if((currentApp != null && previousApp != currentApp && currentApp != "com.lortey.cardflare" )|| overwriteDecisionToLearn) {
                    Thread.sleep(100) // A bug fix it used to randomly pop up when using some apps so i make sure here that this app is really on top
                    currentApp?.let { Log.d("cardflareBackgroundcurrent", it) }
                    if(currentApp == getForegroundApp(this@AppMonitorService)) {
                        previousApp = currentApp ?: ""
                        overwriteDecisionToLearn = false
                        if (currentApp in currentlyBlockedApps) {
                            val randomCards = rankByDueDate(
                                context = applicationContext,
                                deckList = deckNamesToDeckList(
                                    getRuleFromApp(
                                        appName = currentApp ?: ""
                                    )!!.deckList.toList(), context = applicationContext
                                )
                            ).take(3)
                            Log.d("cardflare5", randomCards.toString())
                            if (randomCards != null) {
                                CardsToLearn = randomCards.toMutableList()
                                startOverlay()
                            }
                        }
                    }
                }
              handler.postDelayed(this, checkInterval) // Adds delay between checks
            }
        })
    }
    private suspend fun updateBlockedApps(){
        val previousBlockedApps = currentlyBlockedApps
        Log.d("cardflare4prev",previousBlockedApps.toString())
        currentlyBlockedApps = generateSetOfBlockedApps()
        Log.d("cardflare4",currentlyBlockedApps.toString())
        val currentApp = getForegroundApp(this@AppMonitorService)
        if(currentApp in currentlyBlockedApps && currentApp !in previousBlockedApps){
            overwriteDecisionToLearn = true
        }
    }
    private fun createNotificationChannel() {
        val channelId = "background_service_channel"
        val channel = NotificationChannel(
            channelId, "Background Service", NotificationManager.IMPORTANCE_HIGH,
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    private fun createNotification(): Notification {
        val channelId = "background_service_channel"
        val channel = NotificationChannel(
            channelId, "Background Service", NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
                .setContentTitle(getTranslation("Cardflare Is Ready To Serve You Flashcards"))
                .setContentText(getTranslation("Cardflare's service is running in the background."))
                .setPriority(NotificationCompat.PRIORITY_LOW) // For pre-Oreo devices
                .setOngoing(true) // Makes it persistent (can't be swiped away)
                .setAutoCancel(false)
                .setSound(null) // No sound
                .setVibrate(null) // No vibration
                .setOnlyAlertOnce(true).build() // Only alert once even if updated

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
        usageStats.let {
            //sorts usage stats data by most recently used
            val sortedStats = it.sortedByDescending { stats -> stats.lastTimeUsed }
            // returns the most recently used app name
            return sortedStats.firstOrNull()?.packageName
        }
        return null
    }
    public fun userPresent(){
        createNotification()
        val ruleToRun = anyRuleSetToRunAtUnlock()
        if(ruleToRun != null){
                val randomCards = rankByDueDate(
                    context = applicationContext,
                    deckList = deckNamesToDeckList(
                        ruleToRun.deckList.toList(), context = applicationContext
                    )
                ).take(3)
                Log.d("cardflare5", randomCards.toString())
                if (randomCards != null) {
                    CardsToLearn = randomCards.toMutableList()
                    startOverlay()
                }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnlockEvent(event: UnlockEvent?) {
        userPresent()
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
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Ensures the service restarts if killed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);
        handler.removeCallbacksAndMessages(null)
    }
}
