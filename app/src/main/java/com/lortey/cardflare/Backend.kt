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
import java.time.temporal.ChronoUnit.WEEKS

// This file contains all the functions used to load manage and store database of Flashcards And Decks


fun copyAssetsToFilesDir(context: Context) { // Leaving in it code for easier updates in the future it remaps old Deck data types to new
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

//Function for reading a file used everywhere
fun readFileFromFilesDir(filename: String, context: Context,folderName:String = "FlashcardDirectory"): String {
    return try {
        val file = File(context.getExternalFilesDir(null), "$folderName/$filename") // location of filesDir /0/android/data/com.lortey.cardflare/files/
        if(file.exists()) { // If file exists i dont check the directory existence as it's creation is insured by
            file.bufferedReader().use { it.readText() }
        }else{
           ""
        }
    } catch (e: Exception) {
        Log.e("FilesDir", "Error reading file $filename: ${e.message}") // File or directory missing
        ""
    }
}
private val jsonFormat = Json { prettyPrint = true } // jsonFormat

// Save deck to file
fun saveDeck(context: Context, deck: Deck, filename: String, folderName: String = "FlashcardDirectory") {
    val jsonString = jsonFormat.encodeToString(deck)
    val file = File(context.getExternalFilesDir(null), "$folderName/$filename")
    file.writeText(jsonString)
}

// Load deck from file
fun loadData(context: Context, filename: String, folderName: String = "FlashcardDirectory"): List<Deck> {
    var filenames: Array<String> = arrayOf()
    if(filename.isBlank()) { // if filename is "" load all from folder
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
                    decks.add(jsonFormat.decodeFromString<Deck>(jsonString)) //deserialize deck from json
                }
            }catch(e:Exception){
                Log.d("cardflare", "Error While deserializing ${name}")
            }

        }
    }
    return decks.toList()
}

//add Flashcard to deck
fun addFlashcard(filename: String, flashcardContent: Flashcard, name: String = "", context: Context, folderName:String="FlashcardDirectory", reassignID: Boolean = true) {
    var readData = loadData(filename = filename, context = context, folderName =  folderName)
    if(readData.size<=0){
        readData = listOf(getDeck(filename = filename, name = name))
    }
    // if id should be reassigned so for example new card is added get minimalk id else add card as it is
    val flashcardToSave = if(reassignID) flashcardContent.copy(id = readData[0].minimalID + 1, FromDeck = filename) else flashcardContent.copy(FromDeck = filename)
    readData[0].cards.add(flashcardToSave) //assigns an id to flashcard if specified to do so and adds it
    readData[0].minimalID += 1 // increase minimal card id
    saveDeck(context, readData[0], filename,folderName) //save deck with new card

    // if the flashcard is being added to bin auto add it to bin descriptor
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


//save bindescriptor
fun saveBinDescriptor(context: Context, binDescriptorContent: List<BinEntry>, filename: String = ".binDescriptor", folderName: String = "BinDirectory") {
    val jsonString = jsonFormat.encodeToString(binDescriptorContent)
    val file = File(context.getExternalFilesDir(null), "$folderName/$filename")
    file.writeText(jsonString)
}

//add Deck
fun addDeck(context: Context, name:String="", filename: String = "", deck:Deck? = null, folderName: String = "FlashcardDirectory") {
    var nameCopy = ""
    var filenameCopy = ""
    if(name.isBlank()){ // if no name was added add it automatically from filename
        nameCopy = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString())
    }else{
        nameCopy = name
    }

    if(filename.isBlank()){ // if no name was added add it automatically from display name
        filenameCopy = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
    }else{
        filenameCopy = filename
    }

    val FlashcardDirectory = File(context.getExternalFilesDir(null), folderName)
    if (!FlashcardDirectory.exists()) {
        FlashcardDirectory.mkdirs() // Create folder if it doesn't exist
    }

    val file = File(FlashcardDirectory, filenameCopy) // File inside the directory
    if (!file.exists()) {// if no such file exists
        file.createNewFile() // Create new file
        val currentTimeMillis = System.currentTimeMillis()
        val currentTimeTenSec = ((currentTimeMillis / 1000) / 10).toInt() * 10 // get time to assign as creation time of deck
        val jsonString: String
        if(deck == null){ // if deck was null create new empty deck
             jsonString = jsonFormat.encodeToString(getDeck(name = nameCopy, filename = filenameCopy, date_made = currentTimeTenSec, last_edited = currentTimeTenSec))
        }else{ //else save inputed deck
             jsonString = jsonFormat.encodeToString(deck)
        }

        file.writeText(jsonString)
        reloadDecks(context) // reload main menu
    }
}

