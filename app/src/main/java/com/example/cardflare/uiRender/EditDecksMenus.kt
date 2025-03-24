package com.example.cardflare.uiRender

import android.content.Context
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cardflare.addDeck
import com.example.cardflare.ui.theme.Material3AppTheme

// The menu that appears when you press pluss button on the deck view screen
@Composable
fun AddDeckScreen(context: Context, navController: NavController){
    var DeckName by remember{ mutableStateOf("") }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Text("Deck Name:", color = MaterialTheme.colorScheme.primary)
        TextField(
            value = DeckName,
            placeholder = { Text("Deck Name", color = MaterialTheme.colorScheme.inverseSurface.copy(alpha=0.8f)) },
            onValueChange = { newText:String -> DeckName = newText},
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp, max = 200.dp)
                .background(color = MaterialTheme.colorScheme.inverseOnSurface),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                unfocusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                disabledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
                focusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.inverseSurface,
            ),
            maxLines = 5,
            minLines = 1
        )
        Text("Add", Modifier.clickable { addDeck(context, DeckName); navController.popBackStack()})
    }
}