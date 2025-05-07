package com.lortey.cardflare

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import kotlin.math.sqrt
import kotlin.math.pow
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.renderscript.Script
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContentProviderCompat.requireContext
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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.minus
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.lang.Float.min

val CenterToWidth:Float = 0f
val CenterToHeight:Float = 0.5f

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var boxLines by remember { mutableStateOf(mutableListOf<TextLine>()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var reload by remember { mutableStateOf(true) }
    var mutableImageBitmap:ImageBitmap = (uriToBitmap(context, imageUri ?: ContactsContract.Contacts.CONTENT_URI) ?: createBitmap(
        1,
        1,
        Bitmap.Config.ARGB_4444
    )).asImageBitmap()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    var scaledposition by remember { mutableStateOf(Offset(0f,0f))}
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        uri?.let {
            extractTextFromUri(context, it) { boxes ->
                boxLines  = boxes
            }
        }
    }
    var boxesDraggedOverThisDrag: MutableList<TextLine> = mutableListOf()
    var currentMapTo by remember{ mutableStateOf( MappedTo.Side_A)}
    Column(modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())) {
        //item {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .background(MaterialTheme.colorScheme.inverseOnSurface),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Select Image")
            }
            if(imageUri != null){
                Text(
                    text = getTranslation(currentMapTo.toString().replace("_"," ")),
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp).background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {

            DropdownMenuItem(
                text = { Text(getTranslation("Side A")) },
                onClick = {
                    currentMapTo = MappedTo.Side_A
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(getTranslation("Side B")) },
                onClick = {
                    currentMapTo = MappedTo.Side_B
                    expanded = false
                }
            )

        }}
        imageUri?.let { uri ->
            Canvas (modifier = Modifier.fillMaxSize()  // ← Critical for full-screen centering
                .background(Color.LightGray)
                .pointerInput(Unit) {

                    detectTapAndDragGestures(
                        onDrag = { change, dragAmount ->

                            // Check for hover during drag
                            val currentPosition = change.position
                            val scale = min(
                                size.width / mutableImageBitmap.width.toFloat(),
                                size.height / mutableImageBitmap.height.toFloat()
                            )
                            val imageOffset = Offset(
                                (size.width - mutableImageBitmap.width.toFloat() * scale) / 2,
                                (size.height - mutableImageBitmap.height.toFloat() * scale) / 2
                            )
                            val scaledPosition = (currentPosition - imageOffset) / scale

                            scaledposition = scaledPosition
                            val draggedBoxIndex = boxLines.indexOfFirst { box ->
                                scaledPosition.x in box.offset.x..(box.offset.x + box.width.toFloat()) &&
                                        scaledPosition.y in box.offset.y..(box.offset.y + box.height.toFloat())
                            }.takeIf { it != -1 }

                            if(draggedBoxIndex != null && draggedBoxIndex >= 0){
                                if (boxLines[draggedBoxIndex] !in boxesDraggedOverThisDrag){
                                    Log.d("cardflareDragged", boxLines[draggedBoxIndex].toString())
                                    boxesDraggedOverThisDrag.add(boxLines[draggedBoxIndex])
                                    if(boxLines[draggedBoxIndex].mappedTo == currentMapTo){
                                        boxLines[draggedBoxIndex].mappedTo = MappedTo.None
                                    }else{
                                        boxLines[draggedBoxIndex].mappedTo = currentMapTo
                                    }
                                    reload = !reload
                                }
                            }
                        },
                        onDragEnd = {
                            boxesDraggedOverThisDrag.clear()
                        },
                        onTap = { pos ->
                            val scale = min(
                                size.width / mutableImageBitmap.width.toFloat(),
                                size.height / mutableImageBitmap.height.toFloat()
                            )
                            val imageOffset = Offset(
                                (size.width - mutableImageBitmap.width.toFloat() * scale) / 2,
                                (size.height - mutableImageBitmap.height.toFloat() * scale) / 2
                            )
                            val scaledPosition = (pos - imageOffset) / scale

                            scaledposition = scaledPosition
                            val draggedBoxIndex = boxLines.indexOfFirst { box ->
                                scaledPosition.x in box.offset.x..(box.offset.x + box.width.toFloat()) &&
                                        scaledPosition.y in box.offset.y..(box.offset.y + box.height.toFloat())
                            }.takeIf { it != -1 }

                            if(draggedBoxIndex != null && draggedBoxIndex >= 0){
                                Log.d("cardflareDragged", boxLines[draggedBoxIndex].toString())
                                if(boxLines[draggedBoxIndex].mappedTo == currentMapTo){
                                    boxLines[draggedBoxIndex].mappedTo = MappedTo.None
                                }else{
                                    boxLines[draggedBoxIndex].mappedTo = currentMapTo
                                }
                                reload = !reload
                            }

                        }
                    )
                }){

                scale(scaleX = listOf(size.width / mutableImageBitmap.width , size.height / mutableImageBitmap.height).min(),
                    scaleY = listOf(size.width / mutableImageBitmap.width , size.height / mutableImageBitmap.height).min()) {

                    val imageWidth = mutableImageBitmap.width.toFloat()
                    val imageHeight = mutableImageBitmap.height.toFloat()
                    drawImage(
                        mutableImageBitmap,
                        topLeft = Offset(
                            (size.width - imageWidth) / 2,  // Center X
                            (size.height - imageHeight) / 2  // Center Y
                        )
                    )
                    withTransform({
                        translate((size.width - imageWidth) / 2, (size.height - imageHeight) / 2)
                    }) {
                        boxLines.forEach { box ->
                            drawRect(
                                color = if(reload)//if is just to force recomposition
                                    when(box.mappedTo){
                                    MappedTo.None -> Color.Gray.copy(alpha = 0.4f)
                                    MappedTo.Side_A -> Color.Blue.copy(alpha = 0.4f)
                                    MappedTo.Side_B -> Color.Red.copy(alpha = 0.4f)
                                }
                                else
                                    when(box.mappedTo){
                                        MappedTo.None -> Color.Gray.copy(alpha = 0.4f)
                                        MappedTo.Side_A -> Color.Blue.copy(alpha = 0.4f)
                                        MappedTo.Side_B -> Color.Red.copy(alpha = 0.4f)
                                    },
                                size = Size(box.width.toFloat(), box.height.toFloat()),
                                topLeft = box.offset,
                            )
                        }
                    }
                }

            }


            //Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {

            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {  }) {
                Text("Add")
            }
        }

    }
}

