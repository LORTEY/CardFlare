package com.example.cardflare.uiRender

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.cardflare.Deck
import com.example.cardflare.Flashcard
import com.example.cardflare.SortType
import com.example.cardflare.loadData

//All variables used by ui
public var currentOpenedDeck : Deck? by mutableStateOf(null)
public var appearAddMenu by mutableStateOf(false)
public var isAscending by  mutableStateOf(true)
public var appearSortMenu by mutableStateOf(false)
public var sortType by mutableStateOf(SortType.ByName)
public var qualifiedDecks = listOf<Deck>()
public var currentOpenFlashCard by mutableStateOf(0)
public var cardsSelected = mutableStateListOf( *Array(0) { 0 })
public var deckAddMenu by mutableStateOf(false)
public var CardsToLearn: MutableList<Flashcard> = mutableListOf()
public var renderMainMenu by mutableStateOf(true)
public var decks : Array<Deck> =  arrayOf<Deck>(Deck("",0,0, listOf<String>(), listOf<Flashcard>()))
public var translatedFlashcardSide:String = ""
public fun reloadDecks(context: Context){
    decks = loadData("", context = context)
}