package com.lortey.cardflare.uiRender

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lortey.cardflare.Translations
import com.lortey.cardflare.getPossibleTranslations
import com.lortey.cardflare.getTranslation
import com.lortey.cardflare.ui.theme.Material3AppTheme
import androidx.compose.foundation.lazy.items

@Composable
@Preview
fun Preview5(){
    //launchOnRuleToModify = remember { mutableStateOf(LaunchOnRule(name = "hii", appList = mutableListOf(), flashcardList = mutableListOf(), deckList = mutableListOf())) }
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
                composable("modify_rule") { ChooseLanguage(context = LocalContext.current, navController) }}

        }
    }
}

@Composable
fun ChooseLanguage(context: Context, navController: NavController, languageChosenAction:((Translations) -> Unit)? = null){
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            LazyColumn(modifier = Modifier.fillMaxSize()) {

                items(getPossibleTranslations(context)){ translation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { languageChosenAction?.let{it(translation)}; navController.popBackStack() }
                            .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment =  Alignment.CenterVertically
                    ) {
                        Text(
                            text = translation.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier.padding(5.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = getTranslation(translation.typeOfTranslation.toString()),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}