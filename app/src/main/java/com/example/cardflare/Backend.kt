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

// This file contains all the functions used to load manage and store databases
fun copyAssetsToFilesDir(context: Context) {
    val assetManager = context.assets
    val FlashcardDirectory = File(context.getExternalFilesDir(null), "FlashcardDirectory") // Use getExternalFilesDir() here

    // Create the directory if it doesn't exist
    if (!FlashcardDirectory.exists()) {
        FlashcardDirectory.mkdirs()
        Log.d("FilesDir", "FlashcardDirectory created.")
    }

    try {
        // List the files in the "FlashcardDirectory" within assets folder
        val assetFiles = assetManager.list("FlashcardDirectory")

        // If there are files inside this folder, copy them to external filesDir
        assetFiles?.forEach { filename ->
            val inputStream: InputStream = assetManager.open("FlashcardDirectory/$filename")
            val outputFile = File(FlashcardDirectory, filename)
            val outputStream: OutputStream = FileOutputStream(outputFile)

            // Copy data from assets to external filesDir
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            Log.d("FilesDir", "Copied file: $filename to $FlashcardDirectory")
        }
    } catch (e: Exception) {
        Log.e("FilesDir", "Error copying assets: ${e.message}")
    }
}

// lists all files in FlashcardDirectory
fun listFilesInFilesDir(context: Context, folderName: String = "FlashcardDirectory"): Array<String> {
    try {
        val FlashcardDirectory = File(context.getExternalFilesDir(null), folderName) // Use getExternalFilesDir() here
        if (FlashcardDirectory.exists()) {
            val filenames = FlashcardDirectory.list()
            if (filenames != null) {
                return filenames
            }
        } else {
            Log.d("FilesDir", "No files found in the files directory.")
        }
    } catch (e: Exception) {
        Log.e("FilesDir", "Error reading files directory: ${e.message}")
    }
    return arrayOf()
}