fun transformToColumns(lines:List<TextLine>):MutableList<Pair<String,String>>{
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
    val temporaryColumnAData: MutableList<Pair<TextLine,Pair<TextLine, Float>>> = mutableListOf()
    columns.first.forEach { line->
        temporaryColumnAData.add(Pair(line, getClosest(line.offset, columns.second)))
    }

    var finalColumns: MutableList<Pair<String,String>> = mutableListOf()

    temporaryColumnAData.forEachIndexed { index, lineMap->
        if(index < temporaryColumnAData.size - 1){
            if(lineMap.second.first == temporaryColumnAData[index+1].second.first){
                if(lineMap.second.second > temporaryColumnAData[index+1].second.second){
                    finalColumns[finalColumns.size - 1] = finalColumns[finalColumns.size - 1].copy(first = lineMap.first.text)
                }else{
                    finalColumns.add(Pair(lineMap.first.text, lineMap.second.first.text ))
                }
            }else{
                finalColumns.add(Pair(lineMap.first.text, lineMap.second.first.text ))
            }
        }

    }
    return finalColumns
}

fun getClosest(point:Offset,lines:List<TextLine>):Pair<TextLine, Float>{
    val minimalDistanceLine = lines.minBy {
        distanceFrom(it.offset.x.toInt(), it.offset.y.toInt(), point.x.toInt(),point.y.toInt())
    }
    return Pair(minimalDistanceLine, distanceFrom(minimalDistanceLine.offset.x.toInt(), minimalDistanceLine.offset.y.toInt(), point.x.toInt(),point.y.toInt()))
}

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
                    lines.add(TextLine(
                        text = line.text,
                        width = line.boundingBox?.width() ?: 0,
                        height  = line.boundingBox?.height() ?: 0,
                        offset = Offset((line.boundingBox?.left ?: 0) *1f, (line.boundingBox?.top ?: 0)*1f),
                        confidence = line.confidence
                    ))
                }
                Log.d("cardflareText", "${block.text} ${block.cornerPoints.toString()}")
                result.append(block.text).append("\n")
            }
            Log.d("cardflareText", lines.toString())

            onResult(lines)
        }
        .addOnFailureListener { e ->
            onResult(mutableListOf())
        }
}


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

/* I might be stupid but actually this algorithm works just dont know why i need it
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
data class TextLine(
    val text: String,
    val offset:    Offset,
    val width: Int,
    val height: Int,
    val confidence: Float?,
    var mappedTo :MappedTo  = MappedTo.None
)
enum class MappedTo {
    None, Side_A, Side_B
}
data class TextColumn(
    var lines: MutableList<TextLine>,
    var aspects: MutableList<Aspect?>
)

enum class Aspect{
    LINKED, // Was linked to the upper one
    IGNORED, //Is Ignored

}