package com.lortey.cardflare

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//File contains functions to fetch apps installed on device and store user configs of qhat decks to launch on what apps
data class AppInfo(
    val name: String,
    val icon: Drawable?
)

suspend fun getListOfApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val packageManager: PackageManager = context.packageManager
    val installedApps: List<PackageInfo> = packageManager.getInstalledPackages(0)

    installedApps.map { packageInfo ->
        val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
        val appIcon = packageInfo.applicationInfo?.loadIcon(packageManager)

        AppInfo(appName, appIcon)
    }
}

