package com.lortey.cardflare.uiRender

import android.content.Context
import android.graphics.Color.parseColor
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lortey.cardflare.Deck
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.LaunchOnRule
import com.lortey.cardflare.R
import com.lortey.cardflare.Tag
import com.lortey.cardflare.addDeck
import com.lortey.cardflare.addFlashcard
import com.lortey.cardflare.addTag
import com.lortey.cardflare.extractTextFromUri
import com.lortey.cardflare.getAllSupportedLanguages
import com.lortey.cardflare.getDeck
import com.lortey.cardflare.getTranslation
import com.lortey.cardflare.loadTags
import com.lortey.cardflare.tags
import com.lortey.cardflare.ui.theme.Material3AppTheme
import com.lortey.cardflare.updateSetting
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


// The menu that appears when you press plus button on the deck view screen

public var deckToModify:Deck? = null
var flashcardsAddedToDeck:MutableList<Flashcard> = mutableListOf()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeckScreen(context: Context, navController: NavController){
    var DeckName by remember{ mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<Tag>()) }
    var pickImage by remember { mutableStateOf(false) }
    var sideALang:String? by remember { mutableStateOf(null) }
    var sideBLang:String? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        //flashcardsAddedToDeck = mutableListOf();
        if (deckToModify != null) {
            DeckName = deckToModify!!.name
            tags = deckToModify!!.tags.toMutableStateList()
        }else{
            deckToModify = getDeck()
        }
    }

    var addTagMenu by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .verticalScroll(rememberScrollState(0))
    ) {
        OutlinedTextField(
            value = DeckName,
            onValueChange = { DeckName = it },
            label = {
                Text(
                    getTranslation("Name"),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,

                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,

                cursorColor = MaterialTheme.colorScheme.primary,

                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {
            Text(
                text = getTranslation("Tags"),
                modifier = Modifier.padding(10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
            LazyColumn(
                modifier = Modifier
                    .padding(10.dp)
                    .height((minOf((tags.size) * 48 + 60, 300)).dp)
            ) {
                items(tags) { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                        tagItem(
                            tag,
                            addTag = {
                                deckToModify?.tags?.remove(tag)
                                tags =
                                    deckToModify?.tags?.toMutableStateList() ?: mutableStateListOf()
                            },
                            icon = R.drawable.minus
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable { addTagMenu = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getTranslation("Add Tag"),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Add Tag",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }



        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        ){
            Text(
                text = getTranslation("New Flashcards Added"),
                modifier = Modifier.padding(10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
            LazyColumn(
                modifier = Modifier
                    .padding(10.dp)
                    .height((minOf((flashcardsAddedToDeck.size) * 50, 300)).dp)
            ) {
                items(flashcardsAddedToDeck) { cards ->

                    Surface(modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),  shadowElevation = 4.dp, color = MaterialTheme.colorScheme.inverseOnSurface) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(Color.Transparent)
                                .padding(vertical = 5.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = cards.SideA,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = cards.SideB,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            imageUri = uri
            uri?.let {
                extractTextFromUri(context, it) { boxes ->
                    boxLines  = boxes
                }

                navController.navigate("image_get")
            }

        }
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
                    imagePicker.launch("image/*");

                }) {

                Text(
                    text = getTranslation("From Image"),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {
            Text(
                text = getTranslation("Auto Completion"),
                modifier = Modifier.padding(10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
            var appearChooseLanguageMenuA by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
                    .clickable { appearChooseLanguageMenuA = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getTranslation("SideA Language"),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (sideALang == null) getTranslation("None") else sideALang ?: "None",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp).background(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ),
                    style = MaterialTheme.typography.titleLarge
                )

                DropdownMenu(
                    expanded = appearChooseLanguageMenuA,
                    onDismissRequest = { appearChooseLanguageMenuA = false }, // Close menu on dismiss
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .padding(start = 16.dp)
                        .height(400.dp)
                ) {
                    val possibleLanguages by remember(Unit){ mutableStateOf(getAllSupportedLanguages())}
                        possibleLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = lang,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    appearChooseLanguageMenuA = false
                                    sideALang = lang
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                }
            }
            var appearChooseLanguageMenuB by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface)
                    .clickable { appearChooseLanguageMenuB = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getTranslation("SideB Language"),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (sideBLang == null) getTranslation("None") else sideBLang ?: "None",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp).background(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ),
                    style = MaterialTheme.typography.titleLarge
                )

                DropdownMenu(
                    expanded = appearChooseLanguageMenuB,
                    onDismissRequest = { appearChooseLanguageMenuB = false }, // Close menu on dismiss
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .padding(start = 16.dp)
                        .height(400.dp)
                ) {
                    val possibleLanguages by remember(Unit){ mutableStateOf(getAllSupportedLanguages())}
                    possibleLanguages.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = lang,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                appearChooseLanguageMenuB = false
                                sideBLang = lang
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
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
                        sideBLang = null; sideALang = null
                    }) {

                    Text(
                        text = getTranslation("No Auto Completion"),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

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
                    val filename = if ((deckToModify!= null && deckToModify!!.filename.isNotBlank())) deckToModify!!.filename else URLEncoder.encode(DeckName, StandardCharsets.UTF_8.toString())
                    if (deckToModify!=null) {
                        val sourceDir = File(context.getExternalFilesDir(null), "FlashcardDirectory")

                        val previousDeck = File(sourceDir, deckToModify!!.filename)

                        if (previousDeck.exists()) {
                            previousDeck.delete() // Delete existing file first
                        }

                    }
                    addDeck(
                        context, name = DeckName,
                        deck = getDeck(
                            filename = filename,
                            name = if(DeckName.isNotBlank()) DeckName else deckToModify!!.name,
                            tags = deckToModify!!.tags,
                            sideALang = sideALang,
                            sideBLang = sideBLang,
                        ),
                        filename = filename)
                    if (deckToModify!=null) {

                        deckToModify!!.cards.forEach { card ->
                            addFlashcard(
                                filename = filename,
                                card,
                                context = context,
                                reassignID = false
                            )
                        }
                    }
                    flashcardsAddedToDeck.forEach { card ->
                        addFlashcard(
                            filename = filename,
                            card,
                            context = context,
                            reassignID = true)
                    }
                    navController.clearBackStack("main_menu")
                    navController.navigate("main_menu")
                }, enabled = DeckName.isNotBlank()
                ) {

                Text(
                    text = getTranslation("Add"),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if(addTagMenu) {
            Popup(
                properties = PopupProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    focusable = true
                ),
                alignment = Alignment.Center,
                onDismissRequest = { addTagMenu = false },

                ) {
                Box(modifier = Modifier
                    .padding(50.dp)
                    .fillMaxSize()
                    .focusable()) {
                    tagsMenu(context, {
                        Log.d("cardflare8", deckToModify!!.tags.toString());
                        tags = deckToModify?.tags?.toMutableStateList() ?: mutableStateListOf(Tag("empty", ""));
                        Log.d("cardflare81", tags.toMutableList().toString());
                    },
                        {addTagMenu = false})

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                            .padding(6.dp)
                            .align(Alignment.BottomCenter)
                    ) {

                    }
                }
            }
        }

        if(pickImage){

        }
        //Text("Add", Modifier.clickable { addDeck(context, name = DeckName, filename = URLEncoder.encode(DeckName, StandardCharsets.UTF_8.toString())); navController.popBackStack()})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun tagsMenu(context: Context, reloadTags:() -> Unit, disappear:() -> Unit){

    var tagsLocal by remember { mutableStateOf(tags) }
    LaunchedEffect(Unit) {
        loadTags(context)
        tagsLocal = tags
    }
    var newTagName by remember { mutableStateOf("") }
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.inverseOnSurface, shape = MaterialTheme.shapes.medium)) {
        // Choose Deck to Activate
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTagName,
                onValueChange = { newTagName = it },
                label = {
                    Text(
                        getTranslation("New Tag Name"),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,

                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,

                    cursorColor = MaterialTheme.colorScheme.primary,

                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                ),
                modifier = Modifier
                    .padding(10.dp)
                    .weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.plus),
                contentDescription = "Add Tag",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                    .clickable {
                        addTag(context, newTagName); loadTags(context);tagsLocal =
                        tags; newTagName = ""
                    })
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            items(tagsLocal) { tag ->
                tagItem(
                    tag,
                    {
                        deckToModify?.tags?.add(tag);
                        reloadTags()
                    })
            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { disappear() }) {

                Text(
                    text = getTranslation("Close"),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun tagItem(tag: Tag, addTag: (() -> Unit)? = null, icon:Int = R.drawable.plus){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.inverseOnSurface)
            .padding(6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment =  Alignment.CenterVertically
    ) {
        ColorIndicator(Color(parseColor(tag.color)))
        Text(
            text = tag.tagName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.padding(5.dp)
        )
        if(addTag != null) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Add Tag",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                        .clickable {
                            addTag()
                        },
                )
            }
        }

    }
}

@Composable
fun ColorIndicator(
    color: Color,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
@Preview
fun preview2(){
    launchOnRuleToModify = remember {mutableStateOf(LaunchOnRule(name = "hii", appList = mutableListOf(), flashcardList = mutableListOf(), deckList = mutableListOf()))}
    // Apply Material 3 Theme with Dynamic Colors
    //Greeter(context = LocalContext.current,::checkAndRequestPermissions1, arePermissionsMissing = ::areAnyPermissionsMissing1)
    Material3AppTheme {
        flashcardsAddedToDeck.add(Flashcard(1,"adfdsdsf","fgdhjjt", "   ", "   ", 0))
        val navController = rememberNavController()
        val colorScheme = MaterialTheme.colorScheme
        Log.d("ThemeDebug", MaterialTheme.colorScheme.primary.toString())
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "modify_rule"
            ) {
                composable("modify_rule") { AddDeckScreen(context = LocalContext.current, navController = navController) }}

        }
    }
}

