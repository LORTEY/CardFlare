package com.example.cardflare

import android.Manifest
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardflare.ui.theme.CardFlareTheme
import com.example.cardflare.ui.theme.ColorPalette
import com.example.cardflare.ui.theme.MainMenuRender
import com.example.cardflare.ui.theme.deckScreen
import com.example.cardflare.ui.theme.renderMainMenu
import com.google.gson.Gson


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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (isActivityRunning(OverlayActivity::class.java)) {
            Log.d("ActivityA", "Activity B is running. Killing Activity A.")
            finish() // Finish Activity A if Activity B is running
        }
        enableEdgeToEdge()
        loadColorPalette()
        checkAndRequestPermissions()
        startMainMenu()
    }
    private fun isActivityRunning(activityClass: Class<*>): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (task in activityManager.getRunningTasks(10)) {
            // Check if Activity B is running
            if (task.topActivity!!.className == activityClass.name) {
                return true // Activity B is running
            }
        }
        return false // Activity B is not running
    }
    private fun loadColorPalette() {
        val jsonString =
            getResources().openRawResource(R.raw.colorstouse).bufferedReader().use { it.readText() }
        val colorPaletteData = Gson().fromJson(jsonString, ColorPaletteData::class.java)
        try {
            ColorPalette.pa0 = Color.parseColor(colorPaletteData.pa0)
            ColorPalette.pa10 = Color.parseColor(colorPaletteData.pa10)
            ColorPalette.pa20 = Color.parseColor(colorPaletteData.pa20)
            ColorPalette.pa30 = Color.parseColor(colorPaletteData.pa30)
            ColorPalette.pa40 = Color.parseColor(colorPaletteData.pa40)
            ColorPalette.pa50 = Color.parseColor(colorPaletteData.pa50)
            ColorPalette.sa0 = Color.parseColor(colorPaletteData.sa0)
            ColorPalette.sa10 = Color.parseColor(colorPaletteData.sa10)
            ColorPalette.sa20 = Color.parseColor(colorPaletteData.sa20)
            ColorPalette.sa30 = Color.parseColor(colorPaletteData.sa30)
            ColorPalette.sa40 = Color.parseColor(colorPaletteData.sa40)
            ColorPalette.sa50 = Color.parseColor(colorPaletteData.sa50)

            Log.d("MainActivity", "Color Palette Loaded Successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading color palette: ${e.message}")
        }
    }
    public fun startMainMenu(){

        val intent = Intent(this, AppMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
        renderMainMenu = true
        setContent {
            CardFlareTheme {
                // Initialize the NavController
                val navController = rememberNavController()

                // Set up the navigation graph
                NavHost(
                    navController = navController,
                    startDestination = "main_menu"
                ) {
                    composable("main_menu") { MainMenuRender(navController, loadData("", context = LocalContext.current)) }

                    composable("deck_menu") { deckScreen(context = LocalContext.current) }}
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        }
        return false
    }
}
