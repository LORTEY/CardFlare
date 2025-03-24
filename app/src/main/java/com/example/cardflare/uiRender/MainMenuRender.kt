package com.example.cardflare.uiRender

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.parseColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
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
import com.example.cardflare.ui.theme.Material3AppTheme
import com.example.cardflare.updateSetting
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

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