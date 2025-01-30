package com.example.cardflare.ui.theme

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardflare.R
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.DropdownMenu
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.cardflare.Deck
import com.example.cardflare.SortType
import com.example.cardflare.loadData
import com.example.cardflare.sortDecks


var currentOpenedDeck : Deck? by mutableStateOf(null);
var appearAddMenu by mutableStateOf(false)
var isAscending by  mutableStateOf(true)
var appearSortMenu by mutableStateOf(false)
var sortType by mutableStateOf(SortType.ByName)
var qualifiedDecks = listOf<Deck>()
var currentOpenFlashCard by mutableStateOf(0)
var deckAddMenu by mutableStateOf(false)
public var renderMainMenu by mutableStateOf(true)

@Composable
fun MainMenuRender(navController: NavHostController, decks : Array<Deck>) {
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
            modifier = Modifier.background(Color(ColorPalette.sa10))
                .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(ColorPalette.sa10))
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
                            //files = listOf("ghf", "dfg","wedfhiuoidu","sdhe","sdiu","ghf", "dfg","wedfhiuoidu","sdhe","sdiu","ghf", "dfg","wedfhiuoidu","sdhe","sdiu");
                                items(qualifiedDecks.size) { index ->
                                    Text(
                                        text = qualifiedDecks[index].name,
                                        color = Color(ColorPalette.pa0),
                                        modifier = Modifier
                                            .shadow(
                                                elevation = 10.dp,
                                                shape = RoundedCornerShape(10.dp),
                                                clip = false
                                            )
                                            .background(

                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(ColorPalette.sa30), // Start color
                                                        Color(ColorPalette.sa20) // End color
                                                    )
                                                )
                                            )
                                            .fillMaxWidth(0.5f)
                                            .height(100.dp)
                                            .padding(10.dp)
                                            .clickable { currentOpenedDeck = qualifiedDecks[index]; navController.navigate("deck_menu") }
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
                            Column(modifier = Modifier.width(128.dp).align(Alignment.End)) {
                                PopAddMenu()
                            }
                        }
                    }
                }

                // Fade effect and upper menu
                Column(modifier = Modifier.height(90.dp)) {

                    Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

                    //Row holding the menu icon and search field
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .height(50.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(ColorPalette.sa30), // Start color
                                        Color(ColorPalette.sa20) // End color
                                    )
                                ), shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                    ) {

                        // "More" Menu button
                        Icon(
                            painter = painterResource(id = R.drawable.menu),
                            contentDescription = "chart",
                            tint = Color(ColorPalette.pa40),
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
                                    color = Color(ColorPalette.pa50),
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search Sets...",
                                    color = Color(ColorPalette.sa40), // Placeholder text color
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                            }
                        }

                        // "Sort" Menu button
                       Column(){
                           Icon(
                               painter = painterResource(id = R.drawable.sort_ascending),
                               contentDescription = "chart",
                               tint = Color(ColorPalette.pa40),
                               modifier = Modifier
                                   .fillMaxHeight()
                                   .clickable {appearSortMenu = !appearSortMenu; }
                                   .width(40.dp)
                           )
                           DropdownMenu(
                               expanded = appearSortMenu,
                               onDismissRequest = { appearSortMenu = false }, // Close menu on dismiss
                               modifier = Modifier
                                   .width(200.dp)
                                   .background(Color(ColorPalette.sa20))
                                   .padding(start = 16.dp)
                           ) {
                               SortMenuContent(decks = decks, searchQuery = searchQuery)
                           }
                       }


                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth().height(90.dp)
                    )
                    {
                        // Fade effect gradient
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(ColorPalette.sa10),
                                    Color(android.graphics.Color.parseColor("#00000000"))
                                ),
                                start = Offset(0f, 0f), // Top
                                end = Offset(0f, size.height)
                            )
                        )
                    }
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
                            .background(Color(ColorPalette.sa50))
                            .fillMaxHeight()
                            .fillMaxWidth(0.4f)
                    ) {
                        SlideMenuContent()
                    }
                }
            }
        }
    }
}

