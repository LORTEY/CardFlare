package com.example.cardflare.ui.theme
import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardflare.R
import android.content.Context
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

    @Composable
    fun MainMenuRender(context: Context, navController: NavHostController) {
        var searchQuery by remember { mutableStateOf("") }
        var appear by remember { mutableStateOf(false) }
        val screenHeight = LocalConfiguration.current.screenHeightDp
        var files = listFilesInAssets(context).toList()
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(ColorPalette.sa10))) {

            Column(modifier = Modifier.fillMaxSize()){
                Spacer(modifier = Modifier.height((screenHeight * 0.12f).dp))
                Box(modifier = Modifier
                    .fillMaxSize()
                    .weight(1.0f)) {
                    LazyVerticalGrid (
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    )
                    {
                        items(files) { item ->

                            if (searchQuery in item.toString() || searchQuery=="") {
                                Text(
                                    text = item.toString(),
                                    color = Color(ColorPalette.pa0),
                                    modifier = Modifier
                                        .background(
                                            Color(ColorPalette.sa20),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .fillMaxWidth(0.5f)
                                        .height(100.dp)
                                        .padding(10.dp)
                                        .clickable { }
                                )

                            }
                        }
                    }
                    // Add menu
                    Icon(
                        painter = painterResource(id = R.drawable.plus_circle),
                        contentDescription = "chart",
                        tint = Color(ColorPalette.pa40),
                        modifier = Modifier
                            .size(128.dp)
                            .padding(15.dp)
                            .background(Color(ColorPalette.sa10), shape = CircleShape)
                            .clickable { navController.navigate("screen2") }
                            .align(Alignment.BottomEnd)
                    )
                }
            }


            //fade effect and upper menu
            Column(modifier = Modifier.height((screenHeight * 0.15f).dp)) {

                Spacer(modifier = Modifier.fillMaxSize().weight(0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .weight(0.6f)
                        .weight(20f)
                        .fillMaxHeight()
                        .background(Color(ColorPalette.sa20), shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 10.dp)){
                    Icon(
                        painter = painterResource(id = R.drawable.menu),
                        contentDescription = "chart",
                        tint = Color(ColorPalette.pa40),
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { appear = !appear }
                            .weight(0.1f)
                    )
                    //I have no idea how to use the colors in TextField so to make a place holder i used this box
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .weight(1.0f)
                        .background(Color(ColorPalette.sa20), shape = RoundedCornerShape(10.dp))
                        .align(Alignment.CenterVertically)){
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {searchQuery = it},
                            textStyle = TextStyle(color = Color(ColorPalette.pa50), fontSize = 16.sp),
                        )
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search Sets...",
                                color = Color(ColorPalette.sa50), // Placeholder text color
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                    }

                }

                Canvas(modifier = Modifier
                    .fillMaxWidth().weight(0.5f))
                {
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
            AnimatedVisibility(
                visible = appear,
                enter = fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    animationSpec = tween(200)
                ) { fullWidth -> -fullWidth / 2 },
                exit = fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(200)
                ) { fullWidth -> -fullWidth / 2 }
            ) {
                Row(modifier = Modifier
                    .background(Color(ColorPalette.sa10))
                    .fillMaxHeight()
                    .fillMaxWidth(0.4f)){

                    Column(modifier = Modifier
                        .background(Color(ColorPalette.sa10))
                        .fillMaxHeight()
                        .weight(0.1f)){
                        SlideMenuContent()
                    }
                    Divider(color = Color(ColorPalette.sa30), modifier = Modifier.fillMaxHeight().width(2.dp))
                }

            }
        }
    }
    fun  listFilesInAssets(context: Context) : Array<String>{
        try {
            val assetManager = context.assets
            val fileNames = assetManager.list("FlashcardDirectory")
            if (fileNames != null) {
                return fileNames
            } else {
                Log.d("AssetsFiles", "No files found in the assets folder.")
            }
        } catch (e: Exception) {
            Log.e("AssetsFiles", "Error reading assets folder: ${e.message}")
        }
        return arrayOf()
    }
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
fun addMenu(context: Context, navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize().background(Color(ColorPalette.sa20)))
}

/*
    @Preview(showBackground = true)
    @Composable
    fun preview(){
        val context = LocalContext.current
        MainMenuRender(context = context,)
    }
*/
