package com.lortey.cardflare

import android.Manifest
import android.app.AppOpsManager
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
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lortey.cardflare.uiRender.AddDeckScreen
import com.lortey.cardflare.uiRender.AddFlashcardScreen
import com.lortey.cardflare.uiRender.CardMenu
import com.lortey.cardflare.uiRender.LaunchOnMenu
import com.lortey.cardflare.uiRender.LearnScreen
import com.lortey.cardflare.uiRender.MainMenuRender
import com.lortey.cardflare.ui.theme.Material3AppTheme
import com.lortey.cardflare.uiRender.SettingsMenu
import com.lortey.cardflare.uiRender.deckScreen
import com.lortey.cardflare.uiRender.renderMainMenu
import com.lortey.cardflare.uiRender.BinRender
import com.lortey.cardflare.uiRender.BinCards
import com.lortey.cardflare.uiRender.ModifyRule
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder


private const val STORAGE_PERMISSION_CODE = 101
class MainActivity : androidx.activity.ComponentActivity(){
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        listenToKillYourselfBroadcast()

        val intent = Intent(this, AppMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
        enableEdgeToEdge()
        launchOnRules = loadLaunchOnRules(applicationContext)
        EnsureDirectoryStructure(context = applicationContext)
        BinAutoEmpty(context = applicationContext)
        startMainMenu()
    }


    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(receiver)
        Log.d("MainActivity", "Receiver unregistered")
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
                        composable("launch_on_manager") { LaunchOnMenu(navController = navController, context = LocalContext.current) }
                        composable("main_menu") { MainMenuRender(navController, context = LocalContext.current, ::checkAndRequestPermissions1, arePermissionsMissing = ::areAnyPermissionsMissing1) }
                        composable("card_menu") { CardMenu(navController) }
                        composable("learn_screen") { LearnScreen(navController,context = LocalContext.current) }
                        composable("deck_menu") { deckScreen(context = LocalContext.current,navController) }
                        composable("settings") { SettingsMenu(navController,context = LocalContext.current) }
                        composable("deck_add_screen") { AddDeckScreen(context = LocalContext.current, navController) }
                        composable("add_flashcard") { AddFlashcardScreen(context = LocalContext.current, navController = navController) }
                        composable("bin_screen") { BinRender(context = LocalContext.current, navController = navController)}
                        composable("bin_cards_view") { BinCards(context = LocalContext.current, navController = navController) }
                        composable("modify_rule") { ModifyRule(context = LocalContext.current, navController = navController) }}

                }
            }
        }
    }

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
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    private fun areAnyPermissionsMissing1(): Boolean {
        // Check storage permission
        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val isStorageGranted = ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED

        // Check overlay permission
        val isOverlayGranted = Settings.canDrawOverlays(this)

        // Check usage stats permission
        val isUsageAccessGranted = isUsageAccessGranted(this)

        // Return true if any permission is missing
        return /*!isStorageGranted ||*/ !isOverlayGranted || !isUsageAccessGranted
    }

    // Helper function (already in your code)
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