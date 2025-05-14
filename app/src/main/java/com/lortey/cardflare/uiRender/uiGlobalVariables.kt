package com.lortey.cardflare.uiRender

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lortey.cardflare.Deck
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.LaunchOnRule
import com.lortey.cardflare.SortType
import com.lortey.cardflare.getDeck
import com.lortey.cardflare.loadData

//All variables used by ui
var currentOpenedDeck by mutableStateOf(getDeck()) //curently opened deck
var appearAddMenu by mutableStateOf(false) // was the plus button pressed
var isAscending by  mutableStateOf(true) // should decks be sorted ascending
var appearSortMenu by mutableStateOf(false) // should the main screen sort menu appear
var sortType by mutableStateOf(SortType.ByName) // sorting type
var qualifiedDecks by mutableStateOf(listOf<Deck>()) // decks to be shown on main screen
var currentOpenFlashCard by mutableIntStateOf(0) // currently edited flashcard
var deckAddMenu by mutableStateOf(false) // is deck plus menu opened
var CardsToLearn: MutableList<Flashcard> = mutableListOf() // cards the learnscreen should display
var renderMainMenu by mutableStateOf(true) // no usecase now
var decks by mutableStateOf(listOf<Deck>()) // decks in main menu
var decksSelected:MutableList<Boolean> = mutableListOf() // decks selected on main menu
var currentOpenedBinDeck:Deck = getDeck() // currently oppened deck in bin
var launchOnRuleToModify:MutableState<LaunchOnRule?> = mutableStateOf(null) // curent launch on rule being modified
var lastOpenedRule:String? = null // last opened rulemused for add delete tracking
fun reloadDecks(context: Context){ // reload decks on main screen force recomposition
    decks = loadData(filename = "", context = context)
}