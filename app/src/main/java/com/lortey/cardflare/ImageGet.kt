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
import android.widget.TableLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lortey.cardflare.uiRender.PopUp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.lang.Float.min
import java.util.Collections.max

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
fun ImagePickerScreen(navController: NavController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var boxLines by remember { mutableStateOf(mutableListOf<TextLine>()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var reload by remember { mutableStateOf(true) }
    var showTable by remember { mutableStateOf(false) }
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

    BackHandler {
        if(showTable){
            showTable = false
        }else{
            navController.popBackStack()
        }
    }
    var boxesDraggedOverThisDrag: MutableList<TextLine> = mutableListOf()
    var currentMapTo by remember{ mutableStateOf( MappedTo.Side_A)}
    if(!showTable) {
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
                if (imageUri != null) {
                    Text(
                        text = getTranslation(currentMapTo.toString().replace("_", " ")),
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(200.dp)
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
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

                }
            }
            imageUri?.let { uri ->
                Canvas(
                    modifier = Modifier.fillMaxSize()
                        .weight(1f)// ← Critical for full-screen centering
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

                                    if (draggedBoxIndex != null && draggedBoxIndex >= 0) {
                                        if (boxLines[draggedBoxIndex] !in boxesDraggedOverThisDrag) {
                                            Log.d(
                                                "cardflareDragged",
                                                boxLines[draggedBoxIndex].toString()
                                            )
                                            boxesDraggedOverThisDrag.add(boxLines[draggedBoxIndex])
                                            if (boxLines[draggedBoxIndex].mappedTo == currentMapTo) {
                                                boxLines[draggedBoxIndex].mappedTo = MappedTo.None
                                            } else {
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

                                    if (draggedBoxIndex != null && draggedBoxIndex >= 0) {
                                        Log.d(
                                            "cardflareDragged",
                                            boxLines[draggedBoxIndex].toString()
                                        )
                                        if (boxLines[draggedBoxIndex].mappedTo == currentMapTo) {
                                            boxLines[draggedBoxIndex].mappedTo = MappedTo.None
                                        } else {
                                            boxLines[draggedBoxIndex].mappedTo = currentMapTo
                                        }
                                        reload = !reload
                                    }

                                }
                            )
                        }) {

                    scale(
                        scaleX = listOf(
                            size.width / mutableImageBitmap.width,
                            size.height / mutableImageBitmap.height
                        ).min(),
                        scaleY = listOf(
                            size.width / mutableImageBitmap.width,
                            size.height / mutableImageBitmap.height
                        ).min()
                    ) {

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
                            translate(
                                (size.width - imageWidth) / 2,
                                (size.height - imageHeight) / 2
                            )
                        }) {
                            boxLines.forEach { box ->
                                drawRect(
                                    color = if (reload)//if is just to force recomposition
                                        when (box.mappedTo) {
                                            MappedTo.None -> Color.Gray.copy(alpha = 0.4f)
                                            MappedTo.Side_A -> Color.Blue.copy(alpha = 0.4f)
                                            MappedTo.Side_B -> Color.Red.copy(alpha = 0.4f)
                                        }
                                    else
                                        when (box.mappedTo) {
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
                    .height(80.dp)

                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { showTable = true }) {
                    Text("Add")
                }

            }
        }
    }
        if(showTable){
            Box(modifier = Modifier.fillMaxSize().padding(WindowInsets.systemBars.asPaddingValues())){
                DataTable(Columns = transformToColumns(boxLines))
            }
        }


}

@Composable
fun DataTable(
    modifier: Modifier = Modifier,
    Columns: Pair<MutableList<TextColumn>, MutableList<TextColumn>>
) {
    var finalColumns by remember{
        mutableStateOf(aspectsToPairTranslation(columns = Columns))}
    LazyColumn()
    {
        itemsIndexed(finalColumns){index, pairOfLines->
            Row(modifier= Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.SpaceAround, ){
                Text(text = index.toString() ,modifier = Modifier.width(50.dp)
                    .fillMaxHeight()
                    //.heightIn(min = (height).dp, max = (height).dp)
                    //.onSizeChanged{ if(it.height > height) height = (it.height)}
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .padding(8.dp)
                    )

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    //.heightIn(min = (height).dp, max = (height).dp)
                    //.onSizeChanged{ if(it.height > height) height = (it.height)}
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .padding(8.dp)){
                    pairOfLines.first.forEach{ line->
                        Text(text = line.text)
                        var aspect:Aspect by remember { mutableStateOf(line.aspects)}
                        functionPanel(linked = aspect == Aspect.LINKED,
                            onLinkAction = {newState->
                                if(newState){
                                    line.aspects = Aspect.LINKED
                                    aspect = Aspect.LINKED
                                }else{
                                    line.aspects = Aspect.NONE
                                    aspect = Aspect.NONE
                                }
                                finalColumns = aspectsToPairTranslation(columns = Columns)
                            },
                            ignored = aspect == Aspect.IGNORED,
                            onIgnoreAction = {newState->
                                if(newState){
                                    line.aspects = Aspect.IGNORED
                                    aspect = Aspect.IGNORED
                                }else{
                                    line.aspects = Aspect.NONE
                                    aspect = Aspect.NONE
                                }
                                finalColumns = aspectsToPairTranslation(columns = Columns)
                            },
                        )
                    }

                }

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    //.heightIn(min = (height).dp, max = (height).dp)
                    //.onSizeChanged{ if(it.height > height) height = (it.height)}
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .padding(8.dp)){
                    pairOfLines.second.forEach{ line->
                        Text(text = line.text)
                        var aspect:Aspect by remember (line){ mutableStateOf(line.aspects)}
                        functionPanel(linked = aspect == Aspect.LINKED,
                            onLinkAction = {newState->
                                if(newState){
                                    line.aspects = Aspect.LINKED
                                    aspect = Aspect.LINKED
                                }else{
                                    line.aspects = Aspect.NONE
                                    aspect = Aspect.NONE
                                }
                                finalColumns = aspectsToPairTranslation(columns = Columns)
                            },
                            ignored = aspect == Aspect.IGNORED,
                            onIgnoreAction = {newState->
                                if(newState){
                                    line.aspects = Aspect.IGNORED
                                    aspect = Aspect.IGNORED
                                }else{
                                    line.aspects = Aspect.NONE
                                    aspect = Aspect.NONE
                                }
                                finalColumns = aspectsToPairTranslation(columns = Columns)
                            },
                        )
                    }

                }

            }
        }
    }
}

@Composable
fun functionPanel(linked:Boolean, onLinkAction:(Boolean) -> Unit, ignored:Boolean, onIgnoreAction:(Boolean) -> Unit){
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
        IconButton(onClick = {onLinkAction(!linked)}, modifier = Modifier.background(
            shape = RoundedCornerShape(128.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface
        ) .size(32.dp)){
            Icon(
                painter = painterResource(id = if(linked) R.drawable.unlink else R.drawable.link),
                contentDescription = "link",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    //.padding(10.dp)
                    .fillMaxSize()
                //.fillMaxWidth()
                //.weight(1f)
                //.padding(8.dp)
            )
        }
        IconButton(onClick = {onIgnoreAction(!ignored)}, modifier = Modifier.background(
            shape = RoundedCornerShape(128.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface
        ) .size(32.dp)){
            Icon(
                painter = painterResource(id = if(ignored) R.drawable.visibility_off else R.drawable.visible),
                contentDescription = "link",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    //.padding(10.dp)
                    .fillMaxSize()
                    //.fillMaxWidth()
                    //.weight(1f)
                    //.padding(8.dp)
            )
        }

    }
}
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

fun getClosest(point:Offset,lines:List<TextLine>):Pair<TextLine, Float>{
    val minimalDistanceLine = lines.minBy {
        distanceFrom(it.offset.x.toInt(), it.offset.y.toInt(), point.x.toInt(),point.y.toInt())
    }
    return Pair(minimalDistanceLine, distanceFrom(minimalDistanceLine.offset.x.toInt(), minimalDistanceLine.offset.y.toInt(), point.x.toInt(),point.y.toInt()))
}
fun aspectsToPairTranslation(columns:Pair<MutableList<TextColumn>,MutableList<TextColumn>>):MutableList<Pair<MutableList<TextColumn>,MutableList<TextColumn>>>{
    val columnA:MutableList<MutableList<TextColumn>> = mutableListOf()
    columns.first.forEach{ line->
        if(line.aspects == Aspect.LINKED){
            columnA[columnA.lastIndex].add(line)
        }else{
            columnA.add(mutableListOf(line))
        }
    }

    val columnB:MutableList<MutableList<TextColumn>> = mutableListOf()
    columns.second.forEach{ line->
        if(line.aspects == Aspect.LINKED){
            columnB[columnB.lastIndex].add(line)
        }else{
            columnB.add(mutableListOf(line))
        }
    }

    val finalColumns:MutableList<Pair<MutableList<TextColumn>,MutableList<TextColumn>>> = mutableListOf(Pair(
        mutableListOf(), mutableListOf()))

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
    var text :String,
    var aspects: Aspect
)

enum class Aspect{
    LINKED, // Was linked to the upper one
    IGNORED, //Is Ignored
    NONE
}