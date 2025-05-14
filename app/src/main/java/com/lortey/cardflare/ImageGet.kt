package com.lortey.cardflare

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lortey.cardflare.uiRender.distanceFrom
import java.util.*
import androidx.core.graphics.createBitmap
import androidx.navigation.NavController
import com.lortey.cardflare.uiRender.flashcardsAddedToDeck
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.Float.min

val CenterToWidth:Float = 0f
val CenterToHeight:Float = 0.5f
var FinalColumnsGlobal: MutableList<Pair<MutableList<TextColumn>, MutableList<TextColumn>>> = mutableListOf(Pair(
    mutableListOf(), mutableListOf()))

// Helper extension function for combined gesture detection
suspend fun PointerInputScope.detectTapAndDragGestures(
    onTap: (Offset) -> Unit = { },
    onDragStart: (Offset) -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = { }
) {
    coroutineScope {
        launch {
            detectDragGestures(
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd
            )
        }
        launch {
            detectTapGestures(
                onTap = onTap
            )
        }
    }
}

//transform detected text lines into two columns based on user selection
fun transformToColumns(lines:List<TextLine>):Pair<MutableList<TextColumn>,MutableList<TextColumn>>{
    val columns:Pair<MutableList<TextLine>,MutableList<TextLine>> =
        Pair(mutableListOf(),
        mutableListOf()
    )
    lines.forEach{line->
        if(line.mappedTo == MappedTo.Side_A){
            columns.first.add(line)
        }else if (line.mappedTo == MappedTo.Side_B){
            columns.second.add(line)
        }
    }
    columns.first.sortBy { it.offset.y }
    columns.second.sortBy { it.offset.y }

    val finalColumns: Pair<MutableList<TextColumn>,MutableList<TextColumn>> = Pair(mutableListOf<TextColumn>(),mutableListOf<TextColumn>())
    columns.first.forEach{ line->
        finalColumns.first.add(TextColumn(line.text, Aspect.NONE))
    }

    columns.second.forEach{ line->
        finalColumns.second.add(TextColumn(line.text, Aspect.NONE))
    }

    return finalColumns
}


//Translates aspects selected by user so idgnored, linked or none to pair of usable data
fun aspectsToPairTranslation(columns:Pair<MutableList<TextColumn>,MutableList<TextColumn>>):MutableList<Pair<MutableList<TextColumn>,MutableList<TextColumn>>>{
    val columnA:MutableList<MutableList<TextColumn>> = mutableListOf()
    columns.first.forEach{ line->
        if(line.aspects == Aspect.LINKED){
            if(columnA.size > 0){
                columnA[columnA.lastIndex].add(line)
            }else{
                columnA.add(mutableListOf(line))
            }
        }else{
            columnA.add(mutableListOf(line))
        }
    }

    val columnB:MutableList<MutableList<TextColumn>> = mutableListOf()
    columns.second.forEach{ line->
        if(line.aspects == Aspect.LINKED){
            if(columnB.size > 0){
                columnB[columnB.lastIndex].add(line)
            }else{
                columnB.add(mutableListOf(line))
            }
        }else{
            columnB.add(mutableListOf(line))
        }
    }

    val finalColumns:MutableList<Pair<MutableList<TextColumn>,MutableList<TextColumn>>> = mutableListOf()

    var a:Int = 0
    var b:Int = 0
    while(a < columnA.size || b < columnB.size){
        if(a >= columnA.size || (b < columnB.size && (columnB[b].lastOrNull()?.aspects ?: Aspect.NONE) == Aspect.IGNORED)){
            finalColumns.add(Pair(mutableListOf(), columnB[b]))
            b++
        }else if(b >= columnB.size || (a < columnA.size && (columnA[a].lastOrNull()?.aspects
                ?: Aspect.NONE) == Aspect.IGNORED)
        ){
            finalColumns.add(Pair(columnA[a], mutableListOf()))
            a++
        }else {
            finalColumns.add(Pair(columnA[a], columnB[b]))
            b++
            a++
        }
    }

    return finalColumns
}

//Runs text recognizer on selected image
fun extractTextFromUri(
    context: Context,
    uri: Uri,
    onResult: (MutableList<TextLine>) -> Unit
) {

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromFilePath(context, uri)
    var lines : MutableList<TextLine> = mutableListOf()
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val result = StringBuilder()
            for (block in visionText.textBlocks) {
                for (line in block.lines) {

                    //Converts detected lines to supported data type
                    lines.add(TextLine(
                        text = line.text,
                        width = line.boundingBox?.width() ?: 0,
                        height  = line.boundingBox?.height() ?: 0,
                        offset = Offset((line.boundingBox?.left ?: 0) *1f, (line.boundingBox?.top ?: 0)*1f),
                        confidence = line.confidence
                    ))
                }

                result.append(block.text).append("\n")
            }
            onResult(lines) // "return"
        }
        .addOnFailureListener { e ->
            onResult(mutableListOf())
        }
}

//convert uri to  bitmap to use on canvas
fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


//Final translation from data table to Flashcard Sides
fun finalTranslationToListOfFlashcards(finalColumns: MutableList<Pair<MutableList<TextColumn>, MutableList<TextColumn>>>, context: Context):MutableList<Flashcard>{
    val listOfCards: MutableList<Flashcard> = mutableListOf()

    finalColumns.forEach { pair->
        if((pair.first.size>0 || pair.first.firstOrNull()?.aspects != Aspect.IGNORED) && (pair.second.size>0 || pair.second.firstOrNull()?.aspects != Aspect.IGNORED)){

            val fsrsValue = getDefaultFSRSValue(context)
            listOfCards.add(Flashcard(id = -1, SideA = pair.first.map{it.text}.joinToString(" "), SideB = pair.second.map{it.text}.joinToString(" "), FromDeck = "", FsrsData = fsrsValue,due = getDueDate(context,fsrsValue) ))
        }
    }
    return listOfCards
}
/*I leave this algorithm here as it worked but i decided that letting user decide would be a better idea
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
}*/

//Data regarding text and position of detected text on image
data class TextLine(
    val text: String,
    val offset:    Offset,
    val width: Int,
    val height: Int,
    val confidence: Float?,
    var mappedTo :MappedTo  = MappedTo.None
)

// Says whether a canvas box was selected to be side a b or none
enum class MappedTo {
    None, Side_A, Side_B
}

//data type used in data table
data class TextColumn(
    var text :String,
    var aspects: Aspect
)

//The data table aspects
enum class Aspect{
    LINKED, // Was linked to the upper one
    IGNORED, //Is Ignored
    NONE
}