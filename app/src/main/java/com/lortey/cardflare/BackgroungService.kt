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
    //it now does show notification
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 2000L // Recheck every 2 seconds
    private var currentlyBlockedApps:HashSet<String> = hashSetOf()
    private var overwriteDecisionToLearn = false
    private var unlockReceiver: BroadcastReceiver? = null
    override fun onCreate() {
        super.onCreate()

        //so functions can get called when user present
        EventBus.getDefault().register(this)

        //reciever to get userpresent so phone unlocked
        unlockReceiver = UnlockReceiver()
        currentService = this
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)

        //Create Notification must be present for mandroid not to kiill service
        createNotificationChannel()
        startForeground(1, createNotification())

            //updates currently blocked apps every minute
        val updateApps = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                updateBlockedApps()
                delay(60_000L)
            }
        }

        //Starts monitoring on top apps
        startMonitoring()
    }


    fun startMonitoring() {
        var previousApp = ""

        handler.post(object : Runnable {
            override fun run() {
                val currentApp = getForegroundApp(this@AppMonitorService)

                if((currentApp != null && previousApp != currentApp //dont run if app was already checked
                            && currentApp != "com.lortey.cardflare" ) // dont count overlay as ap
                    || overwriteDecisionToLearn ) {// run anyway if blocked apps were updated and currently blocked app which was not yet blocked should now be blocked

                    Thread.sleep(100) // A bug fix it used to randomly pop up when using some apps so i make sure here that this app is really on top

                    if(currentApp == getForegroundApp(this@AppMonitorService)) { //recheck if app is trurly being used
                        previousApp = currentApp ?: "" // reset previous app to current app
                        overwriteDecisionToLearn = false // reset this as it already checked this app
                        if (currentApp in currentlyBlockedApps) { // if app is currently blocked
                            val randomCards = rankByDueDate( // get 3 flashcards with smallest due date to study
                                context = applicationContext,
                                deckList = deckNamesToDeckList(
                                    getRuleFromApp(
                                        appName = currentApp ?: ""
                                    )!!.deckList.toList(), context = applicationContext
                                )
                            ).take(3)

                            if (randomCards.isNotEmpty()) { // if there are cards to check run learn screen in overlay
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

    //updates currently blocked apps
    private suspend fun updateBlockedApps(){
        val previousBlockedApps = currentlyBlockedApps// to check if currently used app is not just now being blocked

        currentlyBlockedApps = generateSetOfBlockedApps()//Get currently blocked set of package names

        val currentApp = getForegroundApp(this@AppMonitorService)
        if(currentApp in currentlyBlockedApps && currentApp !in previousBlockedApps){ // if app was not previously blocked but now is force run learn screen
            overwriteDecisionToLearn = true
        }
    }

    //Create notification channel
    private fun createNotificationChannel() {
        val channelId = "background_service_channel"
        val channel = NotificationChannel(
            channelId, "Background Service", NotificationManager.IMPORTANCE_HIGH,
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    //create notification necessary to not get killed
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

    // returns the name of currently used app
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

    //called  when phone unlocked
    public fun userPresent(){
        createNotification()
        val ruleToRun = anyRuleSetToRunAtUnlock() // checks if there exists a rule that is active now and should run when phone is unlocked
        if(ruleToRun != null){

            //run learn screen
                val randomCards = rankByDueDate(
                    context = applicationContext,
                    deckList = deckNamesToDeckList(
                        ruleToRun.deckList.toList(), context = applicationContext
                    )
                ).take(3)

                if (randomCards != null) {
                    CardsToLearn = randomCards.toMutableList()
                    startOverlay()
                }

        }
    }

    //Is called when reciever recieves user present so phone unlocked
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
