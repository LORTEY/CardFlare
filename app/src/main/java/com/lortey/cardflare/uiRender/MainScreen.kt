package com.lortey.cardflare.uiRender

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.lortey.cardflare.Deck
import com.lortey.cardflare.R
import com.lortey.cardflare.SortType
import com.lortey.cardflare.getTranslation
import com.lortey.cardflare.loadData
import com.lortey.cardflare.multipleDeckMoveToBin
import com.lortey.cardflare.sortDecks

//main screen
@Composable
fun MainMenuRender(navController: NavHostController, context: Context, permissionGranter:() -> Unit, arePermissionsMissing:()->Boolean) {

    var searchQuery by remember { mutableStateOf("") }
    val isAscending by remember { mutableStateOf(true) }
    var selectMode by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        decks = loadData(filename = "", context = context)
    }

    var decksImage by remember { mutableStateOf( loadData(filename = "", context = context)) }//local copy of decks
    var appear by remember { mutableStateOf(false) } //appear slide menu

    qualifiedDecks = sortDecks(searchQuery, decksImage, sortType = sortType, isAscending)
    decksSelected = remember(Unit) {  // Runs only once
        mutableStateListOf<Boolean>().apply {
            addAll(List(qualifiedDecks.size) { false })
        }
    }
    BackHandler { // Handle the back button press
        if (appear || appearAddMenu){
            appearAddMenu = false
            appear = false
        }else{
            navController.popBackStack()
        }
    }
    if(renderMainMenu){ // no use case
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
                            items(qualifiedDecks.size) { index -> // render decks in grid
                                Text(
                                    text = qualifiedDecks[index].name,
                                    color = if (decksSelected[index]) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 10.dp,
                                            shape = RoundedCornerShape(10.dp),
                                            clip = false
                                        )
                                        .background(
                                            if (decksSelected[index]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inverseOnSurface
                                        )
                                        .fillMaxWidth(0.5f)
                                        .height(100.dp)
                                        .padding(10.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    decksSelected[index] = true
                                                    selectMode = true
                                                },
                                                onTap = {
                                                    if (selectMode) {
                                                        decksSelected[index] =
                                                            !decksSelected[index] //flips ones to zeroes and vice versa
                                                        if (decksSelected.count { it } == 0) {
                                                            // if no more selected cards left stop select mode
                                                            selectMode = false
                                                        }
                                                    } else {
                                                        currentOpenedDeck = qualifiedDecks[index]
                                                        navController.navigate("deck_menu")
                                                    }
                                                }
                                            )
                                        },
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Will make all menus hide if box and not them is clicked
                        if (appearAddMenu || appear) {
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

                        //Plus menu
                        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                            Column(horizontalAlignment = Alignment.End,
                                modifier = Modifier
                                    .align(Alignment.End)) {
                                UniversalAddMenu(appearAddMenu, changeVisibility = {appearAddMenu = !appearAddMenu},
                                    listOf(
                                        //add new deck
                                        AddMenuEntry("Add Deck", R.drawable.create_empty, Action = {
                                            deckToModify = null;
                                            flashcardsAddedToDeck.clear();
                                            navController.navigate("deck_add_screen") }),
                                        //move selected decks to bin
                                        AddMenuEntry(Name = "Remove Decks", Icon = R.drawable.delete,
                                            Action = {
                                                multipleDeckMoveToBin(context = context, decks = qualifiedDecks, selected = decksSelected)
                                                decks = loadData(filename = "", context = context)
                                                decksImage = decks
                                                decksSelected.fill(false)
                                                qualifiedDecks = sortDecks(searchQuery, decksImage, sortType = sortType,
                                                    com.lortey.cardflare.uiRender.isAscending
                                                )
                                                selectMode = false
                                            }),
                                        //edit selected deck
                                        AddMenuEntry(Name = "Edit Deck", Icon = R.drawable.edit, Action = {
                                            flashcardsAddedToDeck.clear()
                                            if (decksSelected.count{it} == 1){
                                                deckToModify = qualifiedDecks[decksSelected.indexOf(true)]
                                                navController.navigate("deck_add_screen")
                                            }
                                        })
                                    )
                                )
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
                                onValueChange = { searchQuery = it; qualifiedDecks = sortDecks(searchQuery, decksImage, sortType = sortType, true) },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = getTranslation("Search Sets..."),
                                    color = MaterialTheme.colorScheme.onBackground, // Placeholder text color
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                            }
                        }

                        // "Sort" Menu button
                        Column {
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
                                SortMenuContent(decks = decksImage, searchQuery = searchQuery)
                            }
                        }
                    }
                    //Fade Effect
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
            //Greeter pop up
            Greeter(context, permissionGranter, arePermissionsMissing)
        }
    }
}

// definition used for rendering components of left slide menu used by MainMenuRender function
@Composable
fun SlideMenuContent(navController: NavController){

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable { navController.navigate("main_menu")}){
        Icon(
            painter = painterResource(id = R.drawable.home),
            contentDescription = "chart",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = getTranslation("Home"), color = MaterialTheme.colorScheme.primary)
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable { navController.navigate("settings") }){
        Icon(
            painter = painterResource(id = R.drawable.settings),
            contentDescription = "chart",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = getTranslation("Settings"), color = MaterialTheme.colorScheme.primary)

    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable { navController.navigate("launch_on_manager")}){
        Icon(
            painter = painterResource(id = R.drawable.apps),
            contentDescription = "chart",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = getTranslation("Launch On Options"), color = MaterialTheme.colorScheme.primary)
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable { navController.navigate("fsrs_due_today")}){
        Icon(
            painter = painterResource(id = R.drawable.intelligent),
            contentDescription = "chart",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = getTranslation("Cards Due Today"), color = MaterialTheme.colorScheme.primary)
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable { navController.navigate("bin_screen")}){
        Icon(
            painter = painterResource(id = R.drawable.delete),
            contentDescription = "chart",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = getTranslation("Bin"), color = MaterialTheme.colorScheme.primary)
    }
}

// Menu that appears when sort icon is pressed
@Composable
fun SortMenuContent(decks: List<Deck>, searchQuery:String){

    Text(getTranslation("Sort By"), fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)

    Text(getTranslation("Name (Ascending)"),
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

    Text(getTranslation("Name (Descending)"),
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

    Text(getTranslation("Last Modified (Ascending)"),
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

    Text(getTranslation("Date Modified (Descending)"),
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

    Text(getTranslation("Date Created (Ascending)"),
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

    Text(getTranslation("Date Created (Descending)"),
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



