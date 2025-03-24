package com.example.cardflare

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import java.io.File


// This file handles settings of the app
fun updateSetting(key: String, newState: Any) {//the settings map is rebuilt in order to force jetpack compose to update on change
    val currentEntry = AppSettings[key] ?: return // ✅ Prevents modifying a non-existent key
    AppSettings[key] = currentEntry.copy(state = newState) // ✅ Triggers recomposition
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
            val loadedState = settingTypeToType(value, AppSettings[key]!!.type)
            AppSettings[key]!!.state = loadedState
        }
    }
    updateSetting("Use Dynamic Color", AppSettings["Use Dynamic Color"]!!.state)
}
private fun encodeSettings():Map<String,String> {
    val mappedSettings: MutableMap<String, String> = mutableMapOf()

    AppSettings.forEach { (key, value) ->
        mappedSettings[key] = value.state.toString()
    }
    return mappedSettings
}
private fun settingTypeToType(input:String, type:SettingsType):Any{
    when (type){
        SettingsType.BOOLEAN -> {
            try{
                return input.toBoolean()
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
        state = 300f
    ),
    "Flip Flashcard Right Wrong Answer" to SettingEntry(
        category = Category.Preferences,
        name = "Flip Flashcard Right Wrong Answer",
        description = "If enabled, The wrong answer option will be on the left and right answer will be on the right",
        type = SettingsType.BOOLEAN,
        state = false
    )
)
public data class SettingEntry(
    val category: Category,
    val name: String,
    val description: String?,
    val type: SettingsType,
    var state: Any,
    val customChooser: Chooser? = Chooser.NonSpecified
){
    /*init {
        when (type) {
            SettingsType.BOOLEAN -> require(state is Boolean) {"state for type BOOLEAN must be a Boolean"}
            SettingsType.SLIDER -> require(state is Int) {"state for type SLIDER must be an Int"}
            SettingsType.COLOR_PICKER -> require(state is String) {"state for type COLOR_PICKER must be a String"}
        }
    }*/
}

public enum class SettingsType {
    BOOLEAN, SLIDER, COLOR_PICKER
}

public enum class Category {
    Appearance, Preferences
}

public enum class Chooser{
    NonSpecified, Switch, Slider
}