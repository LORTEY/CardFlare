package com.example.cardflare

import android.content.Context
import android.util.Log
import com.example.cardflare.ui.theme.reloadDecks
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


// This file contains all the functions used to load manage and store databases
fun copyAssetsToFilesDir(context: Context) {
    val assetManager = context.assets
    val flashcardDirectory = File(context.filesDir, "FlashcardDirectory")

    // Create the directory if it doesn't exist
    if (!flashcardDirectory.exists()) {
        flashcardDirectory.mkdirs()
        Log.d("FilesDir", "FlashcardDirectory created.")
    }
//not gonna lie was busy today and I didn't have time to do anything but I need to push
    try {
        // List the files in the "FlashcardDirectory" within assets folder
        val assetFiles = assetManager.list("FlashcardDirectory")

        // If there are files inside this folder, copy them to filesDir
        assetFiles?.forEach { fileName ->
            val inputStream: InputStream = assetManager.open("FlashcardDirectory/$fileName")
            val outputFile = File(flashcardDirectory, fileName)
            val outputStream: OutputStream = FileOutputStream(outputFile)

            // Copy data from assets to filesDir
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
    fun listFilesInFilesDir(context: Context): Array<String> {
        try {
            val flashcardDirectory = File(context.filesDir, "FlashcardDirectory")
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
        val file = File(context.filesDir, "FlashcardDirectory/$fileName")
        file.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e("FilesDir", "Error reading file $fileName: ${e.message}")
        ""
    }
}

fun loadData(fileName: String = "", context: Context): Array<Deck> {
    Log.d("Flares", "Loading")
    val fileNames: Array<String> = listFilesInFilesDir(context)
    val decks = mutableListOf<Deck>()
    fileNames.forEach { fileName ->
        try {
            val fileContents: Array<String> = readFileFromFilesDir(fileName, context).split("\n").toTypedArray()

            val dateMade = fileContents[0].split(',')[0].toInt()
            val lastEdited = fileContents[0].split(',')[1].toInt()
            // current flashcard id is skipped
            val tags = fileContents[0].split(',').subList(3, fileContents[0].split(',').size)
            val cards = mutableListOf<Flashcard>()

            fileContents.toList().subList(1, fileContents.size).forEach { card ->
                Log.d("cards" ,card);
                cards.add(Flashcard(id = card.split(',').toTypedArray()[0].toInt(), SideA = card.split(',').toTypedArray()[1], SideB = card.split(',').toTypedArray()[2]))
            }

            decks.add(Deck(name = fileName, date_made = dateMade, last_edited = lastEdited, tags = tags, cards = cards))

        } catch (e: Exception) {
            Log.d("corrupted", "$fileName ${e.message}")
        val filesDir = context.filesDir.absolutePath
        Log.d("FilesDir", "Path: $filesDir")
        }
    }
    return decks.toTypedArray()
}

/*fun editFlashcard(fileName: String, id: Int){
    val flashcardDirectory = File(context.filesDir, "FlashcardDirectory")
    val file = File(flashcardDirectory, fileName) // File inside the directory
    var readData = readFileToArray(fileName, context).toMutableList()
    readData[0] = updateModifiedTime(readData[0])
    readData.add("\n${flashcardContent.id},${flashcardContent.SideA},${flashcardContent.SideB}")
}*/

fun addFlashcard(fileName: String, flashcardContent: Flashcard, context: Context){
    val flashcardDirectory = File(context.filesDir, "FlashcardDirectory")
    val file = File(flashcardDirectory, fileName) // File inside the directory
    var readData = readFileToArray(fileName, context).toMutableList()
    readData[0] = updateModifiedTime(readData[0])
    var minimalIdResult = getMinimalFlashcardId(readData[0])
    readData[0] = minimalIdResult.first
    readData.add("\n${minimalIdResult.second},${flashcardContent.SideA},${flashcardContent.SideB}")
}
private fun readFileToArray(fileName: String, context: Context): List<String>{
    val flashcardDirectory = File(context.filesDir, "FlashcardDirectory")
    val file = File(flashcardDirectory, fileName) // File inside the directory
    if (file.exists()) {
        return file.readText().split("\n")
    }
    return listOf()
}
private fun updateModifiedTime(firstLine:String):String{
    val currentTimeMillis = System.currentTimeMillis()
    val currentTimeTenSec = ((currentTimeMillis / 1000) / 10).toInt() * 10
    var newLine = firstLine.split(",").toMutableList()
    newLine[1]= currentTimeTenSec.toString()
    return newLine.joinToString { "," }
}
private fun getMinimalFlashcardId(firstLine:String):Pair<String,Int>{
    var newLine = firstLine.split(",").toMutableList()
    newLine[2] = (newLine[2].toInt()+1).toString()
    return Pair(newLine.joinToString { "," }, newLine[2].toInt())
}
fun addDeck(context: Context, fileName: String) {
    val flashcardDirectory = File(context.filesDir, "FlashcardDirectory")
    if (!flashcardDirectory.exists()) {
        flashcardDirectory.mkdirs() // Create folder if it doesn't exist
    }

    Log.d("CardFlare", fileName)

    val file = File(flashcardDirectory, fileName) // File inside the directory
    if (!file.exists()) {
        file.createNewFile() // Create new file
        val currentTimeMillis = System.currentTimeMillis()
        val currentTimeTenSec = ((currentTimeMillis / 1000) / 10).toInt() * 10
        file.writeText("$currentTimeTenSec,$currentTimeTenSec,0,\n1,hi,ioe") // Write initial content
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

public enum class SortType{
    ByName,
    ByCreationDate,
    ByLastEdited
}

public data class Flashcard(
    val id: Int,
    val SideA: String,
    val SideB: String)

public data class Deck(
    val name: String,
    val date_made: Int,
    val last_edited: Int,
    val tags: List<String>,
    val cards: List<Flashcard>)
