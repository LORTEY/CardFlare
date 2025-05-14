package com.lortey.cardflare.uiRender

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.R
import com.lortey.cardflare.addFlashcard
import com.lortey.cardflare.createTranslator
import com.lortey.cardflare.getDefaultFSRSValue
import com.lortey.cardflare.getDueDate
import com.lortey.cardflare.getTranslation
import com.lortey.cardflare.loadData
import com.lortey.cardflare.removeFlashcard
import java.util.Locale

var currentModifiedFlashcard:Flashcard? = null
@Composable
fun AddFlashcardScreen(navController: NavController, context: Context){
    var textStateA by remember {  mutableStateOf("") }
    var textStateB by remember { mutableStateOf("") }
    var defaultStateA by remember {  mutableStateOf("") }
    var defaultStateB by remember { mutableStateOf("") }
    var translator:Translator? by remember(Unit) { mutableStateOf(null)}
    var revTranslator:Translator? by remember(Unit) { mutableStateOf(null)}
    LaunchedEffect(Unit) {
        if(currentOpenedDeck.sideALang != null && currentOpenedDeck.sideBLang != null){
            val sideALang = TranslateLanguage.getAllLanguages().find{ Locale(it).displayName == currentOpenedDeck.sideALang };
            val sideBLang = TranslateLanguage.getAllLanguages().find{ Locale(it).displayName == currentOpenedDeck.sideBLang };
            if(sideBLang != null && sideALang != null){
                translator = createTranslator( sideALang,sideBLang)
                translator!!.downloadModelIfNeeded()

                revTranslator = createTranslator( sideBLang,sideALang)
                revTranslator!!.downloadModelIfNeeded()
            }
        }
        if(currentModifiedFlashcard != null){
            textStateA = currentModifiedFlashcard!!.SideA
            textStateB = currentModifiedFlashcard!!.SideB
        }
    }



    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column {
            Text(getTranslation("Side A"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

            TextField(
                value = textStateA,
                placeholder = { Text(defaultStateA, color = MaterialTheme.colorScheme.inverseSurface.copy(alpha=0.8f)) },
                onValueChange = { newText -> textStateA = newText; if(textStateB.isBlank() && translator != null) translateText(textStateA, translator!!){result-> defaultStateB = result} },
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

            IconButton(onClick = {
                var holder = ""
                if(textStateB.isBlank() && textStateA.isBlank()){
                    holder = defaultStateA
                    defaultStateA = defaultStateB
                    defaultStateB = holder
                }else if(textStateB.isBlank() && textStateA.isNotBlank()){
                        holder = textStateA
                        textStateA = defaultStateB
                        defaultStateB = holder
                }else if(textStateB.isNotBlank() && textStateA.isBlank()){
                    holder = defaultStateA
                    defaultStateA = textStateB
                    textStateB = holder
                }else{
                    holder = textStateA
                    textStateA = textStateB
                    textStateB = holder
                }

            }) {
                Icon(painter = painterResource(id = R.drawable.swap),
                    contentDescription = "Add Tag",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.background, shape = CircleShape))
            }

            Text(getTranslation("Side B"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            TextField(
                value = textStateB,
                placeholder = { Text(defaultStateB, color = MaterialTheme.colorScheme.inverseSurface.copy(alpha=0.8f)) },
                onValueChange = { newText:String -> textStateB = newText; if(textStateA.isBlank() && revTranslator != null) translateText(textStateB, revTranslator!!){result-> defaultStateA = result} },
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


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.background(color = MaterialTheme.colorScheme.inverseOnSurface)
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (ableToAdd) {
                                if(currentModifiedFlashcard == null) {
                                    val fsrsValue = getDefaultFSRSValue(context)
                                    addFlashcard(
                                        currentOpenedDeck.filename,
                                        Flashcard(
                                            0,
                                            SideA = if (textStateA.isNotBlank()) textStateA else defaultStateA,
                                            SideB = if (textStateB.isNotBlank()) textStateB else defaultStateB,
                                            "",
                                            fsrsValue,
                                            getDueDate(context, fsrsValue)
                                        ), context = context
                                    )

                                }else{
                                    removeFlashcard(context,currentModifiedFlashcard!!.FromDeck,currentModifiedFlashcard!!)
                                    addFlashcard(
                                        currentModifiedFlashcard!!.FromDeck,
                                        Flashcard(
                                            currentModifiedFlashcard!!.id,
                                            SideA = if (textStateA.isNotBlank()) textStateA else defaultStateA,
                                            SideB = if (textStateB.isNotBlank()) textStateB else defaultStateB,
                                            currentModifiedFlashcard!!.FromDeck,
                                            currentModifiedFlashcard!!.FsrsData,
                                            currentModifiedFlashcard!!.due
                                        ), context = context
                                    )
                                }
                                currentOpenedDeck =
                                    loadData(
                                        filename = currentOpenedDeck.filename,
                                        context = context
                                    )[0]

                                defaultStateA = ""
                                defaultStateB = ""
                                textStateA = ""
                                textStateB = ""
                            }
                        },
                        enabled = ableToAdd) {

                        Text(
                            text = getTranslation("Add"),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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