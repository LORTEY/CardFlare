package com.lortey.cardflare.uiRender

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lortey.cardflare.Deck
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.SortType
import com.lortey.cardflare.getDeck
import com.lortey.cardflare.loadData

//All variables used by ui
public var currentOpenedDeck by mutableStateOf(IndexTracker(getDeck()))
public var appearAddMenu by mutableStateOf(false)
public var isAscending by  mutableStateOf(true)
public var appearSortMenu by mutableStateOf(false)
public var sortType by mutableStateOf(SortType.ByName)
public var qualifiedDecks by mutableStateOf(listOf<Deck>())
public var currentOpenFlashCard by mutableStateOf(IndexTracker(0))
public var cardsSelected:MutableList<Boolean> = mutableListOf()
public var deckAddMenu by mutableStateOf(false)
public var CardsToLearn: MutableList<Flashcard> = mutableListOf()
public var renderMainMenu by mutableStateOf(true)
public var decks by mutableStateOf(listOf<Deck>())
public var translatedFlashcardSide:String = ""
public var decksSelected:MutableList<Boolean> = mutableListOf()
public var currentOpenedBinDeck:Deck = getDeck()
public fun reloadDecks(context: Context){
    decks = loadData(filename = "", context = context)
}