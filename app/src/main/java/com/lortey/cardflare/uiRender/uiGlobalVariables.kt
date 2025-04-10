package com.lortey.cardflare.uiRender

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lortey.cardflare.Deck
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.LaunchOnRule
import com.lortey.cardflare.SortType
import com.lortey.cardflare.getDeck
import com.lortey.cardflare.loadData

//All variables used by ui
var currentOpenedDeck by mutableStateOf(IndexTracker(getDeck()))
var appearAddMenu by mutableStateOf(false)
var isAscending by  mutableStateOf(true)
var appearSortMenu by mutableStateOf(false)
var sortType by mutableStateOf(SortType.ByName)
var qualifiedDecks by mutableStateOf(listOf<Deck>())
var currentOpenFlashCard by mutableStateOf(IndexTracker(0))
var cardsSelected:MutableList<Boolean> = mutableListOf()
var deckAddMenu by mutableStateOf(false)
var CardsToLearn: MutableList<Flashcard> = mutableListOf()
var renderMainMenu by mutableStateOf(true)
var decks by mutableStateOf(listOf<Deck>())
var translatedFlashcardSide:String = ""
var decksSelected:MutableList<Boolean> = mutableListOf()
var currentOpenedBinDeck:Deck = getDeck()
var launchOnRuleToModify:LaunchOnRule? = null
fun reloadDecks(context: Context){
    decks = loadData(filename = "", context = context)
}