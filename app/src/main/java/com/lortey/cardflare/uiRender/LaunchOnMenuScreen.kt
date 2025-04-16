package com.lortey.cardflare.uiRender

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.parseColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lortey.cardflare.AppInfo
import com.lortey.cardflare.getListOfApps
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DismissState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.createBitmap
import com.lortey.cardflare.appsSearch
import com.lortey.cardflare.sortDecks
import androidx.core.graphics.toColorInt
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lortey.cardflare.Deck
import com.lortey.cardflare.Flashcard
import com.lortey.cardflare.LaunchOnRule
import com.lortey.cardflare.R
import com.lortey.cardflare.addAppToRule
import com.lortey.cardflare.launchOnRules
import com.lortey.cardflare.loadData
import com.lortey.cardflare.loadLaunchOnRules
import com.lortey.cardflare.multipleDeckMoveToBin
import com.lortey.cardflare.removeAppFromRule
import com.lortey.cardflare.saveLaunchOnRules
import com.lortey.cardflare.ui.theme.Material3AppTheme

var appsInfo:List<AppInfo> = listOf()
@Composable
@Preview
fun preview(){

        // Apply Material 3 Theme with Dynamic Colors
        //Greeter(context = LocalContext.current,::checkAndRequestPermissions1, arePermissionsMissing = ::areAnyPermissionsMissing1)
        Material3AppTheme {
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

                    composable("modify_rule") { ModifyRule(context = LocalContext.current, navController = navController) }}

            }
        }
}
@Composable
fun LaunchOnMenu(context: Context, navController: NavController){

    var apps = remember { mutableStateListOf<AppInfo>() }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember{ mutableStateOf("")}
    // Runs once when the composable enters composition
    LaunchedEffect(Unit) {
        isLoading = true
        apps.clear()
        apps.addAll(getListOfApps(context))
        isLoading = false
    }
    LaunchedEffect(apps) {
        appsInfo = apps
    }
    Box(modifier = Modifier
        .padding(WindowInsets.systemBars.asPaddingValues())
        .background(MaterialTheme.colorScheme.background)){
        LazyColumn {
            items(launchOnRules){rule ->
                Row(horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.clickable {
                        launchOnRuleToModify.value = rule
                        lastOpenedRule = rule.name
                        navController.navigate("modify_rule")}){
                    Text(text = rule.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface)
                }
            }
        }

        //SearchableAppListLoad(isLoading, apps, searchQuery)
        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
            Column(horizontalAlignment = Alignment.End,
                modifier = Modifier
                    //.width(128.dp)
                    .align(Alignment.End)) {
                UniversalAddMenu(appearAddMenu, changeVisibility = {appearAddMenu = !appearAddMenu},
                    listOf(AddMenuEntry(Name = "Add Rule", Icon = R.drawable.nav_arrow_down,
                            Action = {
                                launchOnRuleToModify.value = LaunchOnRule(name = "NewLaunchOnRule", appList = mutableListOf(), flashcardList = mutableListOf(), deckList = mutableListOf())
                                lastOpenedRule = null
                                navController.navigate("modify_rule")
                            })
                    ))
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyRule(context: Context,navController: NavController){
    val state by remember { launchOnRuleToModify }
    var name by remember(state) { mutableStateOf(state?.name ?: "NewLaunchOnRule") }
    var listOfApps by remember(state) { mutableStateOf(state?.appList ?: mutableListOf()) }
    var listOfDecks by remember(state){ mutableStateOf(state?.deckList ?: mutableListOf())}
    var flashcardList = mutableListOf<Flashcard>()
    val apps: List<AppInfo> = appsInfo
    val appMap: Map<String, AppInfo> = remember(apps) { apps.associateBy { it.packageName } }
    var appearAppAddMenu by remember { mutableStateOf(false) }
    var appearAddDeckMenu by remember{ mutableStateOf(false)}
    Log.d("cardflare3", launchOnRuleToModify.toString())

    LaunchedEffect(Unit) {  // Run once when composable enters composition
        if (state == null) {
            launchOnRuleToModify.value = LaunchOnRule(
                name = name,
                appList = listOfApps,
                flashcardList = mutableListOf(),
                deckList = mutableListOf()
            )
        }
    }

    Column(modifier = Modifier
        .padding(WindowInsets.systemBars.asPaddingValues())
        .background(MaterialTheme.colorScheme.background)) {

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,

                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,

                cursorColor = MaterialTheme.colorScheme.primary,

                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.padding(10.dp).fillMaxWidth()
        )
        Column(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.inverseOnSurface)){
            LazyColumn(modifier = Modifier
                .padding(10.dp)) {
                items(listOfApps){packageName->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = drawableToBitmap(appMap[packageName]?.icon).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(text = appMap[packageName].let{it?.name ?: packageName },
                            modifier = Modifier.weight(1f).padding(horizontal = 5.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,)
                        Icon(
                            painter = painterResource(id = R.drawable.nav_arrow_down),
                            contentDescription = "remove app from rule",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                //.size(128.dp)
                                .padding(5.dp)
                                .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                                .clickable { removeAppFromRule(packageName) })
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()){
                Text(text = "Add App",)
                Icon(
                    painter = painterResource(id = R.drawable.plus_circle),
                    contentDescription = "Add App",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        //.size(128.dp)
                        .padding(15.dp)
                        .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                        .clickable { appearAppAddMenu = true })
            }
            if(appearAppAddMenu) {
                Popup(
                    properties = PopupProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        focusable = true
                    ),
                    alignment = Alignment.Center,
                    onDismissRequest = { appearAppAddMenu = false },

                    ) {
                    Box(modifier = Modifier.padding(50.dp).fillMaxSize().focusable()) {
                        SearchableAppListLoad(apps, context)
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(6.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Button(
                                onClick = { appearAppAddMenu = false }) {

                                Text(
                                    text = "Close",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            }
        Column(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.inverseOnSurface)){
            LazyColumn(modifier = Modifier
                .padding(10.dp)) {
                items(listOfDecks){deckName->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = deckName.name,
                            modifier = Modifier.weight(1f).padding(horizontal = 5.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()){
                Text(text = "Add Decks",)
                Icon(
                    painter = painterResource(id = R.drawable.plus_circle),
                    contentDescription = "Add App",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        //.size(128.dp)
                        .padding(15.dp)
                        .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                        .clickable { appearAddDeckMenu = true })
            }
            if(appearAddDeckMenu){
                Popup(
                    properties = PopupProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        focusable = true
                    ),
                    alignment = Alignment.Center,
                    onDismissRequest = { appearAddDeckMenu = false },

                    ) {
                    Box(modifier = Modifier.padding(50.dp).fillMaxSize().focusable()) {
                        SelectDecks(context = context,
                            decksCurrentlySelected = listOfDecks,
                            SelectedDecks = {decks ->
                                appearAddDeckMenu = false
                                listOfDecks = decks
                            })

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(6.dp)
                                .align(Alignment.BottomCenter)
                        ) {

                        }
                    }
                }
            }

        }

        Button(

            onClick = {
                if(launchOnRuleToModify.value != null){
                    var rulesToSave:MutableList<LaunchOnRule> = launchOnRules.toMutableList()
                    if(lastOpenedRule == name || !rulesToSave.any{it.name == name}){
                        if(lastOpenedRule != null){
                            rulesToSave = rulesToSave.filterNot { it.name == lastOpenedRule }.toMutableList()
                        }
                        rulesToSave = rulesToSave.apply{add(launchOnRuleToModify.value!!.copy(name = name))}
                        saveLaunchOnRules(context = context, rulesToSave)
                        launchOnRules = loadLaunchOnRules(context)
                        navController.popBackStack()
                    }else{
                        Toast.makeText(context, "Rule named: \"${name}\" already exists. Change the name of current edited rule.", Toast.LENGTH_SHORT).show()
                    }

                }

            }

                ) {

            Text(
                text = "Save Rule",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SelectDecks(context: Context, decksCurrentlySelected:List<Deck>, SelectedDecks: (MutableList<Deck>) -> Unit){
    var selectMode by remember{ mutableStateOf(false)}

    val decksLoaded = loadData( context = context, filename = "")
    Log.d("cardflare4", decksLoaded.toString())
    val decksSelected = remember { mutableStateListOf<Boolean>().apply {
        addAll(List(decksLoaded.size) { false })
    } }
    LaunchedEffect(Unit) {
        decksLoaded.forEachIndexed{index, deckInFunction->
            decksCurrentlySelected.forEach { deck ->
                if (deckInFunction.filename == deck.filename) {
                    decksSelected[index] = true
                }
            }
        }
    }
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.inverseOnSurface, shape = MaterialTheme.shapes.medium)){
// Choose Deck to Activate
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize().weight(1f),
            contentPadding = PaddingValues(16.dp),
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            items(decksLoaded.size) { index ->
                Text(
                    text = decksLoaded[index].name,
                    color = if (decksSelected[index]) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = false
                        )
                        .background(
                            if (decksSelected[index]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
                        )
                        .fillMaxWidth(0.5f)
                        .height(100.dp)
                        .padding(10.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    decksSelected[index] = true
                                    selectMode = true
                                },
                                onTap = {
                                    decksSelected[index] =
                                        !decksSelected[index] //flips ones to zeroes and vice versa

                                }
                            )
                        },
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )


            }
        }
        Button(onClick = {
            var decksToSelect = mutableListOf<Deck>()
            decksLoaded.forEachIndexed{index, deck ->
                if(decksSelected[index]){
                    decksToSelect.add(deck)
                }
            }
            SelectedDecks(decksToSelect)}
        ) {
            Text("ADD", modifier = Modifier.fillMaxWidth().weight(1f).size(50.dp))
        }
    }

}
@Composable
fun SearchableAppListLoad(apps:List<AppInfo>, context: Context){
    // I have no idea how to use the colors in TextField so to make a place holder I used this box
    var apps = remember { mutableStateListOf<AppInfo>() }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember{ mutableStateOf("")}
    // Runs once when the composable enters composition
    LaunchedEffect(Unit) {
        isLoading = true
        apps.clear()
        apps.addAll(getListOfApps(context))
        isLoading = false
    }
    LaunchedEffect(apps) {
        appsInfo = apps
    }
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.inverseOnSurface, shape = MaterialTheme.shapes.medium)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height(50.dp)
                .background(
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
        ) {
            // I have no idea how to use the colors in TextField so to make a place holder I used this box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .weight(1.0f)
                    .background(
                        Color("#00000000".toColorInt()),
                        shape = RoundedCornerShape(10.dp)
                    )
                //.align(Alignment.CenterVertically)
            ) {
                // Text field for searching card decks
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search Apps...",
                        color = MaterialTheme.colorScheme.onBackground, // Placeholder text color
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }
        val appsFiltered =
            remember(searchQuery) { appsSearch(searchQuery = searchQuery, appList = apps) }
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp).align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn() {
                    items(appsFiltered) { app ->
                        Row() {
                            AppItem(app)
                        }
                    }
                }
            }
        }
    }

}
@Composable
fun AppItem(appInfo: AppInfo) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Image(
            bitmap = drawableToBitmap(appInfo.icon).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column (modifier = Modifier.weight(1f)){
            if (appInfo.name == null) {
                Text(
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            } else {
                Text(
                    text = appInfo.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                Text(
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.plus_circle),
            contentDescription = "Add App",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                .clickable { addAppToRule(appInfo)})
    }
}

fun drawableToBitmap(drawable: Drawable?): Bitmap {
    if (drawable == null) {
        // Return an empty 1x1 transparent bitmap if drawable is null
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            eraseColor(parseColor("#00000000"))
        }
    }
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}