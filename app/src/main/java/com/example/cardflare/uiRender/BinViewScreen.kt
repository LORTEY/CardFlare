package com.example.cardflare.uiRender

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cardflare.R
import com.example.cardflare.loadData
import com.example.cardflare.removeMultipleDecksFromBin

@Composable
fun BinRender(context: Context, navController: NavController){
    var decksInBin by remember{ mutableStateOf( loadData(filename = "", context = context, folderName = "BinDirectory"))}
    Log.d("cardflare2",loadData(filename = "", context = context, folderName = "BinDirectory").toString())
    var selectMode by remember{ mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }

    //var cards = arrayOf(arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"),arrayOf("ghf", "dfg"))
    var binSelected = remember(Unit) {  // Runs only once
        mutableStateListOf<Boolean>().apply {
            addAll(List(decksInBin.size) { false })
        }
    }

    BackHandler { // Handle the back button press
        if (selectMode){
            selectMode = false
            binSelected.fill(false)
        }else{
            navController.popBackStack()
        }
    }
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
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
            items(decksInBin.size) { index ->
                Text(
                    text = decksInBin[index].name,
                    color = if (binSelected[index]) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = false
                        )
                        .background(
                            if (binSelected[index]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inverseOnSurface
                        )
                        .fillMaxWidth(0.5f)
                        .height(100.dp)
                        .padding(10.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    binSelected[index] = true
                                    selectMode = true
                                },
                                onTap = {
                                    if (selectMode) {
                                        binSelected[index] =
                                            !binSelected[index] //flips ones to zeroes and vice versa
                                        if (binSelected.count { it } == 0) {
                                            // if no more selected cards left stop select mode
                                            selectMode = false
                                        }
                                    } else {
                                        currentOpenedBinDeck = decksInBin[index];
                                        /*                           */navController.navigate("bin_deck_view")
                                    }
                                }
                            )
                        },
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )


            }
        }
        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
            Column(horizontalAlignment = Alignment.End,
                modifier = Modifier
                    //.width(128.dp)
                    .align(Alignment.End)) {
                UniversalAddMenu(
                    showAddMenu, { showAddMenu = !showAddMenu }, listOf(
                        AddMenuEntry(
                            Name = "Remove Deck", Icon = R.drawable.nav_arrow_down,
                            Action = {
                                removeMultipleDecksFromBin(
                                    context = context,
                                    decksSelected = binSelected,
                                    listOfDecks = decksInBin
                                );
                                decksInBin = loadData(filename = "", context = context, folderName = "BinDirectory")
                            })
                    )
                )
            }
        }
    }
}