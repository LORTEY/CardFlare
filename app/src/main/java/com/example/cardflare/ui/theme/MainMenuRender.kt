package com.example.cardflare.ui.theme

import android.R.attr.maxLines
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color.parseColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardflare.AppInfo
import com.example.cardflare.AppSettings
import com.example.cardflare.Category
import com.example.cardflare.Chooser
import com.example.cardflare.Deck
import com.example.cardflare.Flashcard
import com.example.cardflare.GetListOfApps
import com.example.cardflare.R
import com.example.cardflare.SettingEntry
import com.example.cardflare.SettingsType
import com.example.cardflare.SortType
import com.example.cardflare.addDeck
import com.example.cardflare.addFlashcard
import com.example.cardflare.createTranslator
import com.example.cardflare.loadData
import com.example.cardflare.sortDecks
import com.example.cardflare.updateSetting
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


//This file contains all the ui
var currentOpenedDeck : Deck? by mutableStateOf(null)
var appearAddMenu by mutableStateOf(false)
var isAscending by  mutableStateOf(true)
var appearSortMenu by mutableStateOf(false)
var sortType by mutableStateOf(SortType.ByName)
var qualifiedDecks = listOf<Deck>()
var currentOpenFlashCard by mutableStateOf(0)
var cardsSelected = mutableStateListOf( *Array(0) { 0 })
var deckAddMenu by mutableStateOf(false)
var CardsToLearn: MutableList<Flashcard> = mutableListOf()
public var renderMainMenu by mutableStateOf(true)
var decks : Array<Deck> =  arrayOf<Deck>(Deck("",0,0, listOf<String>(), listOf<Flashcard>()))
var translatedFlashcardSide:String = ""
public fun reloadDecks(context: Context){
    decks = loadData("", context = context)
}
@Composable
fun MainMenuRender(navController: NavHostController, context: Context) {
    decks = loadData("", context = context)
    var searchQuery by remember { mutableStateOf("") }
    var appear by remember { mutableStateOf(false) }
    qualifiedDecks = sortDecks(searchQuery, decks, sortType = sortType, isAscending)

    BackHandler { // Handle the back button press
        if (appear || appearAddMenu){
            appearAddMenu = false
            appear = false
        }else{
            navController.popBackStack()
        }
    }
    if(renderMainMenu){
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {

                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(70.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1.0f)
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
                            //files = listOf("ghf", "dfg","wedfhiuoidu","sdhe","sdiu","ghf", "dfg","wedfhiuoidu","sdhe","sdiu","ghf", "dfg","wedfhiuoidu","sdhe","sdiu")
                                items(qualifiedDecks.size) { index ->
                                    Text(
                                        text = qualifiedDecks[index].name,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier
                                            .shadow(
                                                elevation = 10.dp,
                                                shape = RoundedCornerShape(10.dp),
                                                clip = false
                                            )
                                            .background(
                                                MaterialTheme.colorScheme.inverseOnSurface
                                            )
                                            .fillMaxWidth(0.5f)
                                            .height(100.dp)
                                            .padding(10.dp)
                                            .clickable {
                                                currentOpenedDeck = qualifiedDecks[index];
                                                navController.navigate("deck_menu")
                                            },
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )


                                }
                        }

                        // Add menu
                        if (appearAddMenu || appear) {
                            // Will make all menus hide if box and not them is clicked
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                appearAddMenu = false
                                                appear = false
                                            }
                                        )
                                    }
                            )
                        }

                        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                            Column(modifier = Modifier
                                .width(128.dp)
                                .align(Alignment.End)) {
                                PopAddMenu(context = context,navController)
                            }
                        }
                    }
                }

                // Fade effect and upper menu
                Column(modifier = Modifier.height(90.dp)) {

                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp))

                    //Row holding the menu icon and search field
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .height(50.dp)
                            .background(
                                /*
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), // Start color
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f) // End color
                                    )
                                ),*/
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                    ) {

                        // "More" Menu button
                        Icon(
                            painter = painterResource(id = R.drawable.menu),
                            contentDescription = "chart",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable { appear = !appear }
                                .width(40.dp)
                        )

                        // I have no idea how to use the colors in TextField so to make a place holder I used this box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .weight(1.0f)
                                .background(
                                    Color(android.graphics.Color.parseColor("#00000000")),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .align(Alignment.CenterVertically)
                        ) {
                            // Text field for searching card decks
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it; qualifiedDecks = sortDecks(searchQuery, decks, sortType = sortType, true)},
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search Sets...",
                                    color = MaterialTheme.colorScheme.onBackground, // Placeholder text color
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                            }
                        }

                        // "Sort" Menu button
                       Column(){
                           Icon(
                               painter = painterResource(id = R.drawable.sort_ascending),
                               contentDescription = "chart",
                               tint = MaterialTheme.colorScheme.primary,
                               modifier = Modifier
                                   .fillMaxHeight()
                                   .clickable { appearSortMenu = !appearSortMenu }
                                   .width(40.dp)
                           )
                           DropdownMenu(
                               expanded = appearSortMenu,
                               onDismissRequest = { appearSortMenu = false }, // Close menu on dismiss
                               modifier = Modifier
                                   .width(200.dp)
                                   .background(MaterialTheme.colorScheme.inverseOnSurface)
                                   .padding(start = 16.dp)
                           ) {
                               SortMenuContent(decks = decks, searchQuery = searchQuery)
                           }
                       }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.background,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0f)
                                    )
                                )
                            )
                    ){}
                }

                // left slide menu
                AnimatedVisibility(
                    visible = appear,
                    enter = fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                        animationSpec = tween(200)
                    ) { fullWidth -> -fullWidth / 2 },
                    exit = fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                        animationSpec = tween(200)
                    ) { fullWidth -> -fullWidth / 2 }
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.inverseOnSurface)
                            .fillMaxHeight()
                            .fillMaxWidth(0.4f)
                    ) {
                        SlideMenuContent(navController)
                    }
                }
            }
        }
    }
}

