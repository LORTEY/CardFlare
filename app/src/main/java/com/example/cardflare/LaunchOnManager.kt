package com.example.cardflare

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

//File contains functions to fetch apps installed on device and store user configs of qhat decks to launch on what apps
data class AppInfo(
    val name: String,
    val icon: Drawable?
)

fun GetListOfApps(context: Context): List<AppInfo>{
    val packageManager: PackageManager = context.packageManager
    val installedApps: List<PackageInfo> = packageManager.getInstalledPackages(0)

    return installedApps.map { packageInfo ->
        val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
        val appIcon = packageInfo.applicationInfo?.loadIcon(packageManager)

        AppInfo(appName, appIcon)
    }
}

