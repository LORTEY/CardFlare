package com.lortey.cardflare

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

public var tags:MutableList<Tag> = mutableListOf()
private val jsonFormat = Json { prettyPrint = true }
private const val tagsFile = "tags" // tags file name

//load tags from file
fun loadTags(context:Context){
    val file = File(context.getExternalFilesDir(null), tagsFile)
    if(!file.exists()){
        saveTags(context)
    }
    val jsonString = file.bufferedReader().use { it.readText() }
    if (!jsonString.isBlank()) {
        try {
            tags = jsonFormat.decodeFromString<MutableList<Tag>>(jsonString)
        }catch(e:Exception){
            Log.d("cardflare", "Error While deserializing launchOnRules")
        }
    }
}

//save tags to file
fun saveTags(context:Context){
    val jsonString = jsonFormat.encodeToString(tags)
    val file = File(context.getExternalFilesDir(null), tagsFile)
    file.writeText(jsonString)
}

//random hex color for new tag will have more use cases in the future
private fun getRandomHexColor(): String {
    val random = java.util.Random()
    val color = random.nextInt(0xFFFFFF + 1) // Range: 0 to 16777215 (0xFFFFFF)
    return String.format("#%06x", color)
}

//add new tag with name
fun addTag(context: Context, name:String){
    tags.add(Tag(tagName =  name, color = getRandomHexColor()))
    saveTags(context)
}

//tag dataclass
@Serializable
public data class Tag(
    val tagName: String,
    val color: String
    )