//sort decks based on sorting aspect and user search query
fun sortDecks(searchQuery: String, decks: List<Deck>, sortType: SortType, isAscending: Boolean): List<Deck>{
        // Enumerating tags
        val tagsRequired = mutableListOf<String>()
        if ('#' in searchQuery){
            searchQuery.split(' ').forEach { word ->
                if (word.length > 1){
                    if(word[0] == '#'){ //get tags user inputed. if word starts with '#' its a tag
                        tagsRequired.add(word.substring(1, word.length)) // add tag to the tags the decks need to have
                    }
                }
            }
        }
        var searchTerm: String
        try { // not really used but allows user to input both tags and search terms simultaniously
            searchTerm = searchQuery.split(" & ")[0]
        }catch(e:Exception){
            searchTerm = searchQuery
        }

        var decksQualified = mutableListOf<Deck>()

        decks.forEach { currentDeck-> // get only those decks that have inputed tags or name matches searchquery
            if ((tagsRequired.any { it in currentDeck.tags.map{it.tagName} } || tagsRequired.size == 0)
                || (searchTerm in currentDeck.name || searchTerm.length == 0)){
                decksQualified.add(currentDeck)
            }
        }

        when (sortType){ // sort the qualified decks based on user sort option
            SortType.ByName -> decksQualified = decksQualified.sortedWith(compareBy<Deck> { it.name }.thenBy{ it.date_made}).toMutableList()
            SortType.ByCreationDate -> decksQualified = decksQualified.sortedWith(compareBy<Deck> { it.date_made }.thenBy{ it.name}).toMutableList()
            SortType.ByLastEdited -> decksQualified = decksQualified.sortedWith(compareBy<Deck> { it.last_edited }.thenBy{ it.name}).toMutableList()
        }

        if (!isAscending){ // reverse if sort should be descending
            return decksQualified.reversed()
        }

        return decksQualified
    }


//Move card To bin or from bin
fun moveCardToBin(context: Context, deckFrom: Deck, card: Flashcard, moveToBin:Boolean = true) {
    //Set source destination directories base on course of move
    val to = if(moveToBin) "BinDirectory" else "FlashcardDirectory"
    val from = if(moveToBin) "FlashcardDirectory" else "BinDirectory"

    val BinDir = File(
        context.getExternalFilesDir(null),
        to
    )
    if (!BinDir.exists()) {
        BinDir.mkdirs() // Create folder if it doesn't exist
    }
    val file = File(BinDir, deckFrom.filename) // File inside the bin directory
    if (!file.exists()) { // if its not yet in bin
        file.createNewFile() // Create new file
        addDeck(context = context, filename = deckFrom.filename, folderName = to)
        reloadDecks(context) // reload decks in main menu
    }
    addFlashcard(filename = deckFrom.filename, name = deckFrom.name, flashcardContent = card, context = context, folderName = to, reassignID = false)
    removeFlashcard(context = context, filename = deckFrom.filename, card = card, folderName = from)
}

//remove flashcard from deck
fun removeFlashcard(context: Context, filename: String, card: Flashcard, folderName: String = "FlashcardDirectory") {
    val fileData = loadData(context = context, filename = filename, folderName = folderName)
    val iterator = fileData[0].cards.iterator()
    while (iterator.hasNext()) {
        if (iterator.next().id == card.id) {
            iterator.remove()
        }
    }
    //fileData[0].cards.filterNot { it.id == card.id } //remove flashcard from loaded data with coresponding id
    //save deck without that flashcard
    saveDeck(context = context,fileData[0], filename = filename, folderName = folderName)
}

//Move multiple cards to bin
fun removeMultiple(context: Context, cards:List<Flashcard>, deckFrom: Deck){
    cards.forEach { element ->// move each card
        moveCardToBin(context = context, deckFrom = deckFrom, card = element)
    }
}

//get default value of deck
fun getDeck(name: String ="",filename: String="",date_made:Int = 0, last_edited:Int = 0, tags: MutableList<Tag> = mutableListOf(), cards: MutableList<Flashcard> = mutableListOf(), sideALang: String? = null, sideBLang: String? = null):Deck{
    return Deck(if(filename.isBlank()) URLEncoder.encode(name, StandardCharsets.UTF_8.toString()) else filename,name,date_made,last_edited, tags,cards, 0,sideALang,sideBLang)
}