// loads the screen when you click certain deck
@Composable
fun deckScreen(context: Context, navController: NavController){
    val openedTarget: Deck = currentOpenedDeck ?: Deck("",0,0, listOf<String>(), listOf<Flashcard>())
    val cards = openedTarget.cards
    var selectMode by remember{ mutableStateOf(false) }
    //var cards = arrayOf(arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"))
    cardsSelected = remember {  mutableStateListOf( *Array(cards.size) { 0 })}

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
                        Log.d("Cards2",cardsSelected[index].toString())
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
fun DeckAddMenu(navController: NavController){ // nothing here yet
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
                )}
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
                    .clickable {CardsToLearn.clear(); cardsSelected.forEachIndexed { index,value-> if(value == 1) CardsToLearn.add(currentOpenedDeck!!.cards[index])}; if (CardsToLearn.size == 0) CardsToLearn = currentOpenedDeck!!.cards.toMutableList(); navController.navigate("learn_screen")},
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
                        )}
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
                        )}
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

@ExperimentalSnapperApi
@Composable
fun CardMenu(navController: NavController){ //is the menu you see when viewing individual flashcards in a deck
    val openedTarget: Deck = currentOpenedDeck ?: Deck("",0,0, listOf<String>(), listOf<Flashcard>())
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
        listState.scrollToItem(currentOpenFlashCard)
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


// definition used for rendering components of left slide menu used by MainMenuRender function
@Composable
fun SlideMenuContent(navController: NavController){
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable { navController.navigate("settings") }){
        Icon(
            painter = painterResource(id = R.drawable.settings),
            contentDescription = "chart",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = "Settings", color = MaterialTheme.colorScheme.primary)

    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable { navController.navigate("main_menu")}){
        Icon(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "chart",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = "Home", color = MaterialTheme.colorScheme.primary)
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable { navController.navigate("launch_on_manager")}){
        Icon(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "chart",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = "Launch On Options", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun MyOverlayComposable() {
    renderMainMenu = false
    Log.d("BackgroundService","Overlay")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(android.graphics.Color.parseColor("#709DBFF2"))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hello, Overlay!",
            color = Color.White,
            fontSize = 24.sp
        )
    }
}
@Composable
fun AddDeckScreen(context: Context,navController: NavController){
    var DeckName by remember{ mutableStateOf("") }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Text("Name", color = MaterialTheme.colorScheme.primary)
        BasicTextField(
            value = DeckName,
            onValueChange = { DeckName = it },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text("Add", Modifier.clickable { addDeck(context, DeckName); navController.popBackStack()})
    }
}
// The menu that appears when you click the add button in right bottom of screen
@Composable
fun PopAddMenu(context: Context, navController: NavController){
    AnimatedVisibility(
                visible = appearAddMenu,
                enter = fadeIn(animationSpec = tween(100)) + slideInVertically (
                    animationSpec = tween(100)
                ) { fullWidth -> fullWidth / 2 },
                exit = fadeOut(animationSpec = tween(100)) + slideOutVertically (
                    animationSpec = tween(100)
                ) { fullWidth -> fullWidth / 2 }
            ) {

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface,
                                shape = RoundedCornerShape(128.dp)
                            )
                            .fillMaxWidth(0.5f), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Here are all buttons for the menu
                        Icon(
                            painter = painterResource(id = R.drawable.addempty),
                            contentDescription = "chart",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(15.dp)
                                .clickable { navController.navigate("deck_add_screen") }
                        )
                        Icon(
                                painter = painterResource(id = R.drawable.bar_chart_2),
                        contentDescription = "chart",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(15.dp)
                            .clickable {}
                        )
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
            .clickable { appearAddMenu = !appearAddMenu }
    )
}

