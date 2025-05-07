package com.lortey.cardflare.uiRender

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.lortey.cardflare.AppSettings
import com.lortey.cardflare.Category
import com.lortey.cardflare.Chooser
import com.lortey.cardflare.R
import com.lortey.cardflare.SettingEntry
import com.lortey.cardflare.SettingsType
import com.lortey.cardflare.getTranslation
import com.lortey.cardflare.saveSettings
import com.lortey.cardflare.translations
import com.lortey.cardflare.updateSetting


// The settings screen
@Composable
fun SettingsMenu(navController: NavHostController, context: Context) {
    val appSettings = remember { AppSettings }

    Log.d("cardflareSetts", appSettings.toString())
    DisposableEffect(Unit) {
        onDispose {
            saveSettings(context)
        }
    }
    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // Correctly loop through categories
        Category.entries.forEach { category ->
            val filtered = appSettings.values.filter { it.category == category }

            if (filtered.isNotEmpty()) {
                Log.d("FilteredCardFlare", filtered.toString())

                item {
                    Text(
                        text = getTranslation(category.toString().replace("_", " ")),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                }

                items(filtered) { setting ->
                    SettingsEntryComposable(setting, appSettings, context, navController)
                }
            }
        }
    }
}

@Composable
fun SettingsEntryComposable(setting: SettingEntry, appSettings: Map<String, SettingEntry>, context: Context, navController: NavController) {
    var openPopup by remember { mutableStateOf(false) }
    if (openPopup) {
        if(!setting.description.isNullOrEmpty()) {
            PopUp(getTranslation(setting.name), getTranslation(setting.description), { openPopup = !openPopup }, openPopup)
        }
        /*Popup(
            alignment = Alignment.TopStart,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.inverseOnSurface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(16.dp)
                    .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(setting.description!!, style = MaterialTheme.typography.titleMedium,color = MaterialTheme.colorScheme.onBackground)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { openPopup = false }) {
                        Text("Close")
                    }
                }
            }
        }*/
    }
    if ((setting.type == SettingsType.BOOLEAN && setting.customChooser == Chooser.NonSpecified) || setting.customChooser == Chooser.Switch) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.info),
                contentDescription = "info",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable { openPopup = true }
                    .padding(vertical = 10.dp)
            )
            Text(getTranslation(setting.name), modifier = Modifier.padding(vertical = 10.dp),color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.weight(1f))
            val state = remember { mutableStateOf(setting.state as Boolean) }
            Switch(
                checked = state.value,
                onCheckedChange = { newValue ->
                    state.value = newValue // Update local state
                    updateSetting(setting.name, newValue) // Update appSettings state
                }
            )
        }
    }else if ((setting.type == SettingsType.SLIDER && setting.customChooser == Chooser.NonSpecified) || setting.customChooser == Chooser.Slider) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)){
            val state = remember { mutableStateOf(setting.state as Float) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { openPopup = true }
                        .padding(vertical = 10.dp)
                )
                Text(getTranslation(setting.name), modifier = Modifier.padding(vertical = 10.dp))
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface), horizontalArrangement = Arrangement.SpaceEvenly){
                    Text(
                        text = state.value.toString(),
                    )
                }
            }
                Slider(
                    value = state.value,
                    onValueChange = { newValue ->
                        state.value = newValue
                        updateSetting(setting.name, newValue)
                    },
                    valueRange = (setting.sliderData?.get("from") ?: 0f)..(setting.sliderData?.get("to") ?: 100f),
                    steps = (setting.sliderData?.get("steps") ?: 100).toInt(),
                    modifier = Modifier.fillMaxWidth()
                )
        }
    }else if ((setting.type == SettingsType.CHOOSE && setting.customChooser == Chooser.NonSpecified) /*|| setting.customChooser == Chooser.Slider*/ ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)) {
            val state = remember { mutableStateOf(setting.state) }
            var expanded by remember { mutableStateOf(false) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { openPopup = true }
                        .padding(vertical = 10.dp)
                )
                Text(getTranslation(setting.name), modifier = Modifier.padding(vertical = 10.dp))
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .background(MaterialTheme.colorScheme.inverseOnSurface),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val keyValue = setting.dropDownMenuEntries!!.entries.firstOrNull { it.value == setting.state }?.key
                    Text(
                        text = getTranslation(keyValue ?: ""),
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(200.dp).background(MaterialTheme.colorScheme.inverseOnSurface)
                    ) {
                        setting.dropDownMenuEntries.forEach { (key, value) ->
                            DropdownMenuItem(
                                text = { Text(getTranslation(key)) },
                                onClick = {
                                    updateSetting(setting.name, value)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }else if ((setting.type == SettingsType.CHOOSE && setting.customChooser == Chooser.NonSpecified) /*|| setting.customChooser == Chooser.Slider*/ ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            val state = remember { mutableStateOf(setting.state) }
            var expanded by remember { mutableStateOf(false) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { openPopup = true }
                        .padding(vertical = 10.dp)
                )
                Text(getTranslation(setting.name), modifier = Modifier.padding(vertical = 10.dp))
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .background(MaterialTheme.colorScheme.inverseOnSurface),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val keyValue =
                        setting.dropDownMenuEntries!!.entries.firstOrNull { it.value == setting.state }?.key
                    Text(
                        text = getTranslation(keyValue ?: ""),
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(200.dp)
                            .background(MaterialTheme.colorScheme.inverseOnSurface)
                    ) {
                        setting.dropDownMenuEntries.forEach { (key, value) ->
                            DropdownMenuItem(
                                text = { Text(getTranslation(key)) },
                                onClick = {
                                    updateSetting(setting.name, value)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }else if ((setting.type == SettingsType.ACTION) /*|| setting.customChooser == Chooser.Slider*/ ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable { setting.navChoose?.let { navController.navigate(it) } }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { openPopup = true }
                        .padding(vertical = 10.dp)
                )
                Text(getTranslation(setting.name), modifier = Modifier.padding(vertical = 10.dp))
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .background(MaterialTheme.colorScheme.inverseOnSurface),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = getTranslation(deserializeName(setting.stateDataclass,setting.state)),

                    )
                }
            }
        }
    }
}

fun deserializeName(className: String, input: Any): String {
    return when (className) {
        "translations" -> (input as translations).name
        else -> throw IllegalArgumentException("Unknown class: $className")
    }
}

