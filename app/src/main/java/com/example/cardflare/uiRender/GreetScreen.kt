package com.example.cardflare.uiRender

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Row
import androidx.navigation.NavController

@Composable
fun Greeter(context: Context, permissionGrant: () -> Unit, arePermissionsMissing:() -> Boolean){
    if (arePermissionsMissing()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(50.dp)

        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().align(Alignment.Center).background(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = "Welcome",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(3.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Thank you for downloading Cardflare. Before we begin you will need to give this app permissions to do a couple things. To learn more about why we need this permissions or if you would like to learn how to use the app please click the 'Learn More' button below. If you don't have any further questions please click the 'Grant Permissions' button below.",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(5.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.padding(4.dp).fillMaxWidth()
                ) {
                    Text(
                        "Learn More",
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
                            .padding(5.dp).clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                            context.startActivity(intent)
                        },
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        "Grant Permissions",
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
                            .padding(5.dp).clickable {
                            permissionGrant()
                        },
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
