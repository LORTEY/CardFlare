package com.lortey.cardflare

import android.content.Context
import android.util.Log
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

var currentTranslationMap:MutableMap<String,String> = mutableMapOf()
var currentlyChosenTranslationLocation:location = location.ASSETS
var currentlyChosenTranslationFilePath:String = "Polski"
fun getTranslation(text:String):String{
    if(text in currentTranslationMap){
        return currentTranslationMap[text]!!
    }else{
        Log.d("cardflareTranslations", "No Translation For: $text")
        return text
    }
}

private val jsonFormat = Json { prettyPrint = true }

private fun saveMap(context: Context, filename:String){
    val jsonString = jsonFormat.encodeToString(currentTranslationMap)
    val file = File(context.getExternalFilesDir(null), "Translations/$filename")
    file.writeText(jsonString)
}

fun loadMap(context: Context){
    val state = AppSettings["Language"]?.state as translations
    currentlyChosenTranslationLocation= if(state.typeOfTranslation == typeOfTranslation.Default) location.ASSETS else location.FILES_DIR
    currentlyChosenTranslationFilePath = state.name
    var jsonString = ""
    when(currentlyChosenTranslationLocation){
        location.ASSETS ->
            jsonString = context.assets.open("Translations/$currentlyChosenTranslationFilePath").bufferedReader().use { it.readText() }

        location.FILES_DIR ->
            jsonString = File(context.getExternalFilesDir(null), "Translations/$currentlyChosenTranslationFilePath").readText()
    }

    currentTranslationMap = jsonFormat.decodeFromString<MutableMap<String,String>>(jsonString)
}

fun remap(context: Context){
    val fromTo:Map<String,String> = mapOf()
    fromTo.forEach{key, value ->
        if(key in currentTranslationMap){
            currentTranslationMap[value] = currentTranslationMap[key]!!
            currentTranslationMap.remove(key)
        }

    }
    //saveMap(filename = str)
}

fun getPossibleTranslations(context: Context):List<translations>{
    val possibleTranslations:MutableList<translations> = mutableListOf()
    possibleTranslations.add(translations("English",typeOfTranslation.Default,
))
    possibleTranslations.add(translations("Polski",typeOfTranslation.Default,
))

    val filenames = listFilesInFilesDir(context,folderName =  "Translations")
    filenames.forEach { name ->
        possibleTranslations.add(translations(name,typeOfTranslation.Custom,))
    }

    /*val AiTranslations = getAllSupportedLanguages()

    AiTranslations.forEach { name ->
        possibleTranslations.add(translations(name,typeOfTranslation.AI,
            {currentlyChosenTranslationLocation= location.FILES_DIR
                currentlyChosenTranslationFilePath = name}))
    }*/

    return possibleTranslations
}


suspend fun createCustomTranslation(context: Context, desiredLanguage:String){
    val translator = createTranslator(sourceLang = "English", targetLang = desiredLanguage)
    loadMap(context)
    val translatedUI:MutableMap<String,String> = mutableMapOf()
    val translationJobs = currentTranslationMap.map { (key, value) ->
        CoroutineScope(Dispatchers.IO).async {
            try {
                val translatedText = translator.translate(value)
                key to translatedText
            } catch (e: Exception) {
                key to "Translation failed: ${e.message}"
            }
        }
    }

    // Wait for all translations to complete
    val results = translationJobs.awaitAll()

    // Put all results in the map
    results.forEach { (key, value) ->
        try {
            require(value is String)
            translatedUI[key] = value
        }catch(e:Exception){

        }
    }
}


fun translateTextUi(text: String, translator: Translator, onResult: (String) -> Unit){
        Log.d("CardflareTranslator","hii")
        translator.translate(text)
            .addOnSuccessListener { translatedText -> onResult(translatedText) }
            .addOnFailureListener { e -> onResult("Translation failed: ${e.message}") }

}
enum class location{
    ASSETS, FILES_DIR
}

enum class typeOfTranslation{
    Default, Custom, AI
}

@Serializable
data class translations(
    val name:String,
    val typeOfTranslation: typeOfTranslation
) : StateData