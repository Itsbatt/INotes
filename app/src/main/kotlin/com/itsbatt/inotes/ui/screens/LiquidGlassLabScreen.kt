package com.itsbatt.inotes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.LiquidGlassConfig
import com.itsbatt.inotes.ui.LiquidGlassPill
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlin.math.roundToInt

@Composable
fun LiquidGlassLabScreen(
    config: LiquidGlassConfig,
    isDarkTheme: Boolean,
    onConfigChange: (LiquidGlassConfig) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onExportBackup: (Uri) -> Unit = {},
    onImportBackup: (Uri) -> Unit = {},
    onBack: () -> Unit
) {
    val backdrop = rememberLayerBackdrop()
    var selectedPreset by remember { mutableStateOf("iOS 26") }

    val exportZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) onExportBackup(uri)
    }

    val importZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportBackup(uri)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Rich colorful backdrop (sibling behind all drawBackdrop controls, never wrapping them)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .layerBackdrop(backdrop)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFB300).copy(alpha = 0.42f), Color.Transparent)
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.18f, size.height * 0.18f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF34AADC).copy(alpha = 0.40f), Color.Transparent)
                    ),
                    radius = size.width * 0.60f,
                    center = Offset(size.width * 0.85f, size.height * 0.35f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFF2D55).copy(alpha = 0.30f), Color.Transparent)
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.25f, size.height * 0.78f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF5856D6).copy(alpha = 0.32f), Color.Transparent)
                    ),
                    radius = size.width * 0.50f,
                    center = Offset(size.width * 0.82f, size.height * 0.88f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 62.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
                Text(
                    text = "Liquid Glass",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = "Prueba interruptores, deslizadores y lente óptica en vivo",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 16.dp)
                )

                // Interactive Draggable Liquid Glass Lens Demo
                DraggableGlassPreview(backdrop = backdrop, config = config)

                Spacer(modifier = Modifier.height(18.dp))

                // Presets row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "Sutil" to LiquidGlassConfig(true, false, true, 6f, 6f, 10f, 0.65f),
                        "iOS 26" to LiquidGlassConfig(true, true, true, 12f, 12f, 20f, 0.54f),
                        "Extremo" to LiquidGlassConfig(true, true, true, 4f, 24f, 40f, 0.22f)
                    )
                    presets.forEach { (label, presetConfig) ->
                        val isSelected = selectedPreset == label
                        LiquidGlassPill(
                            backdrop = backdrop,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = CircleShape,
                            tintOverride = if (isSelected) IOSYellow else null,
                            onClick = {
                                selectedPreset = label
                                onConfigChange(presetConfig)
                            }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Switches Card
                GroupedCard {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        LiquidGlassSwitchRow(
                            title = "Refracción óptica (Lens)",
                            subtitle = "Distorsión de lente física en los bordes",
                            checked = config.enableLens,
                            backdrop = backdrop,
                            config = config,
                            onCheckedChange = {
                                selectedPreset = "Personalizado"
                                onConfigChange(config.copy(enableLens = it))
                            },
                            showDivider = true
                        )
                        LiquidGlassSwitchRow(
                            title = "Aberración cromática",
                            subtitle = "Dispersión RGB prismática en los bordes",
                            checked = config.chromaticAberration,
                            backdrop = backdrop,
                            config = config,
                            onCheckedChange = {
                                selectedPreset = "Personalizado"
                                onConfigChange(config.copy(chromaticAberration = it))
                            },
                            showDivider = true
                        )
                        LiquidGlassSwitchRow(
                            title = "Vibrancy",
                            subtitle = "Realce luminoso de colores tras el cristal",
                            checked = config.enableVibrancy,
                            backdrop = backdrop,
                            config = config,
                            onCheckedChange = {
                                selectedPreset = "Personalizado"
                                onConfigChange(config.copy(enableVibrancy = it))
                            },
                            showDivider = true
                        )
                        LiquidGlassSwitchRow(
                            title = "Modo Oscuro",
                            subtitle = "Alterna la paleta clara/oscura en tiempo real",
                            checked = isDarkTheme,
                            backdrop = backdrop,
                            config = config,
                            onCheckedChange = onDarkThemeChange,
                            showDivider = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sliders Card
                GroupedCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LiquidGlassSliderRow(
                            label = "Desenfoque (Blur)",
                            valueText = "${config.blurDp.roundToInt()} dp",
                            value = config.blurDp,
                            valueRange = 0f..24f,
                            backdrop = backdrop,
                            config = config,
                            onValueChange = {
                                selectedPreset = "Personalizado"
                                onConfigChange(config.copy(blurDp = it))
                            }
                        )
                        LiquidGlassSliderRow(
                            label = "Altura de lente",
                            valueText = "${config.refractionHeightDp.roundToInt()} dp",
                            value = config.refractionHeightDp,
                            valueRange = 0f..30f,
                            backdrop = backdrop,
                            config = config,
                            onValueChange = {
                                selectedPreset = "Personalizado"
                                onConfigChange(config.copy(refractionHeightDp = it))
                            }
                        )
                        LiquidGlassSliderRow(
                            label = "Intensidad de refracción",
                            valueText = "${config.refractionAmountDp.roundToInt()} dp",
                            value = config.refractionAmountDp,
                            valueRange = 0f..48f,
                            backdrop = backdrop,
                            config = config,
                            onValueChange = {
                                selectedPreset = "Personalizado"
                                onConfigChange(config.copy(refractionAmountDp = it))
                            }
                        )
                        LiquidGlassSliderRow(
                            label = "Opacidad del cristal",
                            valueText = "${(config.surfaceAlpha * 100).roundToInt()}%",
                            value = config.surfaceAlpha,
                            valueRange = 0.05f..0.85f,
                            backdrop = backdrop,
                            config = config,
                            onValueChange = {
                                selectedPreset = "Personalizado"
                                onConfigChange(config.copy(surfaceAlpha = it))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Backup section (moved into Settings)
                Text(
                    text = "COPIA DE SEGURIDAD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassPill(
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = CircleShape,
                        onClick = { exportZipLauncher.launch("inotes_backup_${System.currentTimeMillis()}.zip") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = IOSYellow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar .zip", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    LiquidGlassPill(
                        backdrop = backdrop,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = CircleShape,
                        onClick = { importZipLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = IOSYellow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importar .zip", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset button
                LiquidGlassPill(
                    backdrop = backdrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = CircleShape,
                    onClick = {
                        selectedPreset = "iOS 26"
                        onConfigChange(LiquidGlassConfig())
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = IOSYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Restablecer valores iOS 26",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IOSYellow
                        )
                    }
                }
            }

        // Floating Liquid Glass Back Pill
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.height(38.dp),
                shape = CircleShape,
                onClick = onBack
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Volver",
                        tint = IOSYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Carpetas",
                        color = IOSYellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableGlassPreview(
    backdrop: LayerBackdrop,
    config: LiquidGlassConfig
) {
    val previewBackdrop = rememberLayerBackdrop()
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "lensScale"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val density = LocalDensity.current
        val maxW = with(density) { maxWidth.toPx() }
        val maxH = with(density) { maxHeight.toPx() }

        // Colorful striped & text layer inside preview so refraction is unmistakable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(previewBackdrop)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFF9500),
                            Color(0xFFFF2D55),
                            Color(0xFF5856D6),
                            Color(0xFF34AADC)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "iOS 26 LIQUID GLASS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Arrastra la cápsula para probar la lente",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Draggable refracting glass pill
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(width = 164.dp, height = 68.dp)
                .shadow(12.dp, CircleShape)
                .drawBackdrop(
                    backdrop = previewBackdrop,
                    shape = { CircleShape },
                    effects = {
                        if (config.enableVibrancy) vibrancy()
                        if (config.blurDp > 0f) blur((config.blurDp * 0.5f).dp.toPx())
                        if (config.enableLens) {
                            lens(
                                refractionHeight = (config.refractionHeightDp + 6f).dp.toPx(),
                                refractionAmount = (config.refractionAmountDp + 10f).dp.toPx(),
                                chromaticAberration = config.chromaticAberration
                            )
                        }
                    },
                    onDrawSurface = {
                        drawRect(Color.White.copy(alpha = if (isDragging) 0.08f else 0.18f))
                    }
                )
                .border(
                    BorderStroke(1.2.dp, Color.White.copy(alpha = 0.85f)),
                    CircleShape
                )
                .pointerInput(maxW, maxH) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val limitX = (maxW / 2f - 100f).coerceAtLeast(20f)
                            val limitY = (maxH / 2f - 50f).coerceAtLeast(20f)
                            offsetX = (offsetX + dragAmount.x).coerceIn(-limitX, limitX)
                            offsetY = (offsetY + dragAmount.y).coerceIn(-limitY, limitY)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lente viva",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    backdrop: LayerBackdrop,
    config: LiquidGlassConfig,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LiquidGlassToggle(
                checked = checked,
                backdrop = backdrop,
                config = config,
                onCheckedChange = onCheckedChange
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun LiquidGlassToggle(
    checked: Boolean,
    backdrop: LayerBackdrop,
    config: LiquidGlassConfig,
    onCheckedChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val thumbFraction by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "toggleThumb"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 1.18f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "togglePress"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF34C759) else MaterialTheme.colorScheme.surfaceVariant,
        label = "toggleTrack"
    )

    val density = LocalDensity.current
    val thumbOffsetPx = with(density) { (24.dp * thumbFraction).toPx() }

    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 34.dp)
            .clip(CircleShape)
            .background(trackColor)
            .pointerInput(checked) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onCheckedChange(!checked) }
                )
            }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .size(width = 28.dp, height = 28.dp)
                .shadow(4.dp, CircleShape)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        if (config.enableVibrancy) vibrancy()
                        if (config.enableLens && isPressed) {
                            lens(
                                refractionHeight = 14.dp.toPx(),
                                refractionAmount = 22.dp.toPx(),
                                chromaticAberration = config.chromaticAberration
                            )
                        }
                    },
                    onDrawSurface = {
                        drawRect(Color.White.copy(alpha = if (isPressed) 0.25f else 0.94f))
                    }
                )
                .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
        )
    }
}