// loads the screen when you click certain deck
@Composable
fun deckScreen(context: Context, navController: NavController){
    val openedTarget: Deck = currentOpenedDeck ?: Deck("",0,0, listOf<String>(), listOf<Array<String>>())
    val cards = openedTarget.cards
    var selectMode by remember{ mutableStateOf(false) }
    //var cards = arrayOf(arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"))
    var cardsSelected = remember {  mutableStateListOf( *Array(cards.size) { 0 })}

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
        modifier = Modifier.background(Color(ColorPalette.sa10))
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(ColorPalette.sa10))
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
                            brush = Brush.verticalGradient(
                                colors = if (cardsSelected[index]==0) listOf(Color(ColorPalette.sa30), Color(ColorPalette.sa20)) else listOf(Color(ColorPalette.pa20), Color(ColorPalette.pa0))
                            )
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    cardsSelected[index] = 1
                                    selectMode = true
                                },
                                onTap = {
                                    if (selectMode){
                                        cardsSelected[index] = 1 - cardsSelected[index] //flips ones to zeroes and vice versa
                                        if (cardsSelected.count { it == 1 } == 0){
                                            // if no more selected cards left stop select mode
                                            selectMode = false
                                        }
                                    }else{
                                        currentOpenFlashCard = index
                                        Log.d("CardFlare2","clicked")
                                        navController.navigate("card_menu")
                                    }
                                }
                            )
                        }
                        .fillMaxWidth(1f/2f)
                        .height((250/3f).dp)
                        .padding(10.dp)
                    ){
                        Log.d("Cards2",cardsSelected[index].toString())
                        Text(
                            text = cards[index][0].toString(),
                            color = Color(ColorPalette.pa50),
                            fontWeight = FontWeight.Bold
                            )
                        Text(
                            text = cards[index][1].toString(),
                            color = Color(ColorPalette.sa40),
                            modifier = Modifier.padding(vertical = 4.dp)
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
                Column(modifier = Modifier.width(128.dp).align(Alignment.End)) {
                    DeckAddMenu()
                }
            }
        }
    }

}
@Composable
fun DeckAddMenu(){
    AnimatedVisibility(
        visible = deckAddMenu,
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
                modifier = Modifier.background(
                    Color(ColorPalette.sa50),
                    shape = RoundedCornerShape(128.dp)
                ).fillMaxWidth(0.5f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Here are all buttons for the menu
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "chart",
                    tint = Color(ColorPalette.pa40),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(15.dp)
                        .clickable{}
                )
                Icon(
                    painter = painterResource(id = R.drawable.bar_chart_2),
                    contentDescription = "chart",
                    tint = Color(ColorPalette.pa40),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(15.dp)
                        .clickable{}
                )
            }
        }
    }

    Icon(
        painter = painterResource(id = R.drawable.plus_circle_solid),
        contentDescription = "chart",
        tint = Color(ColorPalette.pa40),
        modifier = Modifier
            .size(128.dp)
            .padding(15.dp)
            .background(Color(ColorPalette.sa10), shape = CircleShape)
            .clickable { deckAddMenu = !deckAddMenu}
    )
}

@Composable
fun cardMenu(navController: NavController){
    val openedTarget: Deck = currentOpenedDeck ?: Deck("",0,0, listOf<String>(), listOf<Array<String>>())
    val cards = openedTarget.cards
    var isFlipped by remember { mutableStateOf(false) }
    val rotationYy by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing), label = ""
    )
    Column(
        modifier = Modifier.background(Color(ColorPalette.sa10))
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotationYy
                        cameraDistance = 8 * density // prevents distortion
                    }
                    .weight(0.1f)
                    .clickable { isFlipped = !isFlipped },
                contentAlignment = Alignment.Center
            ) {
                if (rotationYy <= 90f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(ColorPalette.sa30), Color(ColorPalette.sa20))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )

                    ) {
                        Text(
                            text = cards[currentOpenFlashCard][0],
                            color = Color(ColorPalette.pa50),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f } //prevents the text from rendering right to left
                            .padding(20.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(ColorPalette.sa30), Color(ColorPalette.sa20))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )

                    ) {
                        Text(
                            text = cards[currentOpenFlashCard][1],
                            color = Color(ColorPalette.pa50),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }


        //Action buttons row
        Row (horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.padding(20.dp).fillMaxWidth().height(50.dp)){
            // Move one flashcard left if arrow left pressed
            Icon(
                painter = painterResource(id = R.drawable.nav_arrow_left),
                contentDescription = "left",
                tint = Color(ColorPalette.pa0),
                modifier = Modifier.clickable { if(currentOpenFlashCard > 0) currentOpenFlashCard-=1 } // handle boundary
            )
            // Rotate Flashcard
            Icon(
                painter = painterResource(id = R.drawable.redo),
                contentDescription = "left",
                tint = Color(ColorPalette.pa0),
                modifier = Modifier.clickable { isFlipped = !isFlipped} // Flip flashcard sides
            )

            // Move one flashcard right if arrow right pressed
            Icon(
                painter = painterResource(id = R.drawable.nav_arrow_right),
                contentDescription = "left",
                tint = Color(ColorPalette.pa0),
                modifier = Modifier.clickable { if(currentOpenFlashCard < cards.size - 1) currentOpenFlashCard+=1 } // handle boundary
            )
        }
    }
}