@Composable
fun SortMenuContent(decks: Array<Deck>, searchQuery:String){

    Text("Sort By", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)

    Text("Name (Ascending)",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .clickable {
                sortType = SortType.ByName
                appearSortMenu = false
                isAscending = true
                qualifiedDecks = sortDecks(
                    searchQuery,
                    decks,
                    sortType = sortType,
                    isAscending
                )
            }
            .fillMaxWidth()
            .padding(vertical = 5.dp))

    Text("Name (Descending)",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .clickable {
                sortType = SortType.ByName
                appearSortMenu = false
                isAscending = false
                qualifiedDecks = sortDecks(
                    searchQuery,
                    decks,
                    sortType = sortType,
                    isAscending
                )
            }
            .fillMaxWidth()
            .padding(vertical = 5.dp))

    Text("Last Modified (Ascending)",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .clickable {
                sortType = SortType.ByLastEdited
                appearSortMenu = false
                isAscending = true
                qualifiedDecks = sortDecks(
                    searchQuery,
                    decks,
                    sortType = sortType,
                    isAscending
                )
            }
            .fillMaxWidth()
            .padding(vertical = 5.dp))

    Text("Date Modified (Descending)",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .clickable {
                sortType = SortType.ByLastEdited
                appearSortMenu = false
                isAscending = false
                qualifiedDecks = sortDecks(
                    searchQuery,
                    decks,
                    sortType = sortType,
                    isAscending
                )
            }
            .fillMaxWidth()
            .padding(vertical = 5.dp))

    Text("Date Created (Ascending)",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .clickable {
                sortType = SortType.ByLastEdited
                appearSortMenu = false
                isAscending = true
                qualifiedDecks = sortDecks(
                    searchQuery,
                    decks,
                    sortType = sortType,
                    isAscending
                )
            }
            .fillMaxWidth()
            .padding(vertical = 5.dp))

    Text("Date Created (Descending)",
    fontSize = 16.sp,
    color = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier
        .clickable {
            sortType = SortType.ByLastEdited
            appearSortMenu = false
            isAscending = false
            qualifiedDecks = sortDecks(
                searchQuery,
                decks,
                sortType = sortType,
                isAscending
            )
        }
        .fillMaxWidth()
        .padding(vertical = 5.dp))
}

@Composable
fun SettingsMenu(navController: NavHostController, context: Context) {
    val appSettings = remember { AppSettings } // ✅ Directly reference mutableStateMapOf

    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // Correctly loop through categories
        Category.entries.forEach { category ->
            val filtered = appSettings.values.filter { it.category == category }

            if (filtered.isNotEmpty()) {
                Log.d("FilteredCardFlare", filtered.toString())

                item { // Correctly add category title
                    Text(
                        text = category.toString().replace("_", " "),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                }

                // Use `items(filtered)` instead of manual indexing
                items(filtered) { setting ->
                    SettingsEntryComposable(setting, appSettings, context)
                }
            }
        }
    }
}



