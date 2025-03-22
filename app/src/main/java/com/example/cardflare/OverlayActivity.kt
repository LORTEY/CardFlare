package com.example.cardflare

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.example.cardflare.uiRender.MyOverlayComposable


class OverlayActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Sends broadcast to kill the main activity. If not for this, if the MainActivity was runing the overlay service would not trurly draw overlays
        killMainActivity()

        // Check if the app has the permission to draw over other apps
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, 1234)
        } else {
            // If permission is granted show the overlay
            showOverlay()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showOverlay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
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
                showOverlay() // If permission granted show overlay
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun killMainActivity() {
        //Sent broadcast to kill main activity
        val intent = Intent("com.example.KILL_MAIN_ACTIVITY")
        sendBroadcast(intent)
    }

    // Finishes the activity when it is no longer seen by the user
    override fun onStop(){
        super.onStop()
        finish()
    }
}

@Composable
fun OverlayView() {
    // activates composable from MainMenuRender
    MyOverlayComposable()
}