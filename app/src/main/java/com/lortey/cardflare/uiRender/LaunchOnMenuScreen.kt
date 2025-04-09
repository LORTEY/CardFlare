package com.lortey.cardflare.uiRender

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.parseColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.lortey.cardflare.appsSearch
import com.lortey.cardflare.sortDecks
import androidx.core.graphics.toColorInt


@OptIn(ExperimentalMaterial3Api::class)
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

    Column(modifier = Modifier
        .padding(WindowInsets.systemBars.asPaddingValues())
        .background(MaterialTheme.colorScheme.background)){
        // I have no idea how to use the colors in TextField so to make a place holder I used this box
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
                    onValueChange = { searchQuery = it},
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search Sets...",
                        color = MaterialTheme.colorScheme.onBackground, // Placeholder text color
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }
        SearchableAppListLoad(isLoading, apps, searchQuery)
    }


}

@Composable
fun SearchableAppListLoad(isLoading:Boolean,apps:List<AppInfo>, searchQuery:String = ""){
    val appsFiltered = remember(searchQuery){ appsSearch(searchQuery = searchQuery, appList = apps) }
    Box(modifier = Modifier.fillMaxSize()){
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp).align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn() {
                items(appsFiltered){app->
                    Row(){
                        AppItem(app)
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
        Column {
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
                    maxLines = 1
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