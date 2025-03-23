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

// loads the screen when you click certain deck
@Composable
fun deckScreen(context: Context, navController: NavController){
    val openedTarget: Deck = currentOpenedDeck ?: Deck("",0,0, listOf<String>(), listOf<Flashcard>())
    val cards = openedTarget.cards
    var selectMode by remember{ mutableStateOf(false) }
    //var cards = arrayOf(arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"))
    cardsSelected = remember {  mutableStateListOf( *Array(cards.size) { 0 }) }

    BackHandler { // Handle the back button press
        if (deckAddMenu){
            deckAddMenu = false
        } else if (selectMode){
            selectMode = false
            cardsSelected.fill(0)
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
                            if (cardsSelected[index] == 0) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.primary
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    cardsSelected[index] = 1
                                    selectMode = true
                                },
                                onTap = {
                                    if (selectMode) {
                                        cardsSelected[index] =
                                            1 - cardsSelected[index] //flips ones to zeroes and vice versa
                                        if (cardsSelected.count { it == 1 } == 0) {
                                            // if no more selected cards left stop select mode
                                            selectMode = false
                                        }
                                    } else {
                                        currentOpenFlashCard = index
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
                            color =  if (cardsSelected[index] == 0)  MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = cards[index].SideB,
                            color = if (cardsSelected[index] == 0)  MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
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
                    DeckAddMenu(navController)
                }
            }
        }
    }

}
@Composable
fun DeckAddMenu(navController: NavController){
    AnimatedVisibility(
        modifier = Modifier.padding(horizontal = 40.dp),
        visible = deckAddMenu,
        enter = fadeIn(animationSpec = tween(100)) + slideInVertically(
            animationSpec = tween(100)
        ) { fullWidth -> fullWidth / 2 },
        exit = fadeOut(animationSpec = tween(100)) + slideOutVertically(
            animationSpec = tween(100)
        ) { fullWidth -> fullWidth / 2 }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
            // Here are all buttons for the menu
            Row(modifier = Modifier
                .clickable {navController.navigate("add_flashcard")},
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                Box(modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 8.dp)
                    .background(
                        shape = RoundedCornerShape(128.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )

                ){
                    Text(
                        "Add Flashcard",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
                //row is here just for background color
                Row(modifier = Modifier
                    .background(
                        shape = RoundedCornerShape(128.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "chart",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(10.dp)
                    )
                }
            }
            Row(modifier = Modifier
                .clickable {
                    CardsToLearn.clear(); cardsSelected.forEachIndexed { index, value-> if(value == 1) CardsToLearn.add(
                    currentOpenedDeck!!.cards[index])}; if (CardsToLearn.size == 0) CardsToLearn = currentOpenedDeck!!.cards.toMutableList(); navController.navigate("learn_screen")},
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                Box(modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 8.dp)
                    .background(
                        shape = RoundedCornerShape(128.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )

                ){
                    Text(
                        "Learn",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
                //row is here just for background color
                Row(modifier = Modifier
                    .background(
                        shape = RoundedCornerShape(128.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    .size(48.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.addempty),
                        contentDescription = "chart",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(10.dp)
                    )
                }
            }
            Row(modifier = Modifier
                .clickable {},
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 8.dp)
                    .background(
                        shape = RoundedCornerShape(128.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )

                ){
                    Text(
                        "Also Something Else",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
                //row is here just for background color
                Row(modifier = Modifier
                    .background(
                        shape = RoundedCornerShape(128.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    .size(48.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.bar_chart_2),
                        contentDescription = "chart",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(10.dp)
                    )
                }
            }
        }


    }

    Icon(
        painter = painterResource(id = R.drawable.plus_circle_solid),
        contentDescription = "chart",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(128.dp)
            .padding(15.dp)
            .background(MaterialTheme.colorScheme.background, shape = CircleShape)
            .clickable { deckAddMenu = !deckAddMenu }
    )
}
