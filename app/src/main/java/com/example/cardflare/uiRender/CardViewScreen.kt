package com.example.cardflare.uiRender

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cardflare.Deck
import com.example.cardflare.Flashcard
import com.example.cardflare.R
import com.example.cardflare.getDeck
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.launch

@ExperimentalSnapperApi
@Composable
fun CardMenu(navController: NavController){ //is the menu you see when viewing individual flashcards in a deck
    val openedTarget: Deck = currentOpenedDeck.value ?: getDeck()
    val cards = openedTarget.cards
    var isFlipped by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val snapperBehavior = rememberSnapperFlingBehavior(listState)
    val coroutineScope = rememberCoroutineScope()
    val rotationYy by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing), label = ""
    )
    LaunchedEffect(Unit) {
        listState.scrollToItem(currentOpenFlashCard.value)
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        LazyRow(
            state = listState,
            flingBehavior = snapperBehavior,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(0.1f)
        ) {

            items(cards.size) { flashcardIndex->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.1f)
                        .width(LocalConfiguration.current.screenWidthDp.dp)
                        .graphicsLayer {
                            rotationY = rotationYy
                            cameraDistance = 8 * density // prevents distortion
                        }
                        .clickable { isFlipped = !isFlipped },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (rotationYy > 90f) rotationY = 180f
                            } //prevents the text from rendering right to left
                            .padding(20.dp)
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface,
                                shape = RoundedCornerShape(20.dp)
                            )

                    ) {
                        Text(
                            text = if (rotationYy > 90f)  cards[flashcardIndex].SideB else cards[flashcardIndex].SideA,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }


        //Action buttons row
        Row (horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .height(50.dp)){
            // Move one flashcard left if arrow left pressed
            Icon(
                painter = painterResource(id = R.drawable.nav_arrow_left),
                contentDescription = "left",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { coroutineScope.launch {
                    val prevIndex = maxOf(listState.firstVisibleItemIndex - 1, 0)
                    listState.animateScrollToItem(prevIndex)
                }} // handle boundary
            )
            // Rotate Flashcard
            Icon(
                painter = painterResource(id = R.drawable.redo),
                contentDescription = "left",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { isFlipped = !isFlipped} // Flip flashcard sides
            )

            // Move one flashcard right if arrow right pressed
            Icon(
                painter = painterResource(id = R.drawable.nav_arrow_right),
                contentDescription = "left",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        val nextIndex = minOf(listState.firstVisibleItemIndex + 1, cards.size - 1)
                        listState.animateScrollToItem(nextIndex)
                    }} // handle boundary
            )
        }
    }
}