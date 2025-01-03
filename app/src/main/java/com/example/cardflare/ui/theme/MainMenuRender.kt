package com.example.cardflare.ui.theme

import android.R.attr.padding
import android.R.attr.start
import android.content.res.AssetManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.cardflare.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.TextStyle


private var currentMenu = 1;
@Composable
fun MainMenuRender(context: Context) {
    if (currentMenu == 1){
        var searchQuery by remember { mutableStateOf("") }
        var appear by remember { mutableStateOf(false) }
        val screenHeight = LocalConfiguration.current.screenHeightDp
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(ColorPalette.sa10))) {

            Column(modifier = Modifier.fillMaxSize()){
                Spacer(modifier = Modifier.height((screenHeight * 0.12f).dp))
                Box(modifier = Modifier
                    .fillMaxSize()
                    .weight(1.0f)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    )
                    {
                        items(20) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            ) {
                                Text(
                                    text = "flashcardset",
                                    color = Color(ColorPalette.pa0),
                                    modifier = Modifier
                                        .background(
                                            Color(ColorPalette.sa20),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .fillMaxWidth(0.1f)
                                        .height(100.dp)
                                        .weight(1f)
                                        .padding(10.dp)
                                        .clickable { }
                                )
                                Text(
                                    text = "flashcardset",
                                    color = Color(ColorPalette.pa0),
                                    modifier = Modifier
                                        .background(
                                            Color(ColorPalette.sa20),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .fillMaxWidth(0.1f)
                                        .weight(1f)
                                        .height(100.dp)
                                        .padding(10.dp)
                                        .clickable { }
                                )
                            }
                        }
                    }
                    // Add icon menu
                    Icon(
                        painter = painterResource(id = R.drawable.plus_circle),
                        contentDescription = "chart",
                        tint = Color(ColorPalette.pa40),
                        modifier = Modifier
                            .size(128.dp)
                            .padding(15.dp)
                            .clickable { }
                            .background(Color(ColorPalette.sa10), shape = CircleShape)
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
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {searchQuery = it},
                        textStyle = TextStyle(color = Color(ColorPalette.pa0), fontSize = 16.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .weight(1.0f)
                            .background(Color(ColorPalette.sa20), shape = RoundedCornerShape(10.dp))
                            .align(Alignment.CenterVertically)

                    )
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
                Column(modifier = Modifier
                    .background(Color(ColorPalette.sa10))
                    .fillMaxHeight()
                    .fillMaxWidth(0.4f)){
                    SlideMenuContent()
                }
            }
        }
    }
}
fun listFilesInAssets(context: Context) {
    try {
        val assetManager = context.assets
        val fileNames = assetManager.list("FlashcardDirectory")
        if (fileNames != null) {
            // Log the names of all files
            for (fileName in fileNames) {
                Log.d("AssetsFiles", "File: $fileName")
            }
        } else {
            Log.d("AssetsFiles", "No files found in the assets folder.")
        }
    } catch (e: Exception) {
        Log.e("AssetsFiles", "Error reading assets folder: ${e.message}")
    }
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

