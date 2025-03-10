package com.example.cardflare

import android.Manifest

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardflare.ui.theme.AddDeckScreen
import com.example.cardflare.ui.theme.AddFlashcardScreen
import com.example.cardflare.ui.theme.MainMenuRender
import com.example.cardflare.ui.theme.CardMenu
import com.example.cardflare.ui.theme.LaunchOnMenu
import com.example.cardflare.ui.theme.LearnScreen
import com.example.cardflare.ui.theme.Material3AppTheme
import com.example.cardflare.ui.theme.SettingsMenu
import com.example.cardflare.ui.theme.Typography
import com.example.cardflare.ui.theme.deckScreen
import com.example.cardflare.ui.theme.renderMainMenu
import com.example.cardflare.ui.theme.translatedFlashcardSide
import com.google.gson.Gson
import com.google.mlkit.nl.translate.TranslateLanguage
import dev.chrisbanes.snapper.ExperimentalSnapperApi


data class ColorPaletteData(
    val pa0: String,
    val pa10: String,
    val pa20: String,
    val pa30: String,
    val pa40: String,
    val pa50: String,
    val sa0: String,
    val sa10: String,
    val sa20: String,
    val sa30: String,
    val sa40: String,
    val sa50: String
)
private const val STORAGE_PERMISSION_CODE = 101
class MainActivity : androidx.activity.ComponentActivity(){
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val intent = Intent(this, AppMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
        listenToKillYourselfBroadcast()
        enableEdgeToEdge()
        checkAndRequestPermissions()
        copyAssetsToFilesDir(getApplicationContext())

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

        renderMainMenu = true

        setContent {
            // Apply Material 3 Theme with Dynamic Colors
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
                        composable("main_menu") { MainMenuRender(navController, context = LocalContext.current) }
                        composable("card_menu") { CardMenu(navController) }
                        composable("learn_screen") { LearnScreen(navController,context = LocalContext.current)}
                        composable("deck_menu") { deckScreen(context = LocalContext.current,navController) }
                        composable("settings") { SettingsMenu(navController,context = LocalContext.current) }
                        composable("deck_add_screen") { AddDeckScreen(context = LocalContext.current, navController) }
                        composable("add_flashcard") { AddFlashcardScreen(context = LocalContext.current, navController = navController) }}
                }
            }
        }
    }



    /*
    @Preview(showBackground = true)
        @Composable
        fun GreetingPreview() {
            CardFlareTheme {
                loadColorPalette()
                MainMenuRender(this)
            }
        }*/


    private fun checkAndRequestPermissions() {
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

    }
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }
    fun isUsageAccessGranted(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
        return false
    }
}
