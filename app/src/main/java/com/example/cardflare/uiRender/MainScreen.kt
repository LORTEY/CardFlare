package com.example.cardflare.uiRender

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.cardflare.Deck
import com.example.cardflare.R
import com.example.cardflare.SortType
import com.example.cardflare.loadData
import com.example.cardflare.sortDecks


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
                                onValueChange = { searchQuery = it; qualifiedDecks = sortDecks(searchQuery, decks, sortType = sortType, true) },
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

// Menu that appears when sort icon is pressed
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