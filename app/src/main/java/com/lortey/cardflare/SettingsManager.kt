package com.lortey.cardflare

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


// This file handles settings of the app
fun updateSetting(key: String, newState: Any) {//the settings map is rebuilt in order to force jetpack compose to update on change
    val currentEntry = AppSettings[key] ?: return // Prevents modifying a non-existent key
    AppSettings[key] = currentEntry.copy(state = newState) // Triggers recomposition
    Log.d("cardflare", AppSettings.toString())
}
//save settings to file run on closing the setting screen
fun saveSettings(context: Context){
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    // Convert map to JSON string
    val jsonString = json.encodeToString(encodeSettings())
    // Save to file
    val file = File(context.getExternalFilesDir(null), "settings.json")
    file.writeText(jsonString)
}

//load settings from file
fun loadSettings(context: Context) {
    val file = File(context.getExternalFilesDir(null), "settings.json")
    if (file.exists()) {
        val json = Json { ignoreUnknownKeys = true }
            val map = json.decodeFromString<Map<String,String>>(file.readText())
        decodeSettings(map = map)
    }
}

//decode savefile to usable setting states
private fun decodeSettings(map: Map<String,String>){
    map.forEach{(key, value) ->
        if(AppSettings[key]?.type != null) {
            val loadedState = settingTypeToType(value, AppSettings[key]!!.type, AppSettings[key])
            AppSettings[key]!!.state = loadedState
        }
    }
    updateSetting("Use Dynamic Color", AppSettings["Use Dynamic Color"]!!.state)
}

//encode setting names and states to savable map
private fun encodeSettings():Map<String,String> {
    val mappedSettings: MutableMap<String, String> = mutableMapOf()
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    AppSettings.forEach { (key, valueOfSetting) ->
        if (AppSettings.get(key)?.type == SettingsType.CHOOSE ) {
            val stringOfEntry = valueOfSetting.dropDownMenuEntries!!.entries.firstOrNull { it.value == valueOfSetting.state }?.key
            mappedSettings[key] = stringOfEntry.toString()

        }else if (AppSettings.get(key)?.type == SettingsType.ACTION ) {
            mappedSettings[key] = json.encodeToString(valueOfSetting.state as Translations)
        }else{
            mappedSettings[key] = valueOfSetting.state.toString()
        }
    }
    return mappedSettings
}

//convert datatypes of saved settings from string
private fun settingTypeToType(input:String, type:SettingsType, settingEntry:SettingEntry? = null):Any{
    when (type){
        SettingsType.BOOLEAN -> {
            try{
                return input.toBoolean()
            }catch (e: Exception){
                return ""
            }
        }
        SettingsType.CHOOSE -> {
            try{
                return settingEntry!!.dropDownMenuEntries!!.get(input)!!
            }catch (e: Exception){
                return ""
            }
        }
        SettingsType.SLIDER -> {
            try{
                return input.toFloat()
            }catch (e: Exception){
                return ""
            }
        }
        SettingsType.COLOR_PICKER -> {
            try{
                return input
            }catch (e: Exception){
                return ""
            }
        }
        SettingsType.ACTION -> {
            //try{
                val x = deserializeDataclass(input, settingEntry?.stateDataclass ?: "")
                Log.d("cardflaressss", x.toString())
                return x

        }

    }
}

//convert serialized data class
fun deserializeDataclass(input:String, type:String):Any{
    val json = Json { ignoreUnknownKeys = true }
    when(type){
        "translations" ->
            return json.decodeFromString<Translations>(input)
        else ->
            return "error"
    }
}
@Serializable
sealed interface StateData

//Settings
val AppSettings = mutableStateMapOf(
    "Choose Theme" to SettingEntry(
        category = Category.Appearance,
        name = "Choose Theme",
        description = "Choose what theme should app be in: Light, Dark or automatically use current system theme.",
        type = SettingsType.CHOOSE,
        state = Themes.AUTO,
        dropDownMenuEntries = mapOf("Light Theme" to Themes.LIGHT, "Dark Theme" to Themes.DARK, "Auto" to Themes.AUTO)
    ),
    "Use Dynamic Color" to SettingEntry(
        category = Category.Appearance,
        name = "Use Dynamic Color",
        description = "If enabled, the app will use dynamic colors",
        type = SettingsType.BOOLEAN,
        state = false
    ),
    "Flashcard Swipe Threshold" to SettingEntry(
        category = Category.Preferences,
        name = "Flashcard Swipe Threshold",
        description = "The distance a flashcard needs to be swiped to be count as a wrong or right answer",
        type = SettingsType.SLIDER,
        state = 300f,
        sliderData = mapOf("from" to 0f, "to" to 800f, "steps" to 15f)
    ),
   /* "Flip Flashcard Right Wrong Answer" to SettingEntry(
        category = Category.Preferences,
        name = "Flip Flashcard Right Wrong Answer",
        description = "If enabled, The wrong answer option will be on the left and right answer will be on the right",
        type = SettingsType.BOOLEAN,
        state = false
    ),*/
    "Do Not Show System Apps" to SettingEntry(
        category = Category.Other,
        name = "Do Not Show System Apps",
        description = "If enabled, You will not be able to find apps flagged as system apps when setting a LaunchOn rule.",
        type = SettingsType.BOOLEAN,
        state = true
    ),
    "Bin Auto Empty Time" to SettingEntry(
        category = Category.Bin,
        name = "Bin Auto Empty Time",
        description = "The Time it takes the bin to remove a flashcard added to it.",
        type = SettingsType.CHOOSE,
        state = Time.MONTH,
        dropDownMenuEntries = mapOf("One Day" to Time.DAY,"One Week" to Time.WEEK, "Two Weeks" to Time.TWO_WEEKS,"One Month" to Time.MONTH, "Two Months" to Time.TWO_MONTHS)
    )
    ,
    "Language" to SettingEntry(
        category = Category.Preferences,
        name = "Language",
        description = "App's Language.",
        type = SettingsType.ACTION,
        state = Translations("Polski",TypeOfTranslation.Default),
        stateDataclass = "translations",
        navChoose = "language_choose"
        )
)

//Setting
data class SettingEntry(
    val category: Category,
    val name: String,
    val description: String?,
    val type: SettingsType, // type of setting
    var state: Any, // state
    val customChooser: Chooser? = Chooser.NonSpecified, // should have a non default choosing method for type
    val sliderData: Map<String,Float>? = null, // used for sliders
    val grayedOutWhen: Boolean = false, // setting disabled when
    val dropDownMenuEntries: Map<String,Any>? = null,
    val runtimeRun:Boolean = false, // should be executed when app is tun
    val navChoose :String?  = null, // to choose a setting navigate to another screen
    val stateDataclass: String = "" //type of dataclass of saved state used for deserialization
)

//Used for app themes
enum class Themes{
    DARK, LIGHT, AUTO
}

//used for bin auto empty time
enum class Time{
    DAY, WEEK, TWO_WEEKS, MONTH, TWO_MONTHS, DEBUG
}


enum class SettingsType {
    BOOLEAN, SLIDER, COLOR_PICKER, CHOOSE, ACTION
}

enum class Category {
    Appearance, Preferences, Bin, Other
}

enum class Chooser{
    NonSpecified, Switch, Slider, Action
}