package com.lortey.cardflare

import android.content.Context
import kotlin.math.sqrt
import kotlin.math.pow
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lortey.cardflare.uiRender.distanceFrom
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

val CenterToWidth:Float = 0f
val CenterToHeight:Float = 0.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var extractedText by remember { mutableStateOf(mutableListOf<MutableList<TextLine>>()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        uri?.let {
            extractTextFromUri(context, it) { text ->
                extractedText = text
            }
        }
    }
    Column(Modifier.padding(16.dp)) {
        //item {
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Select Image")
            }
        //}
       // item {
            Spacer(Modifier.height(16.dp))
       // }
        //item {
            imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected image",
                    modifier = Modifier.size(300.dp)
                )

                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    )
                    {
                        items(extractedText){ column->
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                contentPadding = PaddingValues(16.dp),
                            )
                            {
                            items(column){value->
                                Text(text = value.text)

                            }
                            }
                        }
                    }
                }
            }
       // }
    }
}

fun extractTextFromUri(
    context: Context,
    uri: Uri,
    onResult: (MutableList<MutableList<TextLine>>) -> Unit
) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromFilePath(context, uri)
    var lines : MutableList<TextLine> = mutableListOf()
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val result = StringBuilder()
            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    lines.add(TextLine(
                        text = line.text,
                        locationPoint = Pair((line.boundingBox?.left?.toFloat() ?: 0f ) + (line.boundingBox?.width()?.toFloat() ?: 0f) * CenterToWidth,
                            (line.boundingBox?.top?.toFloat() ?: 0f ) + (line.boundingBox?.height()?.toFloat() ?: 0f) * CenterToHeight),
                        confidence = line.confidence
                    ))
                }
                Log.d("cardflareText", "${block.text} ${block.cornerPoints.toString()}")
                result.append(block.text).append("\n")
            }
            Log.d("cardflareText", lines.toString())
            arrangeLinesIntoColumns(lines)
            onResult(arrangeLinesIntoColumns(lines))
        }
        .addOnFailureListener { e ->
            onResult(mutableListOf())
        }
}

fun arrangeLinesIntoColumns(lines:List<TextLine>):MutableList<MutableList<TextLine>>{
    var linesLeft: MutableList<TextLine> = lines.toMutableList()
    var averageVector: Pair<Float,Float>? = null
    var valuesForThisVector:Int = 0
    var columns:MutableList<MutableList<TextLine>> = mutableListOf(mutableListOf())
    var currentLines:MutableList<Pair<TextLine, Pair<Float,Float>>> = mutableListOf()
    var currentLine:TextLine? = null
    var lastLine:TextLine
    while(linesLeft.size > 0) {
        while (linesLeft.size > 0) {
            if (currentLine == null) {
                currentLine = getUpperMostLine(linesLeft)
                linesLeft.remove(currentLine)
                averageVector = null
                valuesForThisVector = 0
            } else {
                lastLine = currentLine
                currentLine = getClosest(currentLine.locationPoint, averageVector, linesLeft)
                linesLeft.remove(currentLine)
                valuesForThisVector++
                val newVector = Pair(
                    lastLine.locationPoint.first - (currentLine?.locationPoint?.first ?: 0f),
                    lastLine.locationPoint.second - (currentLine?.locationPoint?.second ?: 0f)
                )
                if (averageVector != null) {
                    averageVector = Pair(
                        ((averageVector.first * valuesForThisVector) + newVector.first) / (valuesForThisVector + 1),
                        ((averageVector.second * valuesForThisVector) + newVector.second) / (valuesForThisVector + 1)
                    )
                    valuesForThisVector++
                } else {
                    averageVector = newVector
                    valuesForThisVector = 1
                }
                currentLines.add(Pair(lastLine, newVector))
                if(findOutliers(currentLines).size > 0){
                    break
                }

            }
        }
        val outliers = findOutliers(currentLines)
        for (x in 0 until currentLines.size){
            if(currentLines[x] in outliers){
                columns.add(currentLines.map{it.first}.subList(0,x).toMutableList())
                linesLeft.addAll(currentLines.map{it.first}.subList(x,currentLines.size))
                break
            }
        }
        valuesForThisVector = 0
        currentLine = null
        currentLines = mutableListOf()
        averageVector = null
    }
    Log.d("cardflareColumns", columns.toString())
    return columns
}

fun Pair<Float, Float>.magnitude(): Float {
    return sqrt(first.pow(2) + second.pow(2))
}

fun findOutliers(vectors: List<Pair<TextLine,Pair<Float, Float>>>, zThreshold: Float = 3f): List<Pair<TextLine,Pair<Float, Float>>> {
    val magnitudes = vectors.map { it.second.magnitude() }
    val mean = magnitudes.average().toFloat()
    val stdDev = sqrt(magnitudes.map { (it - mean).pow(2) }.average()).toFloat()

    return vectors.filter { vec ->
        val zScore = (vec.second.magnitude() - mean) / stdDev
        abs(zScore) > zThreshold
    }
}

fun getClosest(cords: Pair<Float,Float>, vector :Pair<Float,Float>? = null, lines:List<TextLine>):TextLine? {
    val cordsToSearch: Pair<Float, Float> =
        Pair(cords.first + (vector?.first ?: 0f), cords.second + (vector?.second ?: 0f))
    return lines.minByOrNull {
        distanceFrom(it.locationPoint.first.toInt(), it.locationPoint.second.toInt(), cordsToSearch.first.toInt(),cordsToSearch.second.toInt())
    }
}

fun getUpperMostLine(lines:List<TextLine>): TextLine?{
    return lines.minByOrNull {
        it.locationPoint.second
    }
}
data class TextLine(
    val text: String,
    val locationPoint:Pair<Float,Float>,
    val confidence: Float?
)

data class TextColumn(
    var lines: MutableList<TextLine>,
    var aspects: MutableList<Aspect?>
)

enum class Aspect{
    LINKED, // Was linked to the upper one
    IGNORED, //Is Ignored

}