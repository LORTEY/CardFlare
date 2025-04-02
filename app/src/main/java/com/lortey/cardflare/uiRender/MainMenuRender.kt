package com.lortey.cardflare.uiRender

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

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
/*
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
                    composable("learn_screen") { LearnScreen(navController,context = LocalContext.current) }
                    composable("deck_menu") { deckScreen(context = LocalContext.current,navController) }
                    composable("settings") { SettingsMenu(navController,context = LocalContext.current) }
                    composable("deck_add_screen") { AddDeckScreen(context = LocalContext.current, navController) }
                    composable("add_flashcard") { AddFlashcardScreen(context = LocalContext.current, navController = navController) }}
            }
}*/