fun readFileFromFilesDir(filename: String, context: Context,folderName:String = "FlashcardDirectory"): String {
    return try {
        val file = File(context.getExternalFilesDir(null), "$folderName/$filename") // Use getExternalFilesDir() here
        if(file.exists()) {
            file.bufferedReader().use { it.readText() }
        }else{
           ""
        }
    } catch (e: Exception) {
        Log.e("FilesDir", "Error reading file $filename: ${e.message}")
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
fun loadData(context: Context, filename: String, folderName: String = "FlashcardDirectory"): List<Deck> {
    var filenames: Array<String> = arrayOf()
    if(filename.isBlank()) {
        filenames = listFilesInFilesDir(context,folderName =  folderName)
    }else{
        filenames = arrayOf(filename)
    }
    val decks = mutableListOf<Deck>()
    filenames.forEach { name ->
        val jsonString = readFileFromFilesDir(name, context, folderName)
        if(!jsonString.isBlank()) {
            Log.d("cardflare2", jsonString)
            decks.add(jsonFormat.decodeFromString<Deck>(jsonString))
        }
    }
    return decks.toList()
}
fun fileExists(context: Context, fileName: String, folderName: String = ""): Boolean {
    val directory = if (folderName.isBlank()) {
        context.filesDir
    } else {
        File(context.filesDir, folderName).apply {
            if (!exists()) return false
        }
    }

    return File(directory, fileName).exists()
}
fun addFlashcard(filename: String, flashcardContent: Flashcard, context: Context, folderName:String="FlashcardDirectory", reassignID: Boolean = true) {
    var readData = loadData(filename = filename, context = context, folderName =  folderName)
    if(readData.size<=0){
        readData = listOf(getDeck(filename = filename))
    }
    readData[0].cards.add(if(reassignID) flashcardContent.copy(id = readData[0].minimalID + 1) else flashcardContent) //assigns an id to flashcard if specified to do so and adds it
    readData[0].minimalID += 1
    saveDeck(context, readData[0], filename,folderName)
}

fun addDeck(context: Context, filename: String, deck:Deck? = null, folderName: String = "FlashcardDirectory") {
    Log.d("CardFlareLine", context.getExternalFilesDir(null)?.absolutePath ?: "No Path")
    val FlashcardDirectory = File(context.getExternalFilesDir(null), folderName) // Use getExternalFilesDir() here
    if (!FlashcardDirectory.exists()) {
        FlashcardDirectory.mkdirs() // Create folder if it doesn't exist
    }

    val file = File(FlashcardDirectory, filename) // File inside the directory
    if (!file.exists()) {
        file.createNewFile() // Create new file
        val currentTimeMillis = System.currentTimeMillis()
        val currentTimeTenSec = ((currentTimeMillis / 1000) / 10).toInt() * 10
        var jsonString: String
        if(deck == null){
             jsonString = jsonFormat.encodeToString(getDeck(filename = filename, date_made = currentTimeTenSec, last_edited = currentTimeTenSec))
        }else{
             jsonString = jsonFormat.encodeToString(deck)
        }

        file.writeText(jsonString)
        reloadDecks(context)
    } else {
        Log.d("FilesDir", FlashcardDirectory.list().toList().toString())
    }
}

public fun sortDecks(searchQuery: String, decks: List<Deck>, sortType: SortType, isAscending: Boolean): List<Deck>{
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

fun MoveCardToBin(context: Context, filename: String, card: Flashcard) {
    val BinDir = File(
        context.getExternalFilesDir(null),
        "BinDirectory"
    ) // Use getExternalFilesDir() here
    if (!BinDir.exists()) {
        BinDir.mkdirs() // Create folder if it doesn't exist
    }
    val file = File(BinDir, "$filename") // File inside the directory
    if (!file.exists()) {
        file.createNewFile() // Create new file
        addDeck(context = context, filename = filename, folderName = "BinDirectory")
        reloadDecks(context)
    }
    addFlashcard(filename = filename, flashcardContent = card, context = context, folderName = "BinDirectory", reassignID = false)
    removeFlashcard(context = context, filename = filename, card = card)
}

fun removeFlashcard(context: Context, filename: String, card: Flashcard) {
    val fileData = loadData(context = context, filename = filename)
    val iterator = fileData[0].cards.iterator()
    while (iterator.hasNext()) {
        if (iterator.next().id == card.id) {
            iterator.remove()
        }
    }
    saveDeck(context = context,fileData[0], filename = filename)
}

fun removeMultiple(context: Context,filename: String, cards:List<Flashcard>){
    cards.forEach(){element ->
        MoveCardToBin(context = context, filename = filename, card = element)
    }
}
fun getDeck(filename: String ="", date_made:Int = 0, last_edited:Int = 0, tags: MutableList<String> = mutableListOf(),cards: MutableList<Flashcard> = mutableListOf() ):Deck{
    return Deck(filename,date_made,last_edited, tags,cards)
}
fun multipleDeckMoveToBin(decks:List<Deck>, selected:MutableList<Boolean>,context: Context){
    selected.forEachIndexed{ index, isSelected->
        if (isSelected){
            moveDeckToBin(context = context, filename = decks[index].name)
        }
    }
}
fun moveDeckToBin(filename: String, context: Context){
    val previousBinContent = loadData(context = context, filename = filename, folderName = "BinDirectory")
    val sourceDir = File(context.getExternalFilesDir(null), "FlashcardDirectory")
    val destDir = File(context.getExternalFilesDir(null), "BinDirectory")

    if (!destDir.exists()) {
        destDir.mkdirs()
    }

    val sourceFile = File(sourceDir, filename)
    val destFile = File(destDir, filename)

   try {
       if (destFile.exists()) {
           destFile.delete() // Delete existing file first
       }
       sourceFile.copyTo(destFile) // Copy with overwrite
       sourceFile.delete() // Delete original
       if(previousBinContent.size > 0) {
           previousBinContent[0].cards.forEach { card ->
               addFlashcard(
                   filename = filename,
                   flashcardContent = card,
                   context = context,
                   folderName = "BinDirectory",
                   reassignID = false
               )
           }
       }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun removeMultipleDecksFromBin(decksSelected:List<Boolean>, context: Context, listOfDecks:List<Deck>){
    val binDir = File(context.getExternalFilesDir(null), "BinDirectory")
    decksSelected.forEachIndexed{index , element->
        if(element) {

            val destFile = File(binDir, listOfDecks[index].name)
            if (destFile.exists()) {
                destFile.delete() // Delete existing file first
            }
        }
    }

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
