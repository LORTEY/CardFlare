package com.example.cardflare.uiRender

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cardflare.Deck
import com.example.cardflare.Flashcard
import com.example.cardflare.R
import com.example.cardflare.getDeck
import com.example.cardflare.loadData
import com.example.cardflare.removeMultiple


//:fun adjustSelected(cardsSelected:)
// loads the screen when you click certain deck
@Composable
fun deckScreen(context: Context, navController: NavController){
    var openedTarget: Deck by remember{ mutableStateOf(currentOpenedDeck.value) }
    var cards by remember{mutableStateOf(openedTarget.cards)}
    var selectMode by remember{ mutableStateOf(false) }

    //var cards = arrayOf(arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"))
    cardsSelected = remember {  mutableStateListOf( *Array(cards.size) { false }) }

    BackHandler { // Handle the back button press
        if (deckAddMenu){
            deckAddMenu = false
        } else if (selectMode){
            selectMode = false
            cardsSelected.fill(false)
        }else{
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(10.dp)
        ) {

            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
            {

                items(cards.size) { index ->
                    Column(modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = false
                        )
                        .background(
                            if (!cardsSelected[index]) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.primary
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    cardsSelected[index] = true
                                    selectMode = true
                                },
                                onTap = {
                                    if (selectMode) {
                                        cardsSelected[index] =
                                            !cardsSelected[index] //flips ones to zeroes and vice versa
                                        if (cardsSelected.count { it } == 0) {
                                            // if no more selected cards left stop select mode
                                            selectMode = false
                                        }
                                    } else {
                                        currentOpenFlashCard = IndexTracker(index)
                                        navController.navigate("card_menu")
                                    }
                                }
                            )
                        }
                        .fillMaxWidth(1f / 2f)
                        .height((250 / 3f).dp)
                        .padding(10.dp)
                    ){
                        Log.d("Cards2", cardsSelected[index].toString())
                        Text(
                            text = cards[index].SideA,
                            color =  if (!cardsSelected[index])  MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = cards[index].SideB,
                            color = if (!cardsSelected[index])  MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            //UniversalGrid(selectMode,{selectMode = true}, { selectMode = false}, navController, cards.map { it.SideA }, cards.map { it.SideB }, onClickAction = {navController.navigate("card_menu")}, indexTracker = currentOpenFlashCard, TrackIndex = true)
            if(deckAddMenu){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    deckAddMenu = false
                                }
                            )
                        })
            }
            Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                Column(horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        //.width(128.dp)
                        .align(Alignment.End)) {
                    UniversalAddMenu(deckAddMenu, changeVisibility = { deckAddMenu = !deckAddMenu},
                        listOf(
                            AddMenuEntry("Learn",
                                R.drawable.addempty,
                                { CardsToLearn.clear(); UniversalSelected.forEachIndexed { index, value->
                                    if(value) CardsToLearn.add(
                                    currentOpenedDeck.value.cards[index])};
                                    if (CardsToLearn.size == 0)
                                        CardsToLearn = currentOpenedDeck.value.cards.toMutableList();
                                    navController.navigate("learn_screen") }),
                            AddMenuEntry("Add Flashcard",
                                R.drawable.settings,
                                {navController.navigate("add_flashcard")}),
                            AddMenuEntry("Remove Flashcards",
                            Icon = R.drawable.nav_arrow_down,
                                Action = {
                                    CardsToLearn.clear(); cardsSelected.forEachIndexed { index, value->
                                    if(value) CardsToLearn.add(
                                        currentOpenedDeck.value.cards[index])};
                                    /*if (CardsToLearn.size == 0)
                                        CardsToLearn = currentOpenedDeck!!.cards.toMutableList()};*/
                                    if (CardsToLearn.size > 0) removeMultiple(context = context, filename = currentOpenedDeck.value.name, cards = CardsToLearn);
                                    openedTarget = loadData(context = context,currentOpenedDeck.value.name)[0] ;
                                    cards = openedTarget.cards;

                                    }
                            )

                    ))
                }
            }
        }
    }

}
