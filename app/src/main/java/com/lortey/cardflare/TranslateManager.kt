package com.lortey.cardflare

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

var currentTranslationMap:MutableMap<String,String> = mutableMapOf()
var currentlyChosenTranslationLocation:Location = Location.ASSETS
var currentlyChosenTranslationFilePath:String = "Polski"

//get string translated in current map
fun getTranslation(text:String):String{
    if(text in currentTranslationMap){
        return currentTranslationMap[text]!!
    }else{
        Log.d("cardflareTranslations", "No Translation For: $text")
        currentTranslationMap[text] = text
        return text
    }
}

private val jsonFormat = Json { prettyPrint = true }

//has debuging usecases
public fun saveMap(context: Context, filename:String){
    val jsonString = jsonFormat.encodeToString(currentTranslationMap)
    val file = File(context.getExternalFilesDir(null), "Translations/$filename")
    file.writeText(jsonString)
}

//load selected language from file
fun loadMap(context: Context){
    val state = AppSettings["Language"]?.state as Translations
    currentlyChosenTranslationLocation= if(state.typeOfTranslation == TypeOfTranslation.Default) Location.ASSETS else Location.FILES_DIR
    currentlyChosenTranslationFilePath = state.name
    var jsonString = ""
    when(currentlyChosenTranslationLocation){
        Location.ASSETS ->
            jsonString = context.assets.open("Translations/$currentlyChosenTranslationFilePath").bufferedReader().use { it.readText() }

        Location.FILES_DIR ->
            jsonString = File(context.getExternalFilesDir(null), "Translations/$currentlyChosenTranslationFilePath").readText()
    }

    currentTranslationMap = jsonFormat.decodeFromString<MutableMap<String,String>>(jsonString)
}

//useful for app updates
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
//get all possible Translations , creator provided, custom and ai generated not yet implemented
fun getPossibleTranslations(context: Context):List<Translations>{
    val possibleTranslations:MutableList<Translations> = mutableListOf()
    possibleTranslations.add(Translations("English",TypeOfTranslation.Default,
))
    possibleTranslations.add(Translations("Polski",TypeOfTranslation.Default,
))

    val filenames = listFilesInFilesDir(context,folderName =  "Translations")
    filenames.forEach { name ->
        possibleTranslations.add(Translations(name,TypeOfTranslation.Custom,))
    }

    /*val AiTranslations = getAllSupportedLanguages()

    AiTranslations.forEach { name ->
        possibleTranslations.add(Translations(name,TypeOfTranslation.AI,
            {currentlyChosenTranslationLocation= location.FILES_DIR
                currentlyChosenTranslationFilePath = name}))
    }*/

    return possibleTranslations
}


/*will implement in the future
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

    // Wait for all Translations to complete
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

}*/

//location of Translations
enum class Location{
    ASSETS, FILES_DIR
}

//type of translation: provided by app creator, user custom or ai translated not yet implemented
enum class TypeOfTranslation{
    Default, Custom, AI
}

//translation entry in translation choose screen
@Serializable
data class Translations(
    val name:String,
    val typeOfTranslation: TypeOfTranslation
) : StateData