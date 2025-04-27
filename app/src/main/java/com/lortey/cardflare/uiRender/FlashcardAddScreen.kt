package com.lortey.cardflare.uiRender

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.addFlashcard
import com.lortey.cardflare.createTranslator
import com.lortey.cardflare.loadData
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.lortey.cardflare.getDefaultFSRSValue
import com.lortey.cardflare.getDueDate


@Composable
fun AddFlashcardScreen(navController: NavController, context: Context){
    var textStateA by remember {  mutableStateOf("") }
    var textStateB by remember { mutableStateOf("") }
    var defaultStateA by remember {  mutableStateOf("") }
    var defaultStateB by remember { mutableStateOf("") }
    val translator = remember { createTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.POLISH) }
    translator.downloadModelIfNeeded()
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column {
            Text("Side A", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

            TextField(
                value = textStateA,
                placeholder = { Text(defaultStateA, color = MaterialTheme.colorScheme.inverseSurface.copy(alpha=0.8f)) },
                onValueChange = { newText -> textStateA = newText; if(textStateB.isBlank()) translateText(textStateA, translator){result-> defaultStateB = result} },
                //label = { Text(text = if(textStateB.isEmpty()) "Enter Text Of Side A" else translatedFlashcardSide,color = MaterialTheme.colorScheme.inverseSurface, style = MaterialTheme.typography.titleMedium) },
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

            Text("Side B", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            TextField(
                value = textStateB,
                placeholder = { Text(defaultStateB, color = MaterialTheme.colorScheme.inverseSurface.copy(alpha=0.8f)) },
                onValueChange = { newText:String -> textStateB = newText; if(textStateA.isBlank()) translateText(textStateB, translator){result-> defaultStateA = result} },
                //label = { Text(text = if(textStateB.isBlank()) "Enter Text Of Side B" else translatedFlashcardSide,color = MaterialTheme.colorScheme.inverseSurface, style = MaterialTheme.typography.titleMedium) },
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
            val ableToAdd = (textStateA.isNotBlank() || defaultStateA.isNotBlank()) && (textStateB.isNotBlank() || defaultStateB.isNotBlank())

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 20.dp)
                .background(color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = if (ableToAdd) 1f else 0.5f))
                .height(50.dp)
                .clickable {
                    if (ableToAdd) {
                        val fsrsValue = getDefaultFSRSValue(context)
                        addFlashcard(
                            currentOpenedDeck.value.filename,
                            Flashcard(
                                0,
                                SideA = if (textStateA.isNotBlank()) textStateA else defaultStateA,
                                SideB = if (textStateB.isNotBlank()) textStateB else defaultStateB,
                                "",
                                fsrsValue,
                                getDueDate(context,fsrsValue)
                            ), context = context
                        )
                        currentOpenedDeck =IndexTracker(
                            loadData(filename = currentOpenedDeck.value.filename, context = context)[0])
                        defaultStateA = ""
                        defaultStateB = ""
                        textStateA = ""
                        textStateB = ""
                    }
                },
                horizontalArrangement = Arrangement.SpaceEvenly) {
                Text(
                    "Add", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = if (ableToAdd) 1f else 0.5f),
                )
            }
        }
    }
}
fun translateText(text: String, translator: Translator, onResult: (String) -> Unit) {
    Log.d("CardflareTranslator","hii")
    translator.translate(text)
        .addOnSuccessListener { translatedText -> onResult(translatedText) }
        .addOnFailureListener { e -> onResult("Translation failed: ${e.message}") }
}