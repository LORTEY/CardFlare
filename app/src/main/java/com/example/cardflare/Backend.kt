package com.example.cardflare

import android.content.Context
import android.util.Log
import com.example.cardflare.uiRender.reloadDecks
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

// This file contains all the functions used to load manage and store databases
fun copyAssetsToFilesDir(context: Context) {
    val assetManager = context.assets
    val flashcardDirectory = File(context.getExternalFilesDir(null), "FlashcardDirectory") // Use getExternalFilesDir() here

    // Create the directory if it doesn't exist
    if (!flashcardDirectory.exists()) {
        flashcardDirectory.mkdirs()
        Log.d("FilesDir", "FlashcardDirectory created.")
    }

    try {
        // List the files in the "FlashcardDirectory" within assets folder
        val assetFiles = assetManager.list("FlashcardDirectory")

        // If there are files inside this folder, copy them to external filesDir
        assetFiles?.forEach { fileName ->
            val inputStream: InputStream = assetManager.open("FlashcardDirectory/$fileName")
            val outputFile = File(flashcardDirectory, fileName)
            val outputStream: OutputStream = FileOutputStream(outputFile)

            // Copy data from assets to external filesDir
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            Log.d("FilesDir", "Copied file: $fileName to $flashcardDirectory")
        }
    } catch (e: Exception) {
        Log.e("FilesDir", "Error copying assets: ${e.message}")
    }
}

// lists all files in FlashcardDirectory
fun listFilesInFilesDir(context: Context, folderName: String = "FlashcardDirectory"): Array<String> {
    try {
        val flashcardDirectory = File(context.getExternalFilesDir(null), folderName) // Use getExternalFilesDir() here
        if (flashcardDirectory.exists()) {
            val fileNames = flashcardDirectory.list()
            if (fileNames != null) {
                return fileNames
            }
        } else {
            Log.d("FilesDir", "No files found in the files directory.")
        }
    } catch (e: Exception) {
        Log.e("FilesDir", "Error reading files directory: ${e.message}")
    }
    return arrayOf()
}

fun readFileFromFilesDir(fileName: String, context: Context): String {
    return try {
        val file = File(context.getExternalFilesDir(null), "FlashcardDirectory/$fileName") // Use getExternalFilesDir() here
        file.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e("FilesDir", "Error reading file $fileName: ${e.message}")
        ""
    }
}
private val jsonFormat = Json { prettyPrint = true }

// Save deck to file
fun saveDeck(context: Context, deck: Deck, filename: String, folderName: String = "FlashcardDirectory") {
    val jsonString = jsonFormat.encodeToString(deck)
    val file = File(context.getExternalFilesDir(null), "$folderName/$filename")
    file.writeText(jsonString)
}

// Load deck from file
fun loadData(context: Context, filename: String): Array<Deck> {
    var fileNames: Array<String> = arrayOf()
    if(filename.isBlank()) {
        fileNames = listFilesInFilesDir(context)
    }else{
        fileNames = arrayOf(filename)
    }
    val decks = mutableListOf<Deck>()
    fileNames.forEach { fileName ->
            val jsonString = readFileFromFilesDir(fileName, context)
            Log.d("cardflare2",jsonString)
            decks.add(jsonFormat.decodeFromString<Deck>(jsonString))

    }
    return decks.toTypedArray()
}

fun addFlashcard(fileName: String, flashcardContent: Flashcard, context: Context, folderName:String="FlashcardDirectory") {
    var readData = loadData(filename = fileName, context = context)
    readData[0].cards.add(flashcardContent.copy(id = readData[0].minimalID + 1)) //assigns an id to flashcard and adds it
    readData[0].minimalID += 1
    saveDeck(context, readData[0], fileName,folderName)
}

