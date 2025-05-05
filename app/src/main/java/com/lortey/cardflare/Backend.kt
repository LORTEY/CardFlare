package com.lortey.cardflare

import android.content.Context
import android.util.Log
import com.lortey.cardflare.uiRender.reloadDecks
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.MONTHS
import java.time.temporal.ChronoUnit.WEEKS

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
        if(name[0]!='.'){
            val jsonString = readFileFromFilesDir(name, context, folderName)
            try {
                if(!jsonString.isBlank()) {
                    Log.d("cardflare2", jsonString)
                    decks.add(jsonFormat.decodeFromString<Deck>(jsonString))
                }
            }catch(e:Exception){
                Log.d("cardflare", "Error While deserializing ${name}")
            }

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
fun addFlashcard(filename: String, flashcardContent: Flashcard, name: String = "", context: Context, folderName:String="FlashcardDirectory", reassignID: Boolean = true) {
    var readData = loadData(filename = filename, context = context, folderName =  folderName)
    if(readData.size<=0){
        readData = listOf(getDeck(filename = filename, name = name))
    }
    val flashcardToSave = if(reassignID) flashcardContent.copy(id = readData[0].minimalID + 1, FromDeck = filename) else flashcardContent.copy(FromDeck = filename)
    readData[0].cards.add(flashcardToSave) //assigns an id to flashcard if specified to do so and adds it
    readData[0].minimalID += 1
    saveDeck(context, readData[0], filename,folderName)
    if(folderName == "BinDirectory"){
        val binDescriptor = loadBinDescriptor(context = context)
        binDescriptor.add(BinEntry( filename = filename, id = flashcardToSave.id , dateAddedToBin = Instant.now().toEpochMilli()))
        saveBinDescriptor(context = context, binDescriptorContent = binDescriptor.toList())
    }
}

fun loadBinDescriptor(context: Context, filename: String = ".binDescriptor", folderName: String = "BinDirectory"): MutableList<BinEntry>{
    val jsonString = readFileFromFilesDir(filename, context, folderName)
    if(jsonString.isNotBlank()) {
        try {
            return jsonFormat.decodeFromString<List<BinEntry>>(jsonString).toMutableList()
        }catch(e:Exception){
            Log.d("cardflare", "Error While deserializing bin descriptor")
            return mutableListOf()
        }

    }
    return mutableListOf()
}

fun saveBinDescriptor(context: Context, binDescriptorContent: List<BinEntry>, filename: String = ".binDescriptor", folderName: String = "BinDirectory") {
    val jsonString = jsonFormat.encodeToString(binDescriptorContent)
    val file = File(context.getExternalFilesDir(null), "$folderName/$filename")
    file.writeText(jsonString)
}

fun addDeck(context: Context, name:String="", filename: String = "", deck:Deck? = null, folderName: String = "FlashcardDirectory") {
    var nameCopy = ""
    var filenameCopy = ""
    if(name.isBlank()){
        nameCopy = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString())
    }else{
        nameCopy = name
    }

    if(filename.isBlank()){
        filenameCopy = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
    }else{
        filenameCopy = filename
    }
    Log.d("CardFlareLine", context.getExternalFilesDir(null)?.absolutePath ?: "No Path")
    val FlashcardDirectory = File(context.getExternalFilesDir(null), folderName) // Use getExternalFilesDir() here
    if (!FlashcardDirectory.exists()) {
        FlashcardDirectory.mkdirs() // Create folder if it doesn't exist
    }

    val file = File(FlashcardDirectory, filenameCopy) // File inside the directory
    if (!file.exists()) {
        file.createNewFile() // Create new file
        val currentTimeMillis = System.currentTimeMillis()
        val currentTimeTenSec = ((currentTimeMillis / 1000) / 10).toInt() * 10
        var jsonString: String
        if(deck == null){
             jsonString = jsonFormat.encodeToString(getDeck(name = nameCopy, filename = filenameCopy, date_made = currentTimeTenSec, last_edited = currentTimeTenSec))
        }else{
             jsonString = jsonFormat.encodeToString(deck)
        }

        file.writeText(jsonString)
        reloadDecks(context)
    } else {
        Log.d("FilesDir", FlashcardDirectory.list().toList().toString())
    }
}