// definition used for rendering components of left slide menu used by MainMenuRender function
@Composable
fun SlideMenuContent(){
    Row(modifier = Modifier.fillMaxWidth().padding(10.dp).clickable {  }){
        Icon(
            painter = painterResource(id = R.drawable.bar_chart_2),
            contentDescription = "chart",
            tint = Color(ColorPalette.pa10),
        )
        Text(text = "Menuoption1", color = Color(ColorPalette.pa20))

    }
    Row(modifier = Modifier.fillMaxWidth().padding(10.dp).clickable {  }){
        Icon(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "chart",
            tint = Color(ColorPalette.pa10),
        )
        Text(text = "Menuoption2", color = Color(ColorPalette.pa20))

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

// The menu that appears when you click the add button in right bottom of screen
@Composable
fun PopAddMenu(){
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
                        modifier = Modifier.background(
                            Color(ColorPalette.sa50),
                            shape = RoundedCornerShape(128.dp)
                        ).fillMaxWidth(0.5f), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Here are all buttons for the menu
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "chart",
                            tint = Color(ColorPalette.pa40),
                            modifier = Modifier
                                .size(64.dp)
                                .padding(15.dp)
                                .clickable{}
                        )
                        Icon(
                                painter = painterResource(id = R.drawable.bar_chart_2),
                        contentDescription = "chart",
                        tint = Color(ColorPalette.pa40),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(15.dp)
                            .clickable{}
                        )
                    }
                }
            }

    Icon(
        painter = painterResource(id = R.drawable.plus_circle_solid),
        contentDescription = "chart",
        tint = Color(ColorPalette.pa40),
        modifier = Modifier
            .size(128.dp)
            .padding(15.dp)
            .background(Color(ColorPalette.sa10), shape = CircleShape)
            .clickable { appearAddMenu = !appearAddMenu}
    )
}

@Composable
fun SortMenuContent(decks: Array<Deck>, searchQuery:String){

    Text("Sort By", fontSize = 20.sp, color = Color(ColorPalette.pa30))

    Text("Name (Ascending)",
        fontSize = 16.sp,
        color = Color(ColorPalette.pa50),
        modifier = Modifier
            .clickable {
                sortType = SortType.ByName;
                appearSortMenu = false;
                isAscending = true;
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
        color = Color(ColorPalette.pa50),
        modifier = Modifier
            .clickable {
                sortType = SortType.ByName;
                appearSortMenu = false;
                isAscending = false;
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
        color = Color(ColorPalette.pa50),
        modifier = Modifier
            .clickable {
                sortType = SortType.ByLastEdited;
                appearSortMenu = false;
                isAscending = true;
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
        color = Color(ColorPalette.pa50),
        modifier = Modifier
            .clickable {
                sortType = SortType.ByLastEdited;
                appearSortMenu = false;
                isAscending = false;
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
        color = Color(ColorPalette.pa50),
        modifier = Modifier
            .clickable {
                sortType = SortType.ByLastEdited;
                appearSortMenu = false;
                isAscending = true;
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
    color = Color(ColorPalette.pa50),
    modifier = Modifier
    .clickable {
        sortType = SortType.ByLastEdited;
        appearSortMenu = false;
        isAscending = false;
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
@Preview()
@Composable
fun preview(){
    val navController = rememberNavController()
    // navigation graph
    NavHost(
        navController = navController,
        startDestination = "deck_menu"
    ) {
        composable("main_menu") { MainMenuRender(navController, loadData("", context = LocalContext.current)) }
        composable("card_menu") { cardMenu(navController) }
        composable("deck_menu") { deckScreen(context = LocalContext.current,navController) }}
}