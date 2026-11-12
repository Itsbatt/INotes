package com.itsbatt.inotes.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.LiquidGlassPill
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import java.io.File
import java.io.FileOutputStream

enum class DrawingTool(val label: String) {
    PEN("Bolígrafo"),
    HIGHLIGHTER("Marcador"),
    PENCIL("Lápiz"),
    ERASER("Borrador")
}

data class DrawStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float,
    val isEraser: Boolean = false
)

@Composable
fun DrawingOverlay(
    onCancel: () -> Unit,
    onSaveDrawing: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val backdrop = rememberLayerBackdrop()

    val canvasBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFAFAFC)
    val defaultInk = if (isDark) Color.White else Color.Black

    var selectedTool by remember { mutableStateOf(DrawingTool.PEN) }
    var selectedColor by remember(isDark) { mutableStateOf(defaultInk) }
    var strokeWidthPx by remember { mutableFloatStateOf(9f) }

    val strokes = remember { mutableStateListOf<DrawStroke>() }
    val redoStrokes = remember { mutableStateListOf<DrawStroke>() }
    var activePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize(1080, 1400)) }

    val paletteColors = listOf(
        defaultInk,
        IOSYellow,
        Color(0xFFFF3B30),
        Color(0xFF007AFF),
        Color(0xFF34C759)
    )

    fun currentAlpha(): Float = when (selectedTool) {
        DrawingTool.PEN -> 1f
        DrawingTool.HIGHLIGHTER -> 0.38f
        DrawingTool.PENCIL -> 0.72f
        DrawingTool.ERASER -> 1f
    }

    fun effectiveWidth(): Float = when (selectedTool) {
        DrawingTool.HIGHLIGHTER -> strokeWidthPx * 2.6f
        DrawingTool.ERASER -> strokeWidthPx * 2.8f
        DrawingTool.PENCIL -> (strokeWidthPx * 0.75f).coerceAtLeast(3f)
        DrawingTool.PEN -> strokeWidthPx
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Drawing Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(canvasBg)
                .layerBackdrop(backdrop)
                .onSizeChanged { canvasSize = it }
                .pointerInput(selectedTool, selectedColor, strokeWidthPx, canvasBg) {
                    detectDragGestures(
                        onDragStart = { start ->
                            activePoints = listOf(start)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            activePoints = activePoints + change.position
                        },
                        onDragEnd = {
                            if (activePoints.isNotEmpty()) {
                                val isEraser = selectedTool == DrawingTool.ERASER
                                strokes.add(
                                    DrawStroke(
                                        points = activePoints,
                                        color = if (isEraser) canvasBg else selectedColor,
                                        strokeWidth = effectiveWidth(),
                                        alpha = currentAlpha(),
                                        isEraser = isEraser
                                    )
                                )
                                redoStrokes.clear()
                                activePoints = emptyList()
                            }
                        },
                        onDragCancel = {
                            activePoints = emptyList()
                        }
                    )
                }
        ) {
            // Subtle dot grid like Apple Notes
            val step = 48f
            val dotColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
            var y = step
            while (y < size.height) {
                var x = step
                while (x < size.width) {
                    drawCircle(dotColor, radius = 1.5f, center = Offset(x, y))
                    x += step
                }
                y += step
            }

            fun drawStrokePath(stroke: DrawStroke) {
                if (stroke.points.size == 1) {
                    drawCircle(
                        color = stroke.color.copy(alpha = stroke.alpha),
                        radius = stroke.strokeWidth / 2f,
                        center = stroke.points.first()
                    )
                    return
                }
                val path = Path().apply {
                    moveTo(stroke.points.first().x, stroke.points.first().y)
                    for (i in 1 until stroke.points.size) {
                        val prev = stroke.points[i - 1]
                        val curr = stroke.points[i]
                        val mid = Offset((prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
                        quadraticTo(prev.x, prev.y, mid.x, mid.y)
                    }
                    lineTo(stroke.points.last().x, stroke.points.last().y)
                }
                drawPath(
                    path = path,
                    color = stroke.color.copy(alpha = stroke.alpha),
                    style = Stroke(
                        width = stroke.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            strokes.forEach { drawStrokePath(it) }
            if (activePoints.isNotEmpty()) {
                val isEraser = selectedTool == DrawingTool.ERASER
                drawStrokePath(
                    DrawStroke(
                        points = activePoints,
                        color = if (isEraser) canvasBg else selectedColor,
                        strokeWidth = effectiveWidth(),
                        alpha = currentAlpha(),
                        isEraser = isEraser
                    )
                )
            }
        }

        // Top Liquid Glass Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                onClick = onCancel
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.height(40.dp),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.size(34.dp),
                        enabled = strokes.isNotEmpty(),
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                redoStrokes.add(strokes.removeAt(strokes.lastIndex))
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Deshacer",
                            tint = if (strokes.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(34.dp),
                        enabled = redoStrokes.isNotEmpty(),
                        onClick = {
                            if (redoStrokes.isNotEmpty()) {
                                strokes.add(redoStrokes.removeAt(redoStrokes.lastIndex))
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Rehacer",
                            tint = if (redoStrokes.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(34.dp),
                        enabled = strokes.isNotEmpty(),
                        onClick = {
                            strokes.clear()
                            redoStrokes.clear()
                        }
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Limpiar",
                            tint = if (strokes.isNotEmpty()) Color(0xFFFF3B30) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                tintOverride = IOSYellow,
                onClick = {
                    val savedPath = saveDrawingBitmap(context, strokes, canvasSize, canvasBg)
                    if (savedPath != null) onSaveDrawing(savedPath) else onCancel()
                }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar dibujo", tint = Color.White)
                }
            }
        }

        // Bottom Liquid Glass Markup Tools (Tools + Colors + Widths)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Color & Size bar
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        paletteColors.forEach { color ->
                            val isSelected = selectedColor == color && selectedTool != DrawingTool.ERASER
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 28.dp else 22.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) IOSYellow else Color.White.copy(alpha = 0.6f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColor = color
                                        if (selectedTool == DrawingTool.ERASER) selectedTool = DrawingTool.PEN
                                    }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5f to 8.dp, 9f to 13.dp, 16f to 18.dp).forEach { (w, dotDp) ->
                            val isSel = strokeWidthPx == w
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) IOSYellow.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { strokeWidthPx = w },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dotDp)
                                        .clip(CircleShape)
                                        .background(if (isSel) IOSYellow else MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }
                    }
                }
            }

            // Tool selector bar
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val toolItems = listOf(
                        Triple(DrawingTool.PEN, Icons.Default.Create, "Boli"),
                        Triple(DrawingTool.HIGHLIGHTER, Icons.Default.Brush, "Marcador"),
                        Triple(DrawingTool.PENCIL, Icons.Default.Edit, "Lápiz"),
                        Triple(DrawingTool.ERASER, Icons.Default.AutoFixHigh, "Goma")
                    )
                    toolItems.forEach { (tool, icon, label) ->
                        val isSel = selectedTool == tool
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSel) IOSYellow.copy(alpha = 0.22f) else Color.Transparent)
                                .clickable { selectedTool = tool }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSel) IOSYellow else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) IOSYellow else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun saveDrawingBitmap(
    context: Context,
    strokes: List<DrawStroke>,
    canvasSize: IntSize,
    bgColor: Color
): String? {
    if (strokes.isEmpty() || canvasSize.width <= 0 || canvasSize.height <= 0) return null
    val w = canvasSize.width.coerceAtMost(1440)
    val h = (canvasSize.height * 0.55f).toInt().coerceIn(480, 1200)
    val scaleX = w.toFloat() / canvasSize.width
    val scaleY = h.toFloat() / canvasSize.height
    val scale = minOf(scaleX, scaleY)

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(bgColor.toArgb())

    val paint = AndroidPaint().apply {
        isAntiAlias = true
        style = AndroidPaint.Style.STROKE
        strokeCap = AndroidPaint.Cap.ROUND
        strokeJoin = AndroidPaint.Join.ROUND
    }

    strokes.forEach { stroke ->
        paint.color = stroke.color.copy(alpha = stroke.alpha).toArgb()
        paint.strokeWidth = stroke.strokeWidth * scale
        if (stroke.points.size == 1) {
            val pt = stroke.points.first()
            paint.style = AndroidPaint.Style.FILL
            canvas.drawCircle(pt.x * scale, pt.y * scale, (stroke.strokeWidth * scale) / 2f, paint)
            paint.style = AndroidPaint.Style.STROKE
        } else {
            val path = AndroidPath().apply {
                moveTo(stroke.points.first().x * scale, stroke.points.first().y * scale)
                for (i in 1 until stroke.points.size) {
                    lineTo(stroke.points[i].x * scale, stroke.points[i].y * scale)
                }
            }
            canvas.drawPath(path, paint)
        }
    }

    val dir = File(context.filesDir, "drawings").apply { mkdirs() }
    val outFile = File(dir, "drawing_${System.currentTimeMillis()}.png")
    FileOutputStream(outFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 92, out)
    }
    bitmap.recycle()
    return outFile.absolutePath
}
