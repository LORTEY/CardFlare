package com.example.cardflare

import android.app.ActivityManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.cardflare.ui.theme.MyOverlayComposable


class OverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        killMainActivity()
        // Check if the app has the permission to draw over other apps
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, 1234)  // 1234 is a request code, can be any integer
        } else {
            // If permission is granted, show the overlay
            showOverlay()
        }
    }

    private fun showOverlay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // For Android 8.0 and above
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        setContent {
            OverlayView()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) {
            if (Settings.canDrawOverlays(this)) {
                showOverlay() // If permission is granted, show overlay
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun killMainActivity() {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(10) // Get running tasks

        for (task in tasks) {
            if (task.topActivity!!.className == "com.example.cardflare.MainActivity") {
                activityManager.killBackgroundProcesses(task.topActivity!!.packageName)
                break
            }
        }
    }
}

@Composable
fun OverlayView() {
    // Activates composable from MainMenuRender
    MyOverlayComposable()
}