fun addDeck(context: Context, filename: String, deck:Deck? = null, folderName: String = "FlashcardDirectory") {
    Log.d("CardFlareLine", context.getExternalFilesDir(null)?.absolutePath ?: "No Path")
    val flashcardDirectory = File(context.getExternalFilesDir(null), folderName) // Use getExternalFilesDir() here
    if (!flashcardDirectory.exists()) {
        flashcardDirectory.mkdirs() // Create folder if it doesn't exist
    }

    val file = File(flashcardDirectory, filename) // File inside the directory
    if (!file.exists()) {
        file.createNewFile() // Create new file
        val currentTimeMillis = System.currentTimeMillis()
        val currentTimeTenSec = ((currentTimeMillis / 1000) / 10).toInt() * 10
        var jsonString: String
        if(deck == null){
             jsonString = jsonFormat.encodeToString(getDeck(fileName = filename, date_made = currentTimeTenSec, last_edited = currentTimeTenSec))
        }else{
             jsonString = jsonFormat.encodeToString(deck)
        }

        file.writeText(jsonString)
        reloadDecks(context)
    } else {
        Log.d("FilesDir", flashcardDirectory.list().toList().toString())
    }
}

public fun sortDecks(searchQuery: String, decks: Array<Deck>, sortType: SortType, isAscending: Boolean): List<Deck>{
        // Enumerating tags
        var tagsRequired = mutableListOf<String>()
        if ('#' in searchQuery){
            searchQuery.split(' ').forEach(){ word ->
                if (word.length > 0){
                    if(word[0] == '#'){
                        tagsRequired.add(word)
                    }
                }
            }
        }
        var searchTerm: String
        try {
            searchTerm = searchQuery.split(" & ")[0]
        }catch(e:Exception){
            searchTerm = searchQuery
        }

        var decksQualified = mutableListOf<Deck>()

        decks.forEach() { currentDeck->
            if ((tagsRequired.any { it in currentDeck.tags } || tagsRequired.size == 0) and (searchTerm in currentDeck.name || searchTerm.length == 0)){
                decksQualified.add(currentDeck)
            }
        }

        when (sortType){
            SortType.ByName -> decksQualified = decksQualified.sortedWith(compareBy<Deck> { it.name }.thenBy{ it.date_made}).toMutableList()
            SortType.ByCreationDate -> decksQualified = decksQualified.sortedWith(compareBy<Deck> { it.date_made }.thenBy{ it.name}).toMutableList()
            SortType.ByLastEdited -> decksQualified = decksQualified.sortedWith(compareBy<Deck> { it.last_edited }.thenBy{ it.name}).toMutableList()
        }

        if (!isAscending){
            return decksQualified.reversed()
        }

        return decksQualified
    }


fun MoveCardToBin(context: Context, fileName: String, card: Flashcard) {
    val BinDir = File(
        context.getExternalFilesDir(null),
        "BinDirectory"
    ) // Use getExternalFilesDir() here
    if (!BinDir.exists()) {
        BinDir.mkdirs() // Create folder if it doesn't exist
    }
    val file = File(BinDir, "$fileName") // File inside the directory
    if (!file.exists()) {
        file.createNewFile() // Create new file
        addDeck(context = context, filename = fileName, folderName = "BinDirectory")
        reloadDecks(context)
    }
    addFlashcard(fileName = fileName, flashcardContent = card, context = context, folderName = "BinDirectory")
    removeFlashcard(context = context, fileName = fileName, card = card)
}
fun removeFlashcard(context: Context, fileName: String, card: Flashcard) {
    val fileData = loadData(context = context, filename = fileName)
    val iterator = fileData[0].cards.iterator()
    while (iterator.hasNext()) {
        if (iterator.next().id == card.id) {
            iterator.remove()
        }
    }
    saveDeck(context = context,fileData[0], filename = fileName)
}

fun removeMultiple(context: Context,fileName: String, cards:List<Flashcard>){
    cards.forEach(){element ->
        MoveCardToBin(context = context, fileName = fileName, card = element)
    }
}
fun getDeck(fileName: String ="", date_made:Int = 0, last_edited:Int = 0, tags: MutableList<String> = mutableListOf(),cards: MutableList<Flashcard> = mutableListOf() ):Deck{
    return Deck(fileName,date_made,last_edited, tags,cards)
}


public enum class SortType{
    ByName,
    ByCreationDate,
    ByLastEdited
}

@Serializable
public data class Flashcard(
    val id: Int,
    val SideA: String,
    val SideB: String)

@Serializable
public data class Deck(
    val name: String,
    val date_made: Int,
    val last_edited: Int,
    val tags: MutableList<String>,
    val cards: MutableList<Flashcard>,
    var minimalID: Int = 0)
