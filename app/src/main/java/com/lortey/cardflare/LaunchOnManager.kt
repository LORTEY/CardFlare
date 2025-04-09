package com.lortey.cardflare

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//File contains functions to fetch apps installed on device and store user configs of qhat decks to launch on what apps
data class AppInfo(
    val name: String?,
    val packageName: String,
    val icon: Drawable?
)

suspend fun getListOfApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val packageManager: PackageManager = context.packageManager
    var installedApps: List<PackageInfo> = packageManager.getInstalledPackages(0)

    val appSettings = AppSettings
    require(appSettings["Do Not Show System Apps"]?.state is Boolean)

    if(appSettings["Do Not Show System Apps"]?.state as Boolean){
        installedApps = installedApps.filterNot { packageInfo ->
            // Exclude system apps
            (packageInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0) ||
                    (packageInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)}
    }


    installedApps.map { packageInfo ->
        val appInfo = packageInfo.applicationInfo
        AppInfo(
            name = if (appInfo == null) null else appInfo.loadLabel(packageManager).toString(),
            packageName = packageInfo.packageName,
            icon = packageInfo.applicationInfo?.loadIcon(packageManager)
        )

    }
}

fun appsSearch(searchQuery:String, appList:List<AppInfo>): List<AppInfo>{
    if (searchQuery.isEmpty()){
        return appList
    }
    return appList.filter{it.packageName.contains(searchQuery, ignoreCase = true)
            || it.name?.contains(searchQuery, ignoreCase = true) ?: false}
        .sortedByDescending { app ->
            maxOf(
                calculateSimilarity(searchQuery, app.packageName),
                app.name?.let { calculateSimilarity(searchQuery, it) } ?: 0.0
            )
        }
}