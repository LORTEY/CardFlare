package com.example.cardflare

import androidx.compose.runtime.mutableStateMapOf

// This file handles settings of the app
fun updateSetting(key: String, newState: Any) {
    val currentEntry = AppSettings[key] ?: return // ✅ Prevents modifying a non-existent key
    AppSettings[key] = currentEntry.copy(state = newState) // ✅ Triggers recomposition
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