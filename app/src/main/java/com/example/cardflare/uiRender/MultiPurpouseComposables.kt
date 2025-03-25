package com.example.cardflare.uiRender

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cardflare.R

public data class AddMenuEntry(
    val Name:String,
    val Icon: Int,
    val Action :() -> Unit
)
@Composable
fun UniversalAddMenu(visibility: Boolean, changeVisibility :() -> Unit, entries: List<AddMenuEntry>) {
    AnimatedVisibility(
        modifier = Modifier.padding(horizontal = 40.dp),
        visible = visibility,
        enter = fadeIn(animationSpec = tween(100)) + slideInVertically(
            animationSpec = tween(100)
        ) { fullWidth -> fullWidth / 2 },
        exit = fadeOut(animationSpec = tween(100)) + slideOutVertically(
            animationSpec = tween(100)
        ) { fullWidth -> fullWidth / 2 }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Here are all buttons for the menu
            entries.forEach() { entry ->

                Row(
                    modifier = Modifier
                        .clickable (onClick = {entry.Action()}),
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                            .background(
                                shape = RoundedCornerShape(128.dp),
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )

                    ) {
                        Text(
                            entry.Name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                    //row is here just for background color
                    Row(
                        modifier = Modifier
                            .background(
                                shape = RoundedCornerShape(128.dp),
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = entry.Icon),
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
    }
    Icon(
        painter = painterResource(id = R.drawable.plus_circle_solid),
        contentDescription = "chart",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(128.dp)
            .padding(15.dp)
            .background(MaterialTheme.colorScheme.background, shape = CircleShape)
            .clickable { changeVisibility() })
}


@Composable
fun PopUp(title:String = "", text:String = "", closeAction:()->Unit, visibility:Boolean, secondButton:(@Composable () -> Unit)? = null){

        Popup(

            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
    ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(20.dp)
                    .clickable(onClick = { closeAction() }),
                contentAlignment = Alignment.Center
            ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            //.align(Alignment.Center)
                            .background(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.background
                            )
                            .clickable {}
                    ) {
                        if (title.length > 1) {
                            androidx.compose.material.Text(
                                text = title,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(3.dp),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        androidx.compose.material.Text(
                            text = text,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(5.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                        ) {
                            androidx.compose.material.Text(
                                "Close",
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(5.dp)
                                    .clickable(onClick = { closeAction() }),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if(secondButton != null){
                                secondButton()
                            }
                        }
                    }
                }

        }
}
