package com.lortey.cardflare

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


// This file handles settings of the app
fun updateSetting(key: String, newState: Any) {//the settings map is rebuilt in order to force jetpack compose to update on change
    val currentEntry = AppSettings[key] ?: return // ✅ Prevents modifying a non-existent key
    AppSettings[key] = currentEntry.copy(state = newState) // ✅ Triggers recomposition
    Log.d("cardflare", AppSettings.toString())
}

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

fun loadSettings(context: Context) {
    val file = File(context.getExternalFilesDir(null), "settings.json")
    if (file.exists()) {
        val json = Json { ignoreUnknownKeys = true }
            val map = json.decodeFromString<Map<String,String>>(file.readText())
        decodeSettings(map = map)
    }
}
private fun decodeSettings(map: Map<String,String>){
    map.forEach{(key, value) ->
        if(AppSettings[key]?.type != null) {
            val loadedState = settingTypeToType(value, AppSettings[key]!!.type, AppSettings[key])
            AppSettings[key]!!.state = loadedState
        }
    }
    updateSetting("Use Dynamic Color", AppSettings["Use Dynamic Color"]!!.state)
}
private fun encodeSettings():Map<String,String> {
    val mappedSettings: MutableMap<String, String> = mutableMapOf()

    AppSettings.forEach { (key, valueOfSetting) ->
        if (AppSettings.get(key)?.type != SettingsType.CHOOSE ) {
            mappedSettings[key] = valueOfSetting.state.toString()
        }else{
            val stringOfEntry = valueOfSetting.dropDownMenuEntries!!.entries.firstOrNull { it.value == valueOfSetting.state }?.key
            mappedSettings[key] = stringOfEntry.toString()
        }
    }
    return mappedSettings
}
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

    }
}
val AppSettings = mutableStateMapOf(
    "Choose Theme" to SettingEntry(
        category = Category.Appearance,
        name = "Choose Theme",
        description = "Choose what theme should app be in Light, Dark or automatically use current system theme.",
        type = SettingsType.CHOOSE,
        state = Themes.AUTO,
        dropDownMenuEntries = mapOf("Light Mode" to Themes.LIGHT, "Dark Mode" to Themes.DARK, "Auto" to Themes.AUTO)
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
    "Flip Flashcard Right Wrong Answer" to SettingEntry(
        category = Category.Preferences,
        name = "Flip Flashcard Right Wrong Answer",
        description = "If enabled, The wrong answer option will be on the left and right answer will be on the right",
        type = SettingsType.BOOLEAN,
        state = false
    ),
    "Bin Auto Empty Time" to SettingEntry(
        category = Category.Appearance,
        name = "Bin Auto Empty Time",
        description = "The Time it takes the been to remove a flashcard added to it.",
        type = SettingsType.CHOOSE,
        state = Time.DEBUG,
        dropDownMenuEntries = mapOf("1 Day" to Time.DAY, "debud" to Time.DEBUG,"One Week" to Time.WEEK, "Two Weeks" to Time.TWO_WEEKS,"One Month" to Time.MONTH, "Two Months" to Time.TWO_MONTHS)
    )
)
public data class SettingEntry(
    val category: Category,
    val name: String,
    val description: String?,
    val type: SettingsType,
    var state: Any,
    val customChooser: Chooser? = Chooser.NonSpecified,
    val sliderData: Map<String,Float>? = null,
    val grayedOutWhen: Boolean = false,
    val dropDownMenuEntries: Map<String,Any>? = null
){
    /*init {
        when (type) {
            SettingsType.BOOLEAN -> require(state is Boolean) {"state for type BOOLEAN must be a Boolean"}
            SettingsType.SLIDER -> require(state is Int) {"state for type SLIDER must be an Int"}
            SettingsType.COLOR_PICKER -> require(state is String) {"state for type COLOR_PICKER must be a String"}
        }
    }*/
}
public enum class Themes{
    DARK, LIGHT, AUTO
}

public enum class Time{
    DAY, WEEK, TWO_WEEKS, MONTH, TWO_MONTHS, DEBUG
}

public enum class SettingsType {
    BOOLEAN, SLIDER, COLOR_PICKER, CHOOSE
}

public enum class Category {
    Appearance, Preferences, Other
}

public enum class Chooser{
    NonSpecified, Switch, Slider
}