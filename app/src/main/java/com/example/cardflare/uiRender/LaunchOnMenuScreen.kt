package com.example.cardflare.uiRender

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.parseColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cardflare.AppInfo
import com.example.cardflare.GetListOfApps


@Composable
fun LaunchOnMenu(context: Context, navController: NavController){
    val apps = remember { GetListOfApps(context) }
    Log.d("cardflareSS", apps.toString())
    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        items(apps){app->
            Row(){
                AppItem(app)
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
        Text(text = appInfo.name, style = MaterialTheme.typography.bodyLarge)
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
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}