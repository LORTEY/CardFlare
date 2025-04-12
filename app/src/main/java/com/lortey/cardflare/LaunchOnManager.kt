package com.lortey.cardflare

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import com.lortey.cardflare.uiRender.launchOnRuleToModify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

//File contains functions to fetch apps installed on device and store user configs of qhat decks to launch on what apps
data class AppInfo(
    val name: String?,
    val packageName: String,
    val icon: Drawable?
)

var launchOnRules:MutableList<LaunchOnRule> = mutableListOf()
const val LaunchOnRuleFile = "LaunchOnRules.txt"
private val jsonFormat = Json { prettyPrint = true }

suspend fun getListOfApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val packageManager: PackageManager = context.packageManager
    var installedApps: List<PackageInfo> = packageManager.getInstalledPackages(0)

    val appSettings = AppSettings
    require(appSettings["Do Not Show System Apps"]?.state is Boolean)
    installedApps = installedApps.filter { packageInfo -> packageInfo.packageName != "com.lortey.cardflare"}
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

fun loadLaunchOnRules(context:Context):MutableList<LaunchOnRule>{
    val file = File(context.getExternalFilesDir(null), LaunchOnRuleFile)
    if(!file.exists()){
        saveLaunchOnRules(context, mutableListOf())
    }
    val jsonString = file.bufferedReader().use { it.readText() }
    if (!jsonString.isBlank()) {
        return jsonFormat.decodeFromString<MutableList<LaunchOnRule>>(jsonString)
    }
    return mutableListOf<LaunchOnRule>()
}

fun saveLaunchOnRules(context:Context, launchOnRules:MutableList<LaunchOnRule>){
    val jsonString = jsonFormat.encodeToString(launchOnRules)
    val file = File(context.getExternalFilesDir(null), LaunchOnRuleFile)
    file.writeText(jsonString)
}

fun addAppToRule(app:AppInfo){
    if(launchOnRuleToModify.value != null && !((app.packageName in (launchOnRuleToModify.value?.appList
            ?: mutableListOf())))){
        // The rebuild is necessary or else jetpack compose wont refresh
        launchOnRuleToModify.value = LaunchOnRule(name = launchOnRuleToModify.value?.name ?: "",
            appList = (launchOnRuleToModify.value?.appList ?: mutableListOf()).toMutableList().apply{add(app.packageName)},
            flashcardList = launchOnRuleToModify.value?.flashcardList ?: mutableListOf())
        Log.d("cardflare3",launchOnRuleToModify.toString())
    }
}

fun removeAppFromRule(packageName: String){
    if(launchOnRuleToModify.value != null){
        // The rebuild is necessary or else jetpack compose wont refresh
        val appList:MutableList<String> = (launchOnRuleToModify.value?.appList ?: mutableListOf()).toMutableList().filterNot { it == packageName }.toMutableList()
        launchOnRuleToModify.value = LaunchOnRule(name = launchOnRuleToModify.value?.name ?: "",
            appList = appList,
            flashcardList = launchOnRuleToModify.value?.flashcardList ?: mutableListOf())
        Log.d("cardflare3",launchOnRuleToModify.toString())
    }
}

fun getCurrentActiveRules():List<LaunchOnRule>{
    var i = mutableListOf<LaunchOnRule>()
    launchOnRules.forEach{rule->
        i.add(rule)
    }
    return i
}

fun getRuleFromApp(appName:String):LaunchOnRule?{
    val activeRules = getCurrentActiveRules()
    activeRules.forEach { rule->
        if(rule.appList.contains(appName)){
            return rule
        }
    }
    return null
}
@Serializable
data class LaunchOnRule(
    var name:String,
    var appList:MutableList<String>,
    var flashcardList : MutableList<Flashcard>
)