@Composable
private fun LiquidGlassSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    backdrop: LayerBackdrop,
    config: LiquidGlassConfig,
    onValueChange: (Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.28f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "sliderThumbScale"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = IOSYellow
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val density = LocalDensity.current
            val thumbWidthPx = with(density) { 36.dp.toPx() }
            val trackWidthPx = with(density) { maxWidth.toPx() }
            val usableWidthPx = (trackWidthPx - thumbWidthPx).coerceAtLeast(1f)
            val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
                .coerceIn(0f, 1f)

            fun updateFromX(x: Float) {
                val newFraction = ((x - thumbWidthPx / 2f) / usableWidthPx).coerceIn(0f, 1f)
                val newValue = valueRange.start + newFraction * (valueRange.endInclusive - valueRange.start)
                onValueChange(newValue)
            }

            // Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .pointerInput(valueRange) {
                        detectTapGestures { offset -> updateFromX(offset.x) }
                    }
                    .pointerInput(valueRange) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                updateFromX(offset.x)
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                updateFromX(change.position.x)
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(IOSYellow)
                )
            }

            // Liquid Glass Thumb
            Box(
                modifier = Modifier
                    .offset { IntOffset((usableWidthPx * fraction).roundToInt(), 0) }
                    .graphicsLayer {
                        scaleX = thumbScale
                        scaleY = thumbScale
                    }
                    .size(width = 36.dp, height = 26.dp)
                    .shadow(6.dp, CircleShape)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {
                            if (config.enableVibrancy) vibrancy()
                            if (config.enableLens) {
                                lens(
                                    refractionHeight = (if (isDragging) 14f else 8f).dp.toPx(),
                                    refractionAmount = (if (isDragging) 24f else 12f).dp.toPx(),
                                    chromaticAberration = config.chromaticAberration
                                )
                            }
                        },
                        onDrawSurface = {
                            drawRect(Color.White.copy(alpha = if (isDragging) 0.16f else 0.90f))
                        }
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.95f), CircleShape)
            )
        }
    }
}
