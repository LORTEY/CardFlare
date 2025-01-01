package com.example.cardflare.ui.theme

import android.R.attr.padding
import android.R.attr.start
import android.util.Log
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

import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.TextStyle


@Composable
fun MainMenuRender() {
    var searchQuery by remember { mutableStateOf("") }

    val screenHeight = LocalConfiguration.current.screenHeightDp

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(ColorPalette.sa10))) {

        Column(modifier = Modifier.fillMaxSize()){
            Spacer(modifier = Modifier.height((screenHeight * 0.12f).dp))
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1.0f)){
                LazyColumn(modifier = Modifier
                    .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp))
                {
                    items(20){
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)){
                            Text(
                                text = "flashcardset",
                                color = Color(ColorPalette.pa0),
                                modifier = Modifier
                                    .background(
                                        Color(ColorPalette.sa20),                                        shape = RoundedCornerShape(10.dp)                                     )
                                    .fillMaxWidth(0.1f)
                                    .height(100.dp)
                                    .weight(1f)
                                    .padding(10.dp)
                                    .clickable {  }
                            )
                            Text(
                                text = "flashcardset",
                                color =  Color(ColorPalette.pa0),
                                modifier = Modifier
                                    .background(
                                        Color(ColorPalette.sa20),                                          shape = RoundedCornerShape(10.dp)                                     )
                                    .fillMaxWidth(0.1f)
                                    .weight(1f)
                                    .height(100.dp)
                                    .padding(10.dp)
                                    .clickable {  }
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
                        .clickable {  }
                        .background(Color(ColorPalette.sa10),shape = CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }



            /*//Menu bar at the bottom
            Row(horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                .fillMaxSize()
                .weight(0.1f)
                .background(Color(ColorPalette.sa20),shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))){
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "chart",
                    tint = Color(ColorPalette.pa40),
                    modifier = Modifier
                        .size(72.dp)
                        .clickable {  }
                        .background(Color(ColorPalette.sa0),shape = CircleShape)
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterVertically)
                )
                Icon(
                    painter = painterResource(id = R.drawable.bar_chart_2),
                    contentDescription = "chart",
                    tint = Color(ColorPalette.pa40),
                    modifier = Modifier
                        .size(72.dp)
                        .clickable {  }
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterVertically)
                )
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "chart",
                    tint = Color(ColorPalette.pa40),
                    modifier = Modifier
                        .size(72.dp)
                        .clickable {  }
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterVertically)
                )
            }*/

        }


        //fade effect and title
        Column(modifier = Modifier.height((screenHeight * 0.15f).dp)) {
            /*Text(
                text = "Your sets",
                color = Color(ColorPalette.pa50),
                fontSize = 24.sp,
                modifier = Modifier.background(Color(ColorPalette.sa10)).weight(1.0f).padding(vertical = 30.dp, horizontal = 15.dp)
            )*/

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
                        .clickable {  }
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

    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CardFlareTheme {
        MainMenuRender()
    }
}