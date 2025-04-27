package com.lortey.cardflare.uiRender

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.lortey.cardflare.AppSettings
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.Rating
import com.lortey.cardflare.reviewCard
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun LearnScreen(navController: NavController, context: Context, atFinnish:(() -> Unit)? = null) {
    var currentCardIndex by remember { mutableStateOf(0) }
    //CardsToLearn = arrayOf( Flashcard(1,"something", "sideB"))
    if (CardsToLearn == null) {
        throw IllegalArgumentException("LearnScreen called not CardsToLearn is null")
        navController.popBackStack()
    }
    var isReversed by remember { mutableStateOf(false) }

    // Reverse only once when the screen first loads
    /*LaunchedEffect(Unit) {
        if (!isReversed && CardsToLearn != null) {
            CardsToLearn.reverse()
            isReversed = true
        }
    }*/
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Enable depth buffering for the container
                compositingStrategy = CompositingStrategy.Offscreen
            }
    ) {
        CardsToLearn.reversed().forEachIndexed { cardIndex, card ->
            SwipeableFlashcard(
                flashcard = CardsToLearn[cardIndex],
                onSwipeWrong = {
                    currentCardIndex += 1

                    reviewCard(context = context, CardsToLearn[cardIndex], Rating.EASY)
                },
                onSwipeRight = {
                    currentCardIndex += 1
                    reviewCard(context = context, CardsToLearn[cardIndex], Rating.HARD)
                },
                modifierParsed = Modifier.background(
                    MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(20.dp)
                ),
                onTop = CardsToLearn.size - cardIndex - 1 == currentCardIndex
            )
        }
    }
    BackHandler {
        navController.popBackStack()
    }
    var isPoppingBack by remember { mutableStateOf(false) }
    if (CardsToLearn.size == currentCardIndex && !isPoppingBack){
        isPoppingBack = true
        if(atFinnish != null){
            atFinnish()
        }
        navController.popBackStack()
    }
}
@Composable
fun SwipeableFlashcard(
    flashcard: Flashcard,
    onSwipeWrong: () -> Unit,
    onSwipeRight: () -> Unit,
    modifierParsed: Modifier,
    onTop: Boolean) {

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val appSettings = AppSettings
    require(appSettings["Flashcard Swipe Threshold"]?.state is Float)
    require(appSettings["Flip Flashcard Right Wrong Answer"]?.state is Boolean)
    var fadeOut by remember { mutableStateOf(false) }
    val swipeThreshold = appSettings["Flashcard Swipe Threshold"]?.state as Float // Distance needed to register a swipe
    var isFlipped by remember { mutableStateOf(false) }
    val rotationYy by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing), label = ""
    )

    // Animate padding change using animateDpAsState
    val closeness by animateFloatAsState(
        targetValue = if (onTop) 1f else 0.7f, // Toggle between two padding values
        animationSpec = tween(durationMillis = 250)
    )
    val darknessValue by animateFloatAsState(
        targetValue = if (onTop) 1f else 0.7f,
        animationSpec = tween(durationMillis = 250)
    )
    AnimatedVisibility(
        visible = !fadeOut,
        exit = fadeOut(animationSpec = tween(durationMillis = 150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                //.padding(paddingValue)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Decide if the card should snap back or swipe away
                            coroutineScope.launch {
                                when {
                                    offsetX.value < -swipeThreshold -> {
                                        offsetX.animateTo(-1000f, tween(300))
                                        fadeOut = true
                                        if (appSettings["Flip Flashcard Right Wrong Answer"]?.state as Boolean) {
                                            onSwipeWrong()
                                        } else {
                                            onSwipeRight()
                                        }
                                    }

                                    offsetX.value > swipeThreshold -> {
                                        offsetX.animateTo(1000f, tween(300))
                                        fadeOut = true
                                        if (appSettings["Flip Flashcard Right Wrong Answer"]?.state as Boolean) {
                                            onSwipeRight()
                                        } else {
                                            onSwipeWrong()
                                        }
                                    }

                                    else -> {
                                        offsetX.animateTo(0f, tween(300)) // Reset position
                                    }
                                }
                            }
                        }
                    ) { _, dragAmount ->
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)  // Move card with finger
                        }
                    }
                }
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LocalConfiguration.current.screenWidthDp.dp)
                    .graphicsLayer {
                        if(onTop) {
                            rotationY = rotationYy
                            cameraDistance = 8 * density
                            // Add these critical properties:
                            shape = RectangleShape // Ensures clean edges during rotation
                            clip = true // Prevents content from bleeding through
                        }
                        scaleX = closeness
                        scaleY = closeness
                    }
                    // Add zIndex to ensure proper stacking
                    .zIndex(if (onTop) 1f else 0f)
                    .clickable { isFlipped = !isFlipped }
                    .padding(20.dp)
                    .border(
                        //Setting Border thickness
                        if (swipeThreshold < abs(offsetX.value)) {
                            (abs(offsetX.value) / 100 + 2).dp
                        } else {
                            0.dp
                        },
                        //Setting Color
                        if ((offsetX.value < 0 && appSettings["Flip Flashcard Right Wrong Answer"]?.state as Boolean == false)
                            || (offsetX.value > 0 && appSettings["Flip Flashcard Right Wrong Answer"]?.state as Boolean)
                        ) {
                            Color(0xff00cc99)
                        } else if ((offsetX.value < 0 && appSettings["Flip Flashcard Right Wrong Answer"]?.state as Boolean)
                            || (offsetX.value > 0 && appSettings["Flip Flashcard Right Wrong Answer"]?.state as Boolean == false)
                        ) {
                            Color(0xffff4d4d)
                        } else {
                            Color(0x00000000)
                        }, RoundedCornerShape(20.dp)
                    )
                    .background(
                        color = if (onTop) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.inverseOnSurface.darken(darknessValue),
                        shape = RoundedCornerShape(20.dp)
                    ),

                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = if (rotationYy > 90f) 180f else 0f
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AutoSizeText(text = if (rotationYy > 90f) flashcard.SideB else flashcard.SideA,
                        color = if (onTop) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.inverseSurface.darken(darknessValue),
                    )
                }
            }
        }
    }
}
inline fun Color.darken(darkenBy: Float = 0.3f): Color {
    return copy(
        red = red * darkenBy,
        green = green * darkenBy,
        blue = blue * darkenBy,
        alpha = alpha
    )
}
