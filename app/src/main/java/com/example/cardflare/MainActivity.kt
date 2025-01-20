package com.example.cardflare

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.cardflare.ui.theme.CardFlareTheme
import com.google.gson.Gson
import com.example.cardflare.ui.theme.ColorPalette
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost
import android.media.audiofx.EnvironmentalReverb
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardflare.ui.theme.MainMenuRender
import com.example.cardflare.ui.theme.AddMenu
import com.example.cardflare.ui.theme.AppMonitorService
import com.example.cardflare.ui.theme.deckScreen

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        loadColorPalette()
        checkAndRequestPermissions()
        val intent = Intent(applicationContext, AppMonitorService::class.java)
        ContextCompat.startForegroundService(applicationContext, intent)
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


    }
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }
}
