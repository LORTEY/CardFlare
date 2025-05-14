package com.lortey.cardflare

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

//Rating of user memory of fsrs
enum class Rating{
    GOOD, AGAIN, HARD, EASY
}

    private var module:PyObject? = null

    private fun getPython(context: Context):Python{
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        val python = Python.getInstance()
        return python
    }
    private fun getModule(context:Context): PyObject {
        return module ?: getPython(context).getModule("FSRSManager")
    }
    public fun initialize(context: Context){
        val module = getModule(context)
        module.callAttr("initialization")
    }

    //parse to python previous fsrs value to getnew fsrs value based on how good user performed
    public fun reviewCard(context: Context, card: Flashcard, rating: Rating){
        val module = getModule(context)

        val returnedFsrsValue = module.callAttr("reviewCard", card.FsrsData, rating.toString()).toString()

        //replace the flashcard swapping the fsrs value
        removeFlashcard(context = context, filename = card.FromDeck, card = card)
        addFlashcard(filename = card.FromDeck, flashcardContent = card.copy(FsrsData = returnedFsrsValue, due = getDueDate(context, returnedFsrsValue)), context = context, reassignID = false)
    }

//default fsrs value for new flashcards
    public fun getDefaultFSRSValue(context: Context):String{
        val module = getModule(context)
        return module.callAttr("default_fsrs_value").toString()
    }

//get due date of a flashcard based on the fsrs value
fun getDueDate(context: Context, fsrsValue:String):Long{
    val module = getModule(context)
    val ret = module.callAttr("get_due_date", fsrsValue).toString()

    return isoToEpochMillis(ret)
}

// is epochi millis time today
fun isEpochMillisToday(millis: Long, timeZone: ZoneId = ZoneId.systemDefault()): Boolean {
    // Convert millis to Instant and then to ZonedDateTime in target timezone
    val dateTime = Instant.ofEpochMilli(millis).atZone(timeZone)

    // Get current date in the same timezone
    val today = ZonedDateTime.now(timeZone).toLocalDate()

    // Compare dates
    return dateTime.toLocalDate() == today
}

// iso time to epoch millis
fun isoToEpochMillis(isoString: String): Long {
    // First standardize the format by replacing space with 'T' for strict ISO-8601
    val standardized = isoString.replace(" ", "T")

    // Parse directly to OffsetDateTime
    val odt = OffsetDateTime.parse(standardized, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    // Convert to Instant and then to epoch milliseconds
    return odt.toInstant().toEpochMilli()
}

// sort list of flashcards by their due date
fun rankByDueDate(context: Context,deckList:List<Deck>? = null):List<Flashcard>{
    var data = listOf<Deck>()
    if(deckList == null){
        data = loadData(context = context, filename = "")
    }else{
        data = deckList
    }

    var listOfCards:MutableList<Flashcard> = mutableListOf()

    data.forEach { deck->
        listOfCards.addAll(deck.cards)
    }

    listOfCards = listOfCards.sortedBy { it.due }.toMutableList()

    return listOfCards
}


fun getDueCards(context: Context):List<Flashcard>{
    val cardsRanked = rankByDueDate(context)
    val cardsDueNow:MutableList<Flashcard> = mutableListOf()
    for (card in cardsRanked){
        if(card.due < System.currentTimeMillis() || isEpochMillisToday(card.due)){
            cardsDueNow.add(card)
        }else{
            //break if card is not today because the list is sorted
            break
        }
    }
    return cardsDueNow
}
