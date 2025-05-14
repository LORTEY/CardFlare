package com.lortey.cardflare

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.lortey.cardflare.uiRender.launchOnRuleToModify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalTime

//File contains functions to fetch apps installed on device and store user configs of qhat decks to launch on what apps
data class AppInfo(
    val name: String?,
    val packageName: String,
    val icon: Drawable?
)

var launchOnRules:MutableList<LaunchOnRule> = mutableListOf()
const val LaunchOnRuleFile = "LaunchOnRules.txt"
private val jsonFormat = Json { prettyPrint = true }

//get list of apps installed on device
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

// get an ap with closest display name or package name to the search query
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

// load all launch on rules from directory
fun loadLaunchOnRules(context:Context):MutableList<LaunchOnRule>{
    val file = File(context.getExternalFilesDir(null), LaunchOnRuleFile)
    if(!file.exists()){
        saveLaunchOnRules(context, mutableListOf())
    }
    val jsonString = file.bufferedReader().use { it.readText() }
    if (!jsonString.isBlank()) {
        try {
            return jsonFormat.decodeFromString<MutableList<LaunchOnRule>>(jsonString)
        }catch(e:Exception){
            Log.d("cardflare", "Error While deserializing launchOnRules")
        }
    }
    return mutableListOf<LaunchOnRule>()
}

//save currently opened launch on rule
fun saveLaunchOnRules(context:Context, launchOnRules:MutableList<LaunchOnRule>){
    val jsonString = jsonFormat.encodeToString(launchOnRules)
    val file = File(context.getExternalFilesDir(null), LaunchOnRuleFile)
    file.writeText(jsonString)
}

//add app to rule
fun addAppToRule(app:AppInfo){
    if(launchOnRuleToModify.value != null && !((app.packageName in (launchOnRuleToModify.value?.appList
            ?: mutableListOf())))){
        // The rebuild is necessary or else jetpack compose wont refresh
        launchOnRuleToModify.value = LaunchOnRule(name = launchOnRuleToModify.value?.name ?: "",
            appList = (launchOnRuleToModify.value?.appList ?: mutableListOf()).toMutableList().apply{add(app.packageName)},
            flashcardList = launchOnRuleToModify.value?.flashcardList ?: mutableListOf(),
            deckList = launchOnRuleToModify.value?.deckList ?: mutableListOf())
        Log.d("cardflare3",launchOnRuleToModify.toString())
    }
}

//add deck to rule
fun addDeckToRule(deckList:List<Deck>){
        // The rebuild is necessary or else jetpack compose wont refresh
        launchOnRuleToModify.value = LaunchOnRule(name = launchOnRuleToModify.value?.name ?: "",
            appList = launchOnRuleToModify.value?.appList ?: mutableListOf(),
            flashcardList = launchOnRuleToModify.value?.flashcardList ?: mutableListOf(),
            deckList = deckList.map{it.name}.toMutableList())
        Log.d("cardflare3",launchOnRuleToModify.toString())

}

//remove app from rule
fun removeAppFromRule(packageName: String){
    if(launchOnRuleToModify.value != null){
        // The rebuild is necessary or else jetpack compose wont refresh
        val appList:MutableList<String> = (launchOnRuleToModify.value?.appList ?: mutableListOf()).toMutableList().filterNot { it == packageName }.toMutableList()
        launchOnRuleToModify.value = LaunchOnRule(name = launchOnRuleToModify.value?.name ?: "",
            appList = appList,
            flashcardList = launchOnRuleToModify.value?.flashcardList ?: mutableListOf(),
            deckList = launchOnRuleToModify.value?.deckList ?: mutableListOf())
    }
}

//Fetches currently active rules
fun getCurrentActiveRules():List<LaunchOnRule>{
    var i = mutableListOf<LaunchOnRule>()
    val currentTime = getCurrentTime()
    launchOnRules.forEach{rule->
        if(isCurrentTimeInBetweenTimes(rule.activeFrom, rule.activeTo, currentTime)){ // current time is in the time when a rule is supposed to be active
            i.add(rule)
        }
    }
    return i
}