@Composable
fun SettingsEntryComposable(setting: SettingEntry, appSettings: Map<String, SettingEntry>,context: Context) {
    var openPopup by remember { mutableStateOf(false) }
    if (openPopup) {
        Popup(
            alignment = Alignment.TopStart,
            //offset = IntOffset(popupPosition.x.toInt(), popupPosition.y.toInt())
        ) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.inverseOnSurface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(16.dp)
                    .border(1.dp, Color.Gray, MaterialTheme.shapes.small)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(setting.description!!, style = MaterialTheme.typography.titleMedium,color = MaterialTheme.colorScheme.onBackground)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { openPopup = false }) {
                        Text("Close")
                    }
                }
            }
        }
    }
        if ((setting.type == SettingsType.BOOLEAN && setting.customChooser == Chooser.NonSpecified) || setting.customChooser == Chooser.Switch) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { openPopup = true }
                        .padding(vertical = 10.dp)
                )
                Text(setting.name, modifier = Modifier.padding(vertical = 10.dp),color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.weight(1f))
                val state = remember { mutableStateOf(setting.state as Boolean) }
                Switch(
                    checked = state.value,
                    onCheckedChange = { newValue ->
                        state.value = newValue // Update local state
                        updateSetting(setting.name, newValue) // Update appSettings state
                    }
                )
            }
        }else if ((setting.type == SettingsType.SLIDER && setting.customChooser == Chooser.NonSpecified) || setting.customChooser == Chooser.Slider) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)){
                val state = remember { mutableStateOf(setting.state as Float) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { openPopup = true }
                        .padding(vertical = 10.dp)
                )
                Text(setting.name, modifier = Modifier.padding(vertical = 10.dp))
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface), horizontalArrangement = Arrangement.SpaceEvenly){
                Text(
                    text = state.value.toString(),
                )}
            }
            Slider(
                value = state.value,
                onValueChange = { newValue ->
                    state.value = newValue
                    updateSetting(setting.name, newValue)
                },
                valueRange = 0f..800f,
                steps = 15,
                modifier = Modifier.fillMaxWidth()
            )
        }
        }
    }


@Composable
fun LearnScreen(navController: NavController, context: Context) {
    var currentCardIndex by remember { mutableStateOf(0) }
    //CardsToLearn = arrayOf( Flashcard(1,"something", "sideB"))
    if (CardsToLearn == null) {
        throw IllegalArgumentException("LearnScreen called not CardsToLearn is null")
        navController.popBackStack()
    }
    CardsToLearn.reverse()// a flip is needed because the last cards in list will; appear first in the foreach loop

    CardsToLearn.forEachIndexed() { cardIndex, card->
        SwipeableFlashcard(
            flashcard = CardsToLearn[cardIndex],
            onSwipeWrong = {
                currentCardIndex += 1
                //flashcards = flashcards.drop(1).toMutableList()
            },
            onSwipeRight = {
                currentCardIndex += 1
                //flashcards = flashcards.drop(1).toMutableList()
            },
            modifierParsed = Modifier.background(
                MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(20.dp)
            ),
            onTop = CardsToLearn.size - cardIndex - 1 == currentCardIndex
        )
    }
    BackHandler {
        navController.popBackStack()
    }
    var isPoppingBack by remember { mutableStateOf(false) }
    if (CardsToLearn.size == currentCardIndex && !isPoppingBack){
        isPoppingBack = true
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
    val paddingValue by animateDpAsState(
        targetValue = if (onTop) 0.dp else 32.dp, // Toggle between two padding values
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)
    )
    val darknessValue by animateFloatAsState(
        targetValue = if (onTop) 1f else 0.7f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)
    )
    AnimatedVisibility(
        visible = !fadeOut,
        exit = fadeOut(animationSpec = tween(durationMillis = 150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
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
                        rotationY = rotationYy
                        cameraDistance = 8 * density // prevents distortion
                    }
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
                            if (rotationYy > 90f) rotationY = 180f
                        } //prevents the text from rendering right to left
                        .padding(20.dp),
                    contentAlignment = Alignment.Center

                ) {
                    Text(
                        text = if (rotationYy > 90f) flashcard.SideB else flashcard.SideA,
                        color = if (onTop) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.inverseSurface.darken(darknessValue),
                        textAlign = TextAlign.Center
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
@Composable
fun AddFlashcardScreen(navController: NavController, context: Context){
    var textStateA by remember {  mutableStateOf("") }
    var textStateB by remember { mutableStateOf("") }
    var defaultStateA by remember {  mutableStateOf("") }
    var defaultStateB by remember { mutableStateOf("") }
    val translator = remember { createTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.POLISH) }
    translator.downloadModelIfNeeded()
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column {
            Text("Side A", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

            TextField(
                value = textStateA,
                placeholder = {Text(defaultStateA, color = MaterialTheme.colorScheme.inverseSurface.copy(alpha=0.8f))},
                onValueChange = { newText -> textStateA = newText; if(textStateB.isBlank()) translateText(textStateA, translator){result-> defaultStateB = result}},
                //label = { Text(text = if(textStateB.isEmpty()) "Enter Text Of Side A" else translatedFlashcardSide,color = MaterialTheme.colorScheme.inverseSurface, style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp, max = 200.dp)
                    .background(color = MaterialTheme.colorScheme.inverseOnSurface),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    focusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                ),
                maxLines = 5,
                minLines = 1
            )

            Text("Side B", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            TextField(
                value = textStateB,
                placeholder = {Text(defaultStateB, color = MaterialTheme.colorScheme.inverseSurface.copy(alpha=0.8f))},
                onValueChange = { newText:String -> textStateB = newText; if(textStateA.isBlank()) translateText(textStateB, translator){result-> defaultStateA = result} },
                //label = { Text(text = if(textStateB.isBlank()) "Enter Text Of Side B" else translatedFlashcardSide,color = MaterialTheme.colorScheme.inverseSurface, style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp, max = 200.dp)
                    .background(color = MaterialTheme.colorScheme.inverseOnSurface),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    focusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                ),
                maxLines = 5,
                minLines = 1
            )
                val ableToAdd = (textStateA.isNotBlank() || defaultStateA.isNotBlank()) && (textStateB.isNotBlank() || defaultStateB.isNotBlank())

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 20.dp)
                    .background(color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = if (ableToAdd) 1f else 0.5f))
                    .height(50.dp)
                    .clickable {
                        if (ableToAdd) {
                            addFlashcard(
                                currentOpenedDeck!!.name,
                                Flashcard(
                                    0,
                                    SideA = if (textStateA.isNotBlank()) textStateA else defaultStateA,
                                    SideB = if (textStateB.isNotBlank()) textStateB else defaultStateB
                                ), context = context
                            )
                            currentOpenedDeck =
                                loadData(fileName = currentOpenedDeck!!.name, context = context)[0]
                            defaultStateA = ""
                            defaultStateB = ""
                            textStateA = ""
                            textStateB = ""
                        }
                    },
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(
                        "Add", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = if (ableToAdd) 1f else 0.5f),
                    )
                }
            }
        }
    }
