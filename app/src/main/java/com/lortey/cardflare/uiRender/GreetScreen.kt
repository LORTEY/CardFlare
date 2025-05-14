package com.lortey.cardflare.uiRender

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lortey.cardflare.getTranslation
import com.lortey.cardflare.Translations
import com.lortey.cardflare.TypeOfTranslation
import com.lortey.cardflare.updateSetting
import kotlinx.coroutines.delay
import java.util.Locale


@Composable
fun Greeter(context: Context, permissionGrant: () -> Unit, arePermissionsMissing:() -> Boolean){
    var refresh by remember{ mutableStateOf(true) }
    var permissionsMissing by remember { mutableStateOf(arePermissionsMissing()) }
    val locale = Locale.getDefault().toString()
    if(locale == "pl"){
        updateSetting("Language", Translations("Polski", TypeOfTranslation.Default))
    }
    // Auto-refreshing coroutine
    LaunchedEffect(Unit) {
        while (permissionsMissing) {
            permissionsMissing = arePermissionsMissing()
            delay(300) // refresh
        }
    }
    if(permissionsMissing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier

                    .align(Alignment.Center)
                    .background(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.background
                    )
            ) {
                Text(
                    text = getTranslation("Welcome"),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(3.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = getTranslation("Thank you for downloading Cardflare. Before we begin you will need to give this app permissions to do a couple things. To learn more about why we need this permissions or if you would like to learn how to use the app please click the 'Learn More' button below. If you don't have any further questions please click the 'Grant Permissions' button below."),
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
                    Text(
                        getTranslation("Learn More"),
                        modifier = Modifier
                            .padding(5.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(5.dp)
                            .clickable {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                                context.startActivity(intent)
                            },
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        getTranslation("Grant Permissions"),
                        modifier = Modifier
                            .padding(5.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(5.dp)
                            .clickable(onClick = { permissionGrant()}),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}