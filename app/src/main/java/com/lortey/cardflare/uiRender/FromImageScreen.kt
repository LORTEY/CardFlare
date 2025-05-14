package com.lortey.cardflare.uiRender

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lortey.cardflare.Aspect
import com.lortey.cardflare.FinalColumnsGlobal
import com.lortey.cardflare.MappedTo
import com.lortey.cardflare.R
import com.lortey.cardflare.TextColumn
import com.lortey.cardflare.TextLine
import com.lortey.cardflare.aspectsToPairTranslation
import com.lortey.cardflare.detectTapAndDragGestures
import com.lortey.cardflare.extractTextFromUri
import com.lortey.cardflare.finalTranslationToListOfFlashcards
import com.lortey.cardflare.getTranslation
import com.lortey.cardflare.transformToColumns
import com.lortey.cardflare.uriToBitmap
import okhttp3.internal.wait
import java.lang.Float.min
private fun createDefaultBitmap(): ImageBitmap {
    return createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap()
}
class ImageViewModel : ViewModel() {
    var imageUri by mutableStateOf<Uri?>(null)
        private set

    fun updateUri(uri: Uri?) {
        imageUri = uri
    }
}
var imageUri:Uri? = null
var boxLines:MutableList<TextLine> =  mutableListOf()
@Composable
fun ImagePickerScreen(navController: NavController,context:Context) {
    var isLoading by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(true) }
    var reload by remember { mutableStateOf(true) }
    var showTable by remember { mutableStateOf(false) }
    val mutableImageBitmap by remember {
        mutableStateOf(
                uriToBitmap(context, imageUri!!)!!.asImageBitmap())
    }



    var scaledposition by remember { mutableStateOf(Offset(0f,0f)) }



    BackHandler {
        if(showTable){
            showTable = false
        }else{
            navController.popBackStack()
        }
    }
    var boxesDraggedOverThisDrag: MutableList<TextLine> = mutableListOf()
    var currentMapTo by remember{ mutableStateOf( MappedTo.Side_A) }
    if(!showTable) {
        Log.d("cardflare9", "${mutableImageBitmap.colorSpace} ")
        Column(modifier = Modifier.padding(WindowInsets.systemBars.asPaddingValues())) {
            //item {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                //.background(MaterialTheme.colorScheme.inverseOnSurface)

                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

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
            if(imageUri != null){
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
            if (imageUri != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { showTable = true }) {
                        Text(getTranslation("Next"))
                    }
                }
            }
        }
    }
    if(showTable){
        Column(modifier = Modifier.fillMaxSize().padding(WindowInsets.systemBars.asPaddingValues())){
            DataTable(Columns = transformToColumns(boxLines), modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { flashcardsAddedToDeck.addAll(
                    finalTranslationToListOfFlashcards(FinalColumnsGlobal, context)
                )
                    navController.navigate("deck_add_screen")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()) {
                    Text("Add")
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        reload = !reload
    }
}

@Composable
fun DataTable(
    modifier: Modifier = Modifier,
    Columns: Pair<MutableList<TextColumn>, MutableList<TextColumn>>
) {
    var finalColumns by remember{
        mutableStateOf(aspectsToPairTranslation(columns = Columns))
    }
    LaunchedEffect(Unit) { FinalColumnsGlobal = finalColumns }
    val skippedAmount by remember{ mutableIntStateOf(finalColumns.count { it.first.firstOrNull()?.aspects == Aspect.IGNORED || it.second.firstOrNull()?.aspects == Aspect.IGNORED }) }
    LazyColumn(modifier = modifier)
    {
        Log.d("cardflareRecomp", finalColumns.toString())
        itemsIndexed(finalColumns){index, pairOfLines->
            Row(modifier= Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.SpaceAround, ){

                Text(text = if(pairOfLines.first.firstOrNull()?.aspects == Aspect.IGNORED || pairOfLines.second.firstOrNull()?.aspects == Aspect.IGNORED)
                    "X"
                else
                    (index + 1 - skippedAmount).toString(),
                    color = if(pairOfLines.first.firstOrNull()?.aspects == Aspect.IGNORED || pairOfLines.second.firstOrNull()?.aspects == Aspect.IGNORED) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.width(50.dp)
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
                        Text(text = line.text,
                            color = if(line.aspects == Aspect.IGNORED) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                        )
                        var aspect: Aspect by remember { mutableStateOf(line.aspects) }
                        functionPanel(aspect = aspect ,
                            onLinkAction = {newState->
                                if(newState){
                                    line.aspects = Aspect.LINKED
                                    aspect = Aspect.LINKED
                                }else{
                                    line.aspects = Aspect.NONE
                                    aspect = Aspect.NONE
                                }
                                finalColumns = aspectsToPairTranslation(columns = Columns)
                                FinalColumnsGlobal = finalColumns
                            },
                            onIgnoreAction = {newState->
                                if(newState){
                                    line.aspects = Aspect.IGNORED
                                    aspect = Aspect.IGNORED
                                }else{
                                    line.aspects = Aspect.NONE
                                    aspect = Aspect.NONE
                                }
                                finalColumns = aspectsToPairTranslation(columns = Columns)
                                FinalColumnsGlobal = finalColumns
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
                        Text(text = line.text,
                            color = if(line.aspects == Aspect.IGNORED) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                        )
                        var aspect: Aspect = line.aspects
                        functionPanel(aspect = aspect,
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
fun functionPanel(aspect: Aspect, onLinkAction:(Boolean) -> Unit, onIgnoreAction:(Boolean) -> Unit){
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
        IconButton(onClick = {onLinkAction(aspect != Aspect.LINKED)}, modifier = Modifier.background(
            shape = RoundedCornerShape(128.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface
        ) .size(32.dp)){
            Icon(
                painter = painterResource(id = if(aspect == Aspect.LINKED) R.drawable.unlink else R.drawable.link),
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
        IconButton(onClick = {onIgnoreAction(aspect != Aspect.IGNORED)}, modifier = Modifier.background(
            shape = RoundedCornerShape(128.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface
        ) .size(32.dp)){
            Icon(
                painter = painterResource(id = if(aspect == Aspect.IGNORED) R.drawable.visibility_off else R.drawable.visible),
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