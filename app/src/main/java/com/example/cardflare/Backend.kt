package com.example.cardflare

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.InputStreamReader


    // lists all files in FlashcardDirectory
    fun  listFilesInAssets(context: Context) : Array<String>{
        try {
            val assetManager = context.assets
            val fileNames = assetManager.list("FlashcardDirectory")
            if (fileNames != null) {
                return fileNames
            } else {
                Log.d("AssetsFiles", "No files found in the assets folder.")
            }
        } catch (e: Exception) {
            Log.e("AssetsFiles", "Error reading assets folder: ${e.message}")
        }
        return arrayOf()
    }

    public fun loadData(fileName: String = "", context: Context) : Array<Deck>
    {
            Log.d("Flares","Loading");
            val fileNames: Array<String> = listFilesInAssets(context)
            val decks = mutableListOf<Deck>();

            fileNames.forEach(){ fileName->
                try{
                    val fileContents: Array<String> = readFileFromAssets(fileName.toString(), context).split("\n").toTypedArray()

                    val dateMade = fileContents[0].split(',')[0].toInt()
                    val lastEdited = fileContents[0].split(',')[1].toInt()
                    val tags = fileContents[0].split(',').subList(2, fileContents[0].split(',').size)
                    val cards = mutableListOf<String>()

                    fileContents.toList().subList(1,fileContents.size).forEach(){ card->
                        cards.add(card.split(',').toTypedArray()[0])
                        cards.add(card.split(',').toTypedArray()[1])
                    }

                    decks.add(Deck(name = fileName, date_made = dateMade, last_edited = lastEdited, tags = tags, cards = cards))

                }catch (e:Exception){ Log.d("corrupted", fileName)}
            }
            return decks.toTypedArray()
    }
    public fun readFileFromAssets(fileName: String, context: Context): String {
        return try {
            val inputStream = context.assets.open("FlashcardDirectory/$fileName")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            bufferedReader.use { it.readText() }
        } catch (e: Exception) {
            Log.e("AssetsFiles", "Error reading file $fileName: ${e.message}")
            ""
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

        if (isAscending){
            return decksQualified.reversed()
        }

        return decksQualified
    }

public enum class SortType{
    ByName,
    ByCreationDate,
    ByLastEdited
}


public data class Deck(
    val name: String,
    val date_made: Int,
    val last_edited: Int,
    val tags: List<String>,
    val cards: List<String>)
