package com.lortey.cardflare

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lortey.cardflare.ui.theme.Material3AppTheme
import com.lortey.cardflare.uiRender.AddDeckScreen
import com.lortey.cardflare.uiRender.AddFlashcardScreen
import com.lortey.cardflare.uiRender.BinCards
import com.lortey.cardflare.uiRender.BinRender
import com.lortey.cardflare.uiRender.CardMenu
import com.lortey.cardflare.uiRender.DueToday
import com.lortey.cardflare.uiRender.ImagePickerScreen
import com.lortey.cardflare.uiRender.LaunchOnMenu
import com.lortey.cardflare.uiRender.LearnScreen
import com.lortey.cardflare.uiRender.MainMenuRender
import com.lortey.cardflare.uiRender.ModifyRule
import com.lortey.cardflare.uiRender.SettingsMenu
import com.lortey.cardflare.uiRender.chooseLanguage
import com.lortey.cardflare.uiRender.deckScreen
import com.lortey.cardflare.uiRender.renderMainMenu
import dev.chrisbanes.snapper.ExperimentalSnapperApi


private const val STORAGE_PERMISSION_CODE = 101
class MainActivity : androidx.activity.ComponentActivity(){
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //register a kill this activity broadcaster used by overlay activity
        listenToKillYourselfBroadcast()

        val intent = Intent(this, AppMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
        enableEdgeToEdge()

        //load translation map
        loadMap(applicationContext)

        //load launch on rules
        launchOnRules = loadLaunchOnRules(applicationContext)

        //create proper FilesDir directory structure if missing
        EnsureDirectoryStructure(context = applicationContext)

        //Ask for notification permissions android 13+
        if (Build.VERSION.SDK_INT >= 33){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.POST_NOTIFICATIONS),101)
        }

        //auto empty bin
        BinAutoEmpty(context = applicationContext)

        //start ui
        startMainMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(receiver)
    }

    // listens to broadcast that kills this activity when overlay activity starts
    private fun listenToKillYourselfBroadcast(){
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("MainActivity", "Broadcast received")
                if ("com.example.KILL_MAIN_ACTIVITY" == intent.action) {
                    finish()
                }
            }
        }

        val filter = IntentFilter("com.example.KILL_MAIN_ACTIVITY")
        registerReceiver(receiver, filter, RECEIVER_EXPORTED)
    }

    @OptIn(ExperimentalSnapperApi::class)
    private fun startMainMenu() {
        loadSettings(applicationContext)
        renderMainMenu = true

        setContent {
            // Apply Material 3 Theme with Dynamic Colors
            //Greeter(context = LocalContext.current,::checkAndRequestPermissions1, arePermissionsMissing = ::areAnyPermissionsMissing1)
            Material3AppTheme {
                val navController = rememberNavController()
                val colorScheme = MaterialTheme.colorScheme
                Log.d("ThemeDebug", MaterialTheme.colorScheme.primary.toString())
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "main_menu"
                    ) {
                        //all paths

                        //menu of launch on rules
                        composable("launch_on_manager") { LaunchOnMenu(navController = navController, context = LocalContext.current) }
                        //home screen
                        composable("main_menu") { MainMenuRender(navController, context = LocalContext.current, ::checkAndRequestPermissions1, arePermissionsMissing = ::areAnyPermissionsMissing1) }
                        // screen that lets you scroll all cards in deck
                        composable("card_menu") { CardMenu(navController) }
                        //learn screen
                        composable("learn_screen") { LearnScreen(navController,context = LocalContext.current) }
                        //menu that displays flashcards in opened deck
                        composable("deck_menu") { deckScreen(context = LocalContext.current,navController) }
                        //settings menu
                        composable("settings") { SettingsMenu(navController,context = LocalContext.current) }
                        //deck adding screen
                        composable("deck_add_screen") { AddDeckScreen(context = LocalContext.current, navController) }
                        //flashcard adding screen
                        composable("add_flashcard") { AddFlashcardScreen(context = LocalContext.current, navController = navController) }
                        // bin view decks in bin screen
                        composable("bin_screen") { BinRender(context = LocalContext.current, navController = navController)}
                        // bin view cards in opened deck in bin
                        composable("bin_cards_view") { BinCards(context = LocalContext.current, navController = navController) }
                        //screen to modify opened launch on rule
                        composable("modify_rule") { ModifyRule(context = LocalContext.current, navController = navController) }
                        //screen to get flashcards from image
                        composable("image_get") { ImagePickerScreen(navController = navController, LocalContext.current) }
                        //language picker screen
                        composable("language_choose") { chooseLanguage(context = LocalContext.current, navController = navController, { translation -> updateSetting("Language", translation); loadMap(context = applicationContext) }) }
                        //screen displaying flashcards due today
                        composable("fsrs_due_today") { DueToday(context = LocalContext.current, navController = navController) }
                    }
                }
            }
        }
    }
    //check and request missing permissions
    private fun checkAndRequestPermissions1() {
        // List of permissions to check
        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        Log.d("ReadWrite:", "Feature enabled: " + (ContextCompat.checkSelfPermission(this, storagePermission)!= PackageManager.PERMISSION_GRANTED).toString())
        if (ContextCompat.checkSelfPermission(this, storagePermission) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestStoragePermission()
        }

        if (!Settings.canDrawOverlays(getApplicationContext())) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"))
            startActivity(intent)
        }
        if (!isUsageAccessGranted(getApplicationContext())) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplicationContext().startActivity(intent)
        }
        if (!hasUsageStatsPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

            val notificationManager = NotificationManagerCompat.from(this)
            val areNotificationsEnabled = notificationManager.areNotificationsEnabled()

            if (!areNotificationsEnabled) {
                // Notifications are disabled - guide user to app settings
                val intent1 = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent1)
            }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${getApplicationContext().packageName}")
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                getApplicationContext().startActivity(intent)
            }
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    //permissions to get insights on what app is now being used
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    //just check missing permissions
    private fun areAnyPermissionsMissing1(): Boolean {
        // Check storage permission
        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val isStorageGranted = ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED

        // Check overlay permission
        val isOverlayGranted = Settings.canDrawOverlays(this)

        // Check usage stats permission
        val isUsageAccessGranted = isUsageAccessGranted(this)

        val notificationManager = NotificationManagerCompat.from(this)
        val areNotificationsEnabled = notificationManager.areNotificationsEnabled()
        // Return true if any permission is missing
        return /*!isStorageGranted ||*/ !isOverlayGranted || !isUsageAccessGranted || !areNotificationsEnabled
    }

    private fun isUsageAccessGranted(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}