fun sortDecks(searchQuery: String, decks: List<Deck>, sortType: SortType, isAscending: Boolean): List<Deck>{
        // Enumerating tags
        var tagsRequired = mutableListOf<String>()
        if ('#' in searchQuery){
            searchQuery.split(' ').forEach { word ->
                if (word.length > 1){
                    if(word[0] == '#'){
                        tagsRequired.add(word.substring(1, word.length))
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

        decks.forEach { currentDeck->
            Log.d("cardflare111",currentDeck.tags.map{it.tagName}.toString())
            if ((tagsRequired.any { it in currentDeck.tags.map{it.tagName} } || tagsRequired.size == 0)
                || (searchTerm in currentDeck.name || searchTerm.length == 0)){
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

fun MoveCardToBin(context: Context, deckFrom: Deck, card: Flashcard, moveToBin:Boolean = true) {
    val to = if(moveToBin) "BinDirectory" else "FlashcardDirectory"
    val from = if(moveToBin) "FlashcardDirectory" else "BinDirectory"
    val BinDir = File(
        context.getExternalFilesDir(null),
        to
    ) // Use getExternalFilesDir() here
    if (!BinDir.exists()) {
        BinDir.mkdirs() // Create folder if it doesn't exist
    }
    val file = File(BinDir, "${deckFrom.filename}") // File inside the directory
    if (!file.exists()) {
        file.createNewFile() // Create new file
        addDeck(context = context, filename = deckFrom.filename, folderName = to)
        reloadDecks(context)
    }
    addFlashcard(filename = deckFrom.filename, name = deckFrom.name, flashcardContent = card, context = context, folderName = to, reassignID = false)
    removeFlashcard(context = context, filename = deckFrom.filename, card = card, folderName = from)
}

public fun removeFlashcard(context: Context, filename: String, card: Flashcard, folderName: String = "FlashcardDirectory") {
    val fileData = loadData(context = context, filename = filename, folderName = folderName)
    val iterator = fileData[0].cards.iterator()
    while (iterator.hasNext()) {
        if (iterator.next().id == card.id) {
            iterator.remove()
        }
    }
    saveDeck(context = context,fileData[0], filename = filename, folderName = folderName)
}

fun removeMultiple(context: Context, cards:List<Flashcard>, deckFrom: Deck){
    cards.forEach { element ->
        MoveCardToBin(context = context, deckFrom = deckFrom, card = element)
    }
}
fun getDeck(name: String ="",filename: String="",date_made:Int = 0, last_edited:Int = 0, tags: MutableList<Tag> = mutableListOf(), cards: MutableList<Flashcard> = mutableListOf() ):Deck{
    return Deck(if(filename.isBlank()) URLEncoder.encode(name, StandardCharsets.UTF_8.toString()) else filename,name,date_made,last_edited, tags,cards)
}
fun multipleDeckMoveToBin(decks:List<Deck>, selected:MutableList<Boolean>,context: Context){
    selected.forEachIndexed{ index, isSelected->
        if (isSelected){
            moveDeckToBin(context = context, filename = decks[index].filename)
        }
    }
}
private fun moveDeckToBin(filename: String, context: Context){
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

fun RemoveMultipleDecksFromBin(decksSelected:List<Boolean>, context: Context, listOfDecks:List<Deck>){
    val binDir = File(context.getExternalFilesDir(null), "BinDirectory")
    decksSelected.forEachIndexed{index , element->
        if(element) {
            val destFile = File(binDir, listOfDecks[index].filename)
            if (destFile.exists()) {
                destFile.delete() // Delete existing file first
            }
        }
    }
}

fun RemoveMultipleFlashcardsFromBin(cardsSelected:List<Boolean>,context: Context, listOfCards: List<Flashcard>,deck: Deck){
    val fileData = loadData(context = context, filename = deck.filename,"BinDirectory")
    val IDsOfFlashcardsToRemove = listOfCards.zip(cardsSelected)
        .filter { (_, flag) -> flag }
        .map { (value, _) -> value.id }
    val iterator = fileData[0].cards.iterator()
    while (iterator.hasNext()) {
        if (iterator.next().id in IDsOfFlashcardsToRemove) {
            iterator.remove()
        }
    }
    saveDeck(context = context,fileData[0], filename = deck.filename,"BinDirectory")
}
fun addDaysToEpochMillis(epochMillis: Long): Long {
    val settingState = AppSettings["Bin Auto Empty Time"]?.state
    return when (settingState) {
        Time.DAY -> Instant.ofEpochMilli(epochMillis).plus(1, ChronoUnit.DAYS).toEpochMilli()
        Time.DEBUG -> Instant.ofEpochMilli(epochMillis).plus(30, ChronoUnit.SECONDS).toEpochMilli()
        Time.WEEK -> Instant.ofEpochMilli(epochMillis).plus(1, WEEKS).toEpochMilli()
        Time.TWO_WEEKS -> Instant.ofEpochMilli(epochMillis).plus(2, WEEKS).toEpochMilli()
        Time.MONTH -> Instant.ofEpochMilli(epochMillis).plus(1 * 30, ChronoUnit.DAYS).toEpochMilli()
        Time.TWO_MONTHS -> Instant.ofEpochMilli(epochMillis).plus(2 * 30, ChronoUnit.DAYS).toEpochMilli()
        else -> Instant.ofEpochMilli(epochMillis).plus(1 * 30,ChronoUnit.DAYS).toEpochMilli()
    }

}
fun BinAutoEmpty(context:Context){
    var binDescriptorContent = loadBinDescriptor(context = context)
    var lastNotRemovedIndex = 0
    for(entry in binDescriptorContent){
        if(addDaysToEpochMillis(entry.dateAddedToBin) < Instant.now().toEpochMilli()){
            Log.d("cardflare3",Instant.ofEpochMilli(entry.dateAddedToBin).toString())
            Log.d("cardflare3",Instant.ofEpochMilli(entry.dateAddedToBin).plus(1, ChronoUnit.SECONDS).toString())
            Log.d("cardflare3",Instant.now().toString())
            lastNotRemovedIndex += 1
            try{
                removeFlashcard(context = context, filename = entry.filename, Flashcard(entry.id, "", "","", "", 0), folderName = "BinDirectory")
                val deck = loadData(context = context, filename = entry.filename, folderName = "BinDirectory")
                if(deck[0].cards.isEmpty()){
                    RemoveMultipleDecksFromBin(listOf(true), context, deck)
                }
            }catch (e:Exception){
                Log.d("cardflare3",entry.filename)
            }
        }else{
            break
        }
    }
    if(lastNotRemovedIndex > binDescriptorContent.size){
        binDescriptorContent = mutableListOf()
    }else{
        binDescriptorContent = binDescriptorContent.subList(lastNotRemovedIndex, binDescriptorContent.size)
    }

    val descriptorToSave = binDescriptorContent.sortedBy { it.dateAddedToBin }
    saveBinDescriptor(context, descriptorToSave)
}

fun EnsureDirectoryStructure(context: Context){

    val directory = File(context.getExternalFilesDir(null), "BinDirectory")
    if (!directory.exists()) {
        directory.mkdirs() // Creates parent directories if needed
    }

    // Create the file if it doesn't exist
    val file = File(directory, ".binDescriptor")
    if (!file.exists()) {
        try {
            saveBinDescriptor(context, listOf())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    val flashcardDir = File(context.getExternalFilesDir(null), "FlashcardDirectory")
    if (!flashcardDir.exists()) {
        flashcardDir.mkdirs() // Creates parent directories if needed
    }
}

fun RecoverMultipleFlashcards(context:Context,listSelected:List<Boolean>, listOfFlashcards:List<Flashcard>, deckFrom:Deck){
    listOfFlashcards.forEachIndexed{index, card->
        if(listSelected[index]){
            MoveCardToBin(context = context, deckFrom = deckFrom, card = card, moveToBin = false)
        }
    }
    try{
        val deck = loadData(context = context, filename = deckFrom.filename, folderName = "BinDirectory")
        if(deck[0].cards.isEmpty()){
            RemoveMultipleDecksFromBin(listOf(true), context, deck)
        }
    }catch (e:Exception){
        Log.d("cardflare3",deckFrom.filename)
    }
}

fun RecoverMultipleDecks(context:Context, listSelected:List<Boolean>, listOfDecks:List<Deck>){
    val flashcardDir = File(context.getExternalFilesDir(null), "FlashcardDirectory")
    listOfDecks.forEachIndexed { index, deck ->
        if(listSelected[index]) {
            if (!File(flashcardDir, deck.filename).exists()) {
                val sourceFile =
                    File(context.getExternalFilesDir(null), "BinDirectory/${deck.filename}")
                val destFile =
                    File(context.getExternalFilesDir(null), "FlashcardDirectory/${deck.filename}")
                destFile.parentFile?.mkdirs()
                try {
                    sourceFile.copyTo(destFile, overwrite = false)
                    sourceFile.delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                deck.cards.forEach { card ->
                    MoveCardToBin(
                        context = context,
                        deckFrom = deck,
                        card = card,
                        moveToBin = false
                    )
                }
            }
            try {
                val deckInBin =
                    loadData(
                        context = context,
                        filename = deck.filename,
                        folderName = "BinDirectory"
                    )
                if (deckInBin[0].cards.isEmpty()) {
                    RemoveMultipleDecksFromBin(listOf(true), context, deckInBin)
                }
            } catch (e: Exception) {
                Log.d("cardflare3", deck.filename)
            }
        }
    }
}
fun calculateSimilarity(query: String, target: String): Double {
    val maxLength = maxOf(query.length, target.length)
    return if (maxLength == 0) 1.0 else {
        1.0 - (levenshteinDistance(query, target) / maxLength.toDouble())
    }
}

// Levenshtein distance implementation
fun levenshteinDistance(a: String, b: String): Int {
    val dp = Array(a.length + 1) { IntArray(b.length + 1) }
    for (i in 0..a.length) dp[i][0] = i
    for (j in 0..b.length) dp[0][j] = j
    for (i in 1..a.length) {
        for (j in 1..b.length) {
            dp[i][j] = minOf(
                dp[i-1][j] + 1,
                dp[i][j-1] + 1,
                dp[i-1][j-1] + if (a[i-1] == b[j-1]) 0 else 1
            )
        }
    }
    return dp[a.length][b.length]
}

enum class SortType{
    ByName,
    ByCreationDate,
    ByLastEdited
}

@Serializable
data class Flashcard(
    val id: Int,
    val SideA: String,
    val SideB: String,
    val FromDeck: String,
    var FsrsData: String,
    var due:Long)

@Serializable
data class Deck(
    val filename: String,
    val name: String,
    val date_made: Int,
    val last_edited: Int,
    val tags: MutableList<Tag>,
    val cards: MutableList<Flashcard>,
    var minimalID: Int = 0)

@Serializable
data class BinEntry(
    val dateAddedToBin: Long,
    val filename: String,
    val id: Int
)