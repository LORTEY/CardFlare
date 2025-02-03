package com.example.cardflare

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

fun copyAssetsToFilesDir(context: Context) {
    val assetManager = context.assets
    val flashcardDirectory = File(context.filesDir, "FlashcardDirectory")

    // Create the directory if it doesn't exist
    if (!flashcardDirectory.exists()) {
        flashcardDirectory.mkdirs()
        Log.d("FilesDir", "FlashcardDirectory created.")
    }

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
            val tags = fileContents[0].split(',').subList(2, fileContents[0].split(',').size)
            val cards = mutableListOf<Array<String>>()

            fileContents.toList().subList(1, fileContents.size).forEach { card ->
                cards.add(arrayOf(card.split(',').toTypedArray()[0], card.split(',').toTypedArray()[1]))
            }

            decks.add(Deck(name = fileName, date_made = dateMade, last_edited = lastEdited, tags = tags, cards = cards))

        } catch (e: Exception) {
            Log.d("corrupted", fileName)
        }
    }
    return decks.toTypedArray()
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
        file.writeText("13012025,13012025,Japanese\n" +
                "Hello,おはよ\n" +
                "Thank you,ありがとう") // Write initial content (optional)
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
    val SideA: String,
    val SideB: String)

public data class Deck(
    val name: String,
    val date_made: Int,
    val last_edited: Int,
    val tags: List<String>,
    val cards: List<Array<String>>)
