package com.lortey.cardflare.ui.theme

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.lortey.cardflare.AppSettings
import com.lortey.cardflare.Themes

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    inversePrimary = Blue40,
    secondary = Violet80,
    onSecondary = Violet20,
    secondaryContainer = Violet30,
    onSecondaryContainer = Violet90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    tertiary = Orange80,
    onTertiary = Orange20,
    tertiaryContainer = Orange30,
    onTertiaryContainer = Orange90,
    background = Grey10,
    onBackground = Grey90,
    surface = BlueGrey30,
    onSurface = BlueGrey80,
    inverseSurface = Grey90,
    inverseOnSurface = Grey20,
    surfaceVariant = BlueGrey30,
    onSurfaceVariant = BlueGrey80,
    outline = BlueGrey80
)

private val LightColorScheme = lightColorScheme(
    primary = Blue30,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    inversePrimary = Blue80,
    secondary = Violet40,
    onSecondary = Color.White,
    secondaryContainer = Violet90,
    onSecondaryContainer = Violet10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    tertiary = Orange40,
    onTertiary = Color.White,
    tertiaryContainer = Orange90,
    onTertiaryContainer = Orange10,
    background = Grey99,
    onBackground = Grey10,
    surface = BlueGrey90,
    onSurface = BlueGrey30,
    inverseSurface = Grey20,
    inverseOnSurface = Grey90,
    surfaceVariant = BlueGrey90,
    onSurfaceVariant = BlueGrey30,
    outline = BlueGrey50

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Material3AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit){
    val appSettings = AppSettings
    require(appSettings["Use Dynamic Color"]?.state is Boolean)
    val useDynamicColors = (appSettings["Use Dynamic Color"]?.state ?: false) as Boolean && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    val colors = when{
        useDynamicColors && ((darkTheme && appSettings["Choose Theme"]?.state == Themes.AUTO) || appSettings["Choose Theme"]?.state == Themes.DARK )-> dynamicDarkColorScheme(LocalContext.current)
        useDynamicColors && ((!darkTheme && appSettings["Choose Theme"]?.state == Themes.AUTO) || appSettings["Choose Theme"]?.state == Themes.LIGHT ) -> dynamicLightColorScheme(LocalContext.current)
        ((darkTheme && appSettings["Choose Theme"]?.state == Themes.AUTO) || appSettings["Choose Theme"]?.state == Themes.DARK ) -> DarkColorScheme
        else -> LightColorScheme
    }
    Log.d("ThemeDebug", "Using dynamic colors: ${MaterialTheme.colorScheme.primary}")

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
