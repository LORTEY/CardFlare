package com.lortey.cardflare.uiRender

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.icu.text.Transliterator.Position
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import com.lortey.cardflare.AppSettings
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.Rating
import com.lortey.cardflare.reviewCard
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import androidx.core.graphics.toColorInt
import kotlin.random.Random.Default.nextBoolean


@Composable
fun LearnScreen(navController: NavController, context: Context, atFinnish:(() -> Unit)? = null) {
    var currentCardIndex by remember { mutableStateOf(0) }
    //CardsToLearn = arrayOf( Flashcard(1,"something", "sideB"))
    if (CardsToLearn.isEmpty()) {
        //throw IllegalArgumentException("LearnScreen called not CardsToLearn is null")
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
                onSwipe = { rating->
                    currentCardIndex += 1
                    reviewCard(context = context, CardsToLearn[cardIndex], rating)
                },
                modifierParsed = Modifier.background(
                    MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(20.dp)
                ),
                onTop = CardsToLearn.size - cardIndex - 1 == currentCardIndex,
                context = context
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
    onSwipe: (Rating) -> Unit,
    modifierParsed: Modifier,
    onTop: Boolean,
    context: Context) {
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels
    var offset = remember { Animatable(IntOffset(0, 0), IntOffset.VectorConverter) }

    val coroutineScope = rememberCoroutineScope()
    val appSettings = AppSettings
    require(appSettings["Flashcard Swipe Threshold"]?.state is Float)
    var fadeOut by remember { mutableStateOf(false) }
    val swipeThreshold = appSettings["Flashcard Swipe Threshold"]?.state as Float // Distance needed to register a swipe
    var isFlipped by remember { mutableStateOf(nextBoolean()) }
    val rotationYy by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing), label = ""
    )
    val alphaValue = alphaValueBasedOnDistance(offset.value.x,offset.value.y, context, swipeThreshold) * 1.2f // multiplied for the effects to appear closer to the center
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
                    detectDragGestures(
                        onDragEnd = {
                            // Decide if the card should snap back or swipe away
                            coroutineScope.launch {

                                when (getClosestCorner(offset.value.x, offset.value.y, context, swipeThreshold)) {
                                    Points.CENTRE -> {
                                        offset.animateTo(IntOffset(0, 0), tween(300))
                                    }
                                    Points.TOP_LEFT -> {
                                        offset.animateTo(IntOffset(-screenWidth, -screenHeight), tween(300))
                                        fadeOut = true
                                        onSwipe(actionsAndColors[Points.TOP_LEFT]!!.second!!)
                                    }
                                    Points.TOP_RIGHT -> {
                                        offset.animateTo(IntOffset(screenWidth, -screenHeight), tween(300))
                                        fadeOut = true
                                        onSwipe(actionsAndColors[Points.TOP_RIGHT]!!.second!!)
                                    }
                                    Points.BOTTOM_LEFT -> {
                                        offset.animateTo(IntOffset(-screenWidth, screenHeight), tween(300))
                                        fadeOut = true
                                        onSwipe(actionsAndColors[Points.BOTTOM_LEFT]!!.second!!)
                                    }
                                    Points.BOTTOM_RIGHT -> {
                                        offset.animateTo(IntOffset(screenWidth, screenHeight), tween(300))
                                        fadeOut = true
                                        onSwipe(actionsAndColors[Points.BOTTOM_RIGHT]!!.second!!)
                                    }
                                }
                                }

                        }
                    ) { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offset.animateTo(
                                offset.value
                                        + IntOffset(
                                    dragAmount.x.roundToInt(),
                                    dragAmount.y.roundToInt()
                                ), tween(0)
                            ) // Updates both x and y
                        }
                    }
                }
                .offset { IntOffset(offset.value.x, offset.value.y) },
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LocalConfiguration.current.screenWidthDp.dp)
                    .graphicsLayer {
                        rotationY = rotationY
                        if(onTop) {
                            cameraDistance = 8 * density
                            shape = RectangleShape // Ensures clean edges during rotation
                            clip = true // Prevents content from bleeding through
                        }
                        scaleX = closeness
                        scaleY = closeness
                    }
                    // Add zIndex to ensure proper stacking
                    .zIndex(if (onTop) 1f else 0.5f)
                    .clickable { isFlipped = !isFlipped }
                    .padding(20.dp)
                    .border(
                        width = 5.dp,
                        color = actionsAndColors[getClosestCorner(positionX = offset.value.x, positionY = offset.value.y ,
                            context = context, minimalSwipeThreshold = swipeThreshold)]!!.first.copy(alpha = alphaValue)
                        , RoundedCornerShape(20.dp)
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

                    AutoSizeText(
                        text = if (rotationYy > 90f) flashcard.SideB else flashcard.SideA,
                        color = (if (onTop) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.inverseSurface.darken(
                            darknessValue
                        )).copy(alpha = 1f - alphaValue),
                    )
                    val closestCorner = getClosestCorner(positionX = offset.value.x, positionY = offset.value.y, context = context, swipeThreshold)
                    //Rating enumerator
                    Text(text = actionsAndColors[closestCorner]?.second.toString() ?: "",
                        color = actionsAndColors[closestCorner]!!.first.copy(alpha = alphaValue),
                        modifier =  Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.displaySmall)
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
fun alphaValueBasedOnDistance(positionX: Int, positionY:Int, context: Context, minimalSwipeThreshold:Float):Float{
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels
    val maxDistance = distanceFrom(screenHeight/2,screenWidth/2, 0,0)
    val currentDistance = distanceFrom(positionX,positionY, 0,0)
    val alpha = (currentDistance - minimalSwipeThreshold)/(maxDistance - minimalSwipeThreshold)
    return if(alpha<0) 0f else alpha

}

//val configuration = LocalConfiguration.current
fun getClosestCorner(positionX: Int, positionY:Int, context: Context, minimalSwipeThreshold:Float):Points{
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels

    if(distanceFrom(positionX,positionY, 0, 0   ) < minimalSwipeThreshold){
        return Points.CENTRE
    }

    if(positionY < 0){ //So its closest to the top
        if(positionX < 0){
            return Points.TOP_LEFT
        }
        return Points.TOP_RIGHT
    }
    if(positionX < 0){
        return Points.BOTTOM_LEFT
    }
    return Points.BOTTOM_RIGHT
}

fun distanceFrom(Ax:Int,Ay:Int,Bx:Int,By:Int):Float{
    return sqrt(((Ax-Bx) * (Ax-Bx)  + (Ay-By) * (Ay-By)).toFloat())
}

enum class Points{
    TOP_RIGHT,
    TOP_LEFT,
    CENTRE,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

public val actionsAndColors : Map<Points,Pair<Color, Rating?>> =
    mapOf(
        Points.TOP_RIGHT to Pair(Color("#33cc33".toColorInt()), Rating.EASY),
        Points.TOP_LEFT to Pair(Color("#00ccff".toColorInt()), Rating.GOOD),
        Points.CENTRE to Pair(Color("#00000000".toColorInt()), null),
        Points.BOTTOM_RIGHT to Pair(Color("#ff9933".toColorInt()), Rating.HARD),
        Points.BOTTOM_LEFT to Pair(Color("#ff0000".toColorInt()), Rating.AGAIN)

    )