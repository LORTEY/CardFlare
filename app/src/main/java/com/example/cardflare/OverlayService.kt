package com.example.cardflare

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.cardflare.ui.theme.MyOverlayComposable

class OverlayService : Service(), LifecycleOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this@OverlayService)

    override fun onCreate() {
        super.onCreate()
        Log.d("BackgroundService", "created1")

        // Initialize WindowManager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Window Layout Params
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        overlayView = ComposeView(this).apply {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
            setContent {
               MyOverlayComposable() // Your Jetpack Compose overlay UI
            }
        }
        if (Settings.canDrawOverlays(this)) {
            windowManager.addView(overlayView, params)
            Log.d("OverlayService", "Overlay permission granted")
        } else {
            Log.d("OverlayService", "Overlay permission not granted")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //windowManager.removeView(overlayView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