fun translateText(text: String, translator: Translator, onResult: (String) -> Unit) {
    Log.d("CardflareTranslator","hii");
    translator.translate(text)
        .addOnSuccessListener { translatedText -> onResult(translatedText) }
        .addOnFailureListener { e -> onResult("Translation failed: ${e.message}") }
}
@Composable
fun LaunchOnMenu(context: Context, navController: NavController){
    val apps = remember {GetListOfApps(context)}
    Log.d("cardflareSS", apps.toString())
    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        items(apps){app->
            Row(){
                AppItem(app)
            }
        }
    }

}
@Composable
fun AppItem(appInfo: AppInfo) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Image(
            bitmap = drawableToBitmap(appInfo.icon).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = appInfo.name, style = MaterialTheme.typography.bodyLarge)
    }
}
fun drawableToBitmap(drawable: Drawable?): Bitmap {
    if (drawable == null) {
        // Return an empty 1x1 transparent bitmap if drawable is null
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            eraseColor(parseColor("#00000000"))
        }
    }
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
@OptIn(ExperimentalSnapperApi::class)
@Preview()
@Composable
fun preview(){
    Material3AppTheme{
                // initialize the NavController
                val navController = rememberNavController()
                // navigation graph
                NavHost(
                    navController = navController,
                    startDestination = "launch_on_manager"
                ) {
                    composable("launch_on_manager") { LaunchOnMenu(navController = navController, context = LocalContext.current) }
                    composable("main_menu") { MainMenuRender(navController, context = LocalContext.current) }
                    composable("card_menu") { CardMenu(navController) }
                    composable("learn_screen") { LearnScreen(navController,context = LocalContext.current)}
                    composable("deck_menu") { deckScreen(context = LocalContext.current,navController) }
                    composable("settings") { SettingsMenu(navController,context = LocalContext.current) }
                    composable("deck_add_screen") { AddDeckScreen(context = LocalContext.current, navController) }
                    composable("add_flashcard") { AddFlashcardScreen(context = LocalContext.current, navController = navController) }}
            }
}