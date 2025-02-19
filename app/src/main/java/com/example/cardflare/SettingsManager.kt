package com.example.cardflare

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
// This file handles settings of the app
public var AppSettings = mapOf(
    "USER_ENABLED_DYNAMIC_COLORS" to SettingEntry(
        name = "USER_ENABLED_DYNAMIC_COLORS",
        description = "If enabled the app will use the dynamic colors of the system",
        type = SettingsType.BOOLEAN,
        state = false)
)
public data class SettingEntry(
    val name: String,
    val description: String?,
    val type: SettingsType,
    var state: Boolean
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