//get an easy to search collection of currently blocked apps
fun generateSetOfBlockedApps(currentActiveRules : List<LaunchOnRule>? = null):HashSet<String>{
    var setOfApps = hashSetOf<String>()
    (currentActiveRules ?: getCurrentActiveRules()).forEach { rule ->
        setOfApps.addAll(rule.appList)
    }
    return setOfApps
}

//get current time
fun getCurrentTime(): TimeValue {
    val currentTime = LocalTime.now()
    val hour = currentTime.hour
    val minute = currentTime.minute

    return TimeValue(hour, minute)
}

//compare times
fun compareTimes(timeA:TimeValue, timeB:TimeValue):Byte{ // -1 first is bigger 0 they are equal 1 second is bigger
    if(timeA.hour == timeB.hour && timeA.minute == timeB.minute){
        return 0
    }
    if(timeA.hour > timeB.hour || (timeA.hour == timeB.hour && timeA.minute > timeB.minute)){
        return -1
    }
    return 1
}

//checks whether a time is between two other times including times exceeding midnight. Used to determine if rule is currently active
fun isCurrentTimeInBetweenTimes(timeStart:TimeValue?, timeEnd:TimeValue?, currentTime:TimeValue):Boolean{
    if(timeStart == null || timeEnd == null){
        return true
    }
    if(compareTimes(timeStart, timeEnd) == (1).toByte()){
        if(compareTimes(timeStart, currentTime) == (1).toByte() && compareTimes(timeEnd, currentTime) == (-1).toByte()){
            return true
        }else{
            return false
        }
    }
    if(compareTimes(timeStart, currentTime) == (1).toByte() || compareTimes(timeEnd, currentTime) == (-1).toByte()){
        return true
    }else{
        return false
    }
}

/*Will implement in the future
fun getFlashcards(numberOfFlashcards:Int, decks: List<Deck>):List<Flashcard>?{
    val randomFlashcards = mutableListOf<Flashcard>()
    var iterator: Int = 0
    if(numberOfFlashcards > 0 && decks.size > 0){
        while(iterator < numberOfFlashcards){
            val randomFlashcard:Flashcard? = decks.random().cards.randomOrNull()
            if (randomFlashcard != null){
                iterator += 1
                randomFlashcards.add(randomFlashcard)
            }
        }
        return randomFlashcards
    }
    return null
}*/

//Get Aa rule that is now active that should run on currently used app
fun getRuleFromApp(appName:String):LaunchOnRule?{
    val activeRules = getCurrentActiveRules()
    activeRules.forEach { rule->
        if(rule.appList.contains(appName)){
            return rule
        }
    }
    return null
}

//Checks if any currently active rule should run at phone unlock
fun anyRuleSetToRunAtUnlock():LaunchOnRule?{
    val activeRules = getCurrentActiveRules()
    activeRules.forEach { rule->
        if(rule.unlockedCatch){
            return rule
        }
    }
    return null
}

//Converts deck filenames to decks
fun deckNamesToDeckList(deckNames:List<String>,context: Context):List<Deck>{
    val loadedDecks = mutableListOf<Deck>()
    deckNames.forEach { filename ->
        val currentData = loadData(context, filename)
        if(currentData.size == 1){ //expected length
            loadedDecks.add(currentData[0])
        }
    }
    return loadedDecks
}

//Launch on rule
@Serializable
data class LaunchOnRule(
    var name:String,
    var appList:MutableList<String>,
    var flashcardList : MutableList<Flashcard>,
    var deckList : MutableList<String>,
    var activeFrom: TimeValue? = null,
    var activeTo: TimeValue? = null,
    var unlockedCatch:Boolean = false
)

//I dcant serilize instant
@Serializable
data class TimeValue(
    val hour: Int,   // 0-23
    val minute: Int  // 0-59
)