//Move decks to bin
fun multipleDeckMoveToBin(decks:List<Deck>, selected:MutableList<Boolean>,context: Context){
    selected.forEachIndexed{ index, isSelected-> // map user selected decks to their indexes
        if (isSelected){
            moveDeckToBin(context = context, filename = decks[index].filename) // move deck to bin
        }
    }
}

//Move deck from FlashcardDir to Bin
private fun moveDeckToBin(filename: String, context: Context){
    val previousBinContent = loadData(context = context, filename = filename, folderName = "BinDirectory") // contens of deck from bin if there exists one with the same filename
    val sourceDir = File(context.getExternalFilesDir(null), "FlashcardDirectory")
    val destDir = File(context.getExternalFilesDir(null), "BinDirectory")

    if (!destDir.exists()) { //make sure bin/ exists
        destDir.mkdirs()
    }

    val sourceFile = File(sourceDir, filename)
    val destFile = File(destDir, filename)

   try {
       if (destFile.exists()) {
           destFile.delete() // Delete deck from bin if there exists one with the same filename
       }
       sourceFile.copyTo(destFile) // Copy with overwrite
       sourceFile.delete() // Delete original
       if(previousBinContent.isNotEmpty()) { // add flashcards from previous file with the same filename if it existed
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

//Delete Decks from bin
fun removeMultipleDecksFromBin(decksSelected:List<Boolean>, context: Context, listOfDecks:List<Deck>){
    val binDir = File(context.getExternalFilesDir(null), "BinDirectory")
    decksSelected.forEachIndexed{index , element-> // for each selected by user deck delete coresponding deck index
        if(element) {
            val file = File(binDir, listOfDecks[index].filename) //deck file
            if (file.exists()) {
                file.delete() // delete deck file
            }
        }
    }
}

//Delete Flashcards from bin
fun removeMultipleFlashcardsFromBin(cardsSelected:List<Boolean>, context: Context, listOfCards: List<Flashcard>, deck: Deck){
    val fileData = loadData(context = context, filename = deck.filename,"BinDirectory")
    val IDsOfFlashcardsToRemove = listOfCards.zip(cardsSelected) // Translates user selected cards to list of ids of flashcards in deck to remove
        .filter { (_, flag) -> flag }
        .map { (value, _) -> value.id }
    // iterator needed because we are actively deleting elements of what we are iterating on
    val iterator = fileData[0].cards.iterator()
    while (iterator.hasNext()) { //Delete all flashcards with matching IDs
        if (iterator.next().id in IDsOfFlashcardsToRemove) {
            iterator.remove()
        }
    }
    //Save The deck in bin from which we deleted flashcards but without the deleted flashcards
    saveDeck(context = context,fileData[0], filename = deck.filename,"BinDirectory")
}

//Used to determine when a bin descriptor entry has expired based on user settings
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

//Searches bin descriptor file for flashcards that should be removed
fun binAutoEmpty(context:Context){
    var binDescriptorContent = loadBinDescriptor(context = context)
    var lastNotRemovedIndex = 0 // used to find where the new bin descriptor should begin tracking
    for(entry in binDescriptorContent){
        if(addDaysToEpochMillis(entry.dateAddedToBin) < Instant.now().toEpochMilli()){
            // If flashcard is past its due date. We are adding the user's selected bin auto empty file to the time the flashcard was added to bin.
            lastNotRemovedIndex += 1 // Increase index
            try{
                removeFlashcard(context = context, filename = entry.filename, Flashcard(entry.id, "", "","", "", 0), folderName = "BinDirectory")
                val deck = loadData(context = context, filename = entry.filename, folderName = "BinDirectory")
                if(deck[0].cards.isEmpty()){
                    removeMultipleDecksFromBin(listOf(true), context, deck)
                }
            }catch (e:Exception){
                Log.d("cardflare3",entry.filename)
            }
        }else{
            break
        // Because bin descriptor is sorted by add date first to last we break the check loop when an entry is not yet due to delete
        }
    }

    //Creates A new bin descriptor file from where the algorithm stopped deleting entries
    if(lastNotRemovedIndex > binDescriptorContent.size){
        binDescriptorContent = mutableListOf()
    }else{
        binDescriptorContent = binDescriptorContent.subList(lastNotRemovedIndex, binDescriptorContent.size)
    }

    //Just doubler check. Sorting the new bin descriptor by date added to bin.
    val descriptorToSave = binDescriptorContent.sortedBy { it.dateAddedToBin }
    saveBinDescriptor(context, descriptorToSave)
}

fun ensureDirectoryStructure(context: Context){

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

    val TranslationDir = File(context.getExternalFilesDir(null), "Translations")
    if (!TranslationDir.exists()) {
        TranslationDir.mkdirs() // Creates parent directories if needed
    }
}

//Recover multiple flashcard from bin
fun RecoverMultipleFlashcards(context:Context,listSelected:List<Boolean>, listOfFlashcards:List<Flashcard>, deckFrom:Deck){
    listOfFlashcards.forEachIndexed{index, card->
        if(listSelected[index]){     // Translate Selected by user flashcards to list of flashcards to recover
            moveCardToBin(context = context, deckFrom = deckFrom, card = card, moveToBin = false) //reverse moving card to bin
        }
    }
    try{ // if decks left empty in bin delete them
        val deck = loadData(context = context, filename = deckFrom.filename, folderName = "BinDirectory")
        if(deck[0].cards.isEmpty()){
            removeMultipleDecksFromBin(listOf(true), context, deck)
        }
    }catch (e:Exception){
        Log.d("cardflare3",deckFrom.filename)
    }
}

//Recover multiple decks from bin
fun RecoverMultipleDecks(context:Context, listSelected:List<Boolean>, listOfDecks:List<Deck>){
    val flashcardDir = File(context.getExternalFilesDir(null), "FlashcardDirectory")
    listOfDecks.forEachIndexed { index, deck -> // Run for each provided deck
        if(listSelected[index]) {
            if (!File(flashcardDir, deck.filename).exists()) { // If no part of this deck is left in flashcard folder
                val sourceFile =
                    File(context.getExternalFilesDir(null), "BinDirectory/${deck.filename}") // Bin directory
                val destFile =
                    File(context.getExternalFilesDir(null), "FlashcardDirectory/${deck.filename}") //Flashcard Directory
                destFile.parentFile?.mkdirs()
                try {
                    sourceFile.copyTo(destFile, overwrite = false) // Dont overwrite another deck if its there
                    sourceFile.delete() //Delete deck from bin
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else {
                deck.cards.forEach { card ->
                    //If some parts of this deck are left in FlashcardDir copy the flashcards to this decks name mate in FlashcardDir
                    moveCardToBin(
                        context = context,
                        deckFrom = deck,
                        card = card,
                        moveToBin = false
                    )
                }
            }
            try {
                //If a deck was left empty in bin delete them
                val deckInBin =
                    loadData(
                        context = context,
                        filename = deck.filename,
                        folderName = "BinDirectory"
                    )
                if (deckInBin[0].cards.isEmpty()) { //Remove recovered decks from bin if it was not directly copied back to FlashcardDirectory
                    removeMultipleDecksFromBin(listOf(true), context, deckInBin)
                }
            } catch (e: Exception) {
                Log.d("cardflare3", deck.filename)
            }
        }
    }
}

// similarity for searching decks
fun calculateSimilarity(query: String, target: String): Double {
    val maxLength = maxOf(query.length, target.length)
    return if (maxLength == 0) 1.0 else {
        1.0 - (levenshteinDistance(query, target) / maxLength.toDouble())
    }
}

// Levenshtein distance implementation for searching decks
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

enum class SortType{ //Used for sorting search results
    ByName,
    ByCreationDate,
    ByLastEdited
}

@Serializable
data class Flashcard(
    val id: Int, // ID
    val SideA: String, //Side a text
    val SideB: String, //Side B text
    val FromDeck: String, // filename of the source deck
    var FsrsData: String, // Data of this card passed to android fo4r estimating next review time using fsrs
    var due:Long) // epochi millis estimation of when a flashcard needs to be reviewed

@Serializable
data class Deck(
    val filename: String, // The real filename of the deck it never changes and is used by things like launch on rules
    val name: String, // name displayed to the user
    val date_made: Int, //Date the deck was created in epochi millis used for searching
    val last_edited: Int, //last edited in epochi millis used for searching but kind of forgot to implement this
    val tags: MutableList<Tag>, //Tags of deck used for searching
    val cards: MutableList<Flashcard>, // List of flashcards in Deck
    var minimalID: Int = 0, // minimal ID a newly added flashcard can get
    var sideALang:String? = null,// language of side A for auto completion
    var sideBLang:String? = null // language of side B for auto completion
    )

//Used in bin descriptor
@Serializable
data class BinEntry(
    val dateAddedToBin: Long, //Date added to bin used for auto deletion
    val filename: String, // filename from which flashcard is
    val id: Int // flashcard id
)