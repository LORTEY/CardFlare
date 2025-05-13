package com.lortey.cardflare.uiRender

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lortey.cardflare.R
import com.lortey.cardflare.addDeck
import com.lortey.cardflare.addFlashcard
import com.lortey.cardflare.getDeck
import com.lortey.cardflare.getDueCards
import com.lortey.cardflare.getTranslation
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId

@Composable
fun DueToday(context: Context, navController: NavController){
    val dueToday by remember{ mutableStateOf(getDueCards(context))}
    Column(modifier = Modifier.fillMaxSize().padding(WindowInsets.systemBars.asPaddingValues())) {
        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
            items(dueToday){card->
                Surface(modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp).height(80.dp),  shadowElevation = 4.dp, color = MaterialTheme.colorScheme.inverseOnSurface) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.background(Color.Transparent)
                            .padding(vertical = 5.dp, horizontal = 4.dp).fillMaxWidth().fillMaxHeight()
                    ) {
                        Text(
                            text = card.SideA,
                            modifier = Modifier.weight(1f).fillMaxSize().padding(horizontal = 10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = card.SideB,
                            modifier = Modifier.weight(1f).fillMaxSize().padding(horizontal = 10.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val time = Instant.ofEpochMilli(card.due).atZone(ZoneId.systemDefault())
                        Text(
                            text = "${time.hour}:${time.minute} ${time.dayOfMonth}.${time.month}.${time.year}",
                            modifier = Modifier.weight(1f).fillMaxSize().padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.height(50.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = {
                    CardsToLearn = dueToday.toMutableList()
                    navController.navigate("learn_screen")

                }) {

                Icon(
                    painter = painterResource(id = R.drawable.learn),
                    contentDescription = "learn Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(vertical = 5.dp, horizontal = 5.dp)
                )
                Text(
                    text = getTranslation("Learn"),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )


            }
        }
    }
}