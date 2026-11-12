package com.itsbatt.inotes.ui

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.launch

val IOSYellow = Color(0xFFEAA824)
val IOSYellowDark = Color(0xFFFFC400)

val LightBackground = Color(0xFFF2F2F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE5E5EA)
val LightTextPrimary = Color(0xFF000000)
val LightTextSecondary = Color(0xFF8E8E93)

val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF1C1C1E)
val DarkSurfaceVariant = Color(0xFF2C2C2E)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFF98989D)

// Apple Notes color tokens - used for accent elements when in dark mode
val AppleLightBackground = Color(0xFFFAFAFA)
val AppleLightSurface = Color(0xFFFFFFFF)
val AppleLightSurfaceVariant = Color(0xFFFAFAFA)
val AppleLightOnBackground = Color(0xFF000000)
val AppleLightOnSurface = Color(0xFF000000)
val AppleLightOnSurfaceVariant = Color(0xFF8E8E8E)

val AppleDarkBackground = Color(0xFF1C1C1E)
val AppleDarkSurface = Color(0xFF2C2C2E)
val AppleDarkSurfaceVariant = Color(0xFF3A3A3C)
val AppleDarkOnBackground = Color(0xFFFFFFFF)
val AppleDarkOnSurface = Color(0xFFFFFFFF)
val AppleDarkOnSurfaceVariant = Color(0xFF86868B)

@Stable
data class LiquidGlassConfig(
    val enableLens: Boolean = true,
    val chromaticAberration: Boolean = true,
    val enableVibrancy: Boolean = true,
    val blurDp: Float = 12f,
    val refractionHeightDp: Float = 12f,
    val refractionAmountDp: Float = 20f,
    val surfaceAlpha: Float = 0.54f
)

val LocalLiquidGlassConfig = compositionLocalOf { LiquidGlassConfig() }

@Composable
fun INotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    glassConfig: LiquidGlassConfig = LiquidGlassConfig(),
    content: @Composable () -> Unit
) {
    // Use Apple Notes color palette for dark mode, original for light mode
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = IOSYellowDark,
            onPrimary = Color.Black,
            secondary = IOSYellowDark,
            onSecondary = Color.Black,
            background = AppleDarkBackground,
            onBackground = AppleDarkOnBackground,
            surface = AppleDarkSurface,
            onSurface = AppleDarkOnSurface,
            surfaceVariant = AppleDarkSurfaceVariant,
            onSurfaceVariant = AppleDarkOnSurfaceVariant
        )
    } else {
        lightColorScheme(
            primary = IOSYellow,
            onPrimary = Color.White,
            secondary = IOSYellow,
            onSecondary = Color.White,
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightTextSecondary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(20.dp),
        small = RoundedCornerShape(22.dp),
        medium = RoundedCornerShape(26.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(32.dp)
    )

    CompositionLocalProvider(LocalLiquidGlassConfig provides glassConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
            content = content
        )
    }
}

/**
 * iOS 26 Interactive Liquid Glass container powered by Kyant0/AndroidLiquidGlass (backdrop).
 * Features pointer-following radial specular glow, spring jelly deformation, intensified
 * optical lens refraction, and crystal-clear transparency on press.
 */
@Composable
fun LiquidGlassPill(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    tintOverride: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val config = LocalLiquidGlassConfig.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressAnim = remember { Animatable(0f) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else pressAnim.value,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "liquidGlassPress"
    )

    val baseTint = tintOverride ?: if (isDark) Color(0xFF2C2C2E) else Color.White
    val restAlpha = if (tintOverride != null) 0.85f else config.surfaceAlpha
    val pressedAlpha = if (tintOverride != null) 0.28f else (config.surfaceAlpha * 0.12f).coerceAtLeast(0.05f)
    val currentAlpha = restAlpha + (pressedAlpha - restAlpha) * pressProgress

    val borderAlphaTop = if (isDark) 0.30f + 0.40f * pressProgress else 0.96f
    val borderAlphaBottom = if (isDark) 0.08f + 0.24f * pressProgress else 0.38f + 0.42f * pressProgress
    val borderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = borderAlphaTop),
            (if (isDark) Color.White else Color(0xFFC7C7CC)).copy(alpha = borderAlphaBottom)
        )
    )

    val pointerTracker = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            touchOffset = down.position
            scope.launch {
                pressAnim.animateTo(
                    1f,
                    spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
                )
            }
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: break
                touchOffset = change.position
                if (!change.pressed) break
            }
            scope.launch {
                pressAnim.animateTo(
                    0f,
                    spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
                )
            }
        }
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(pointerTracker)
            .shadow(
                elevation = (8f + 8f * pressProgress).dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.14f),
                spotColor = Color.Black.copy(alpha = 0.10f)
            )
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    if (config.enableVibrancy) vibrancy()
                    val activeBlur = (config.blurDp - (config.blurDp * 0.5f) * pressProgress).coerceAtLeast(0f)
                    if (activeBlur > 0f) blur(activeBlur.dp.toPx())
                    if (config.enableLens) {
                        lens(
                            refractionHeight = (config.refractionHeightDp + 12f * pressProgress).dp.toPx(),
                            refractionAmount = (config.refractionAmountDp + 18f * pressProgress).dp.toPx(),
                            chromaticAberration = config.chromaticAberration
                        )
                    }
                },
                layerBlock = {
                    val scale = 1f + 0.08f * pressProgress
                    scaleX = scale
                    scaleY = scale
                    if (pressProgress > 0.001f && size.width > 0f && size.height > 0f) {
                        translationX = (touchOffset.x - size.width / 2f) * 0.04f * pressProgress
                        translationY = (touchOffset.y - size.height / 2f) * 0.04f * pressProgress
                    }
                },
                onDrawSurface = {
                    drawRect(baseTint.copy(alpha = currentAlpha))
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.12f else 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    if (pressProgress > 0.01f) {
                        val radius = maxOf(size.width, size.height).coerceAtLeast(60f) * 0.85f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.42f * pressProgress),
                                    Color.Transparent
                                ),
                                center = touchOffset,
                                radius = radius
                            ),
                            center = touchOffset,
                            radius = radius
                        )
                    }
                }
            )
            .border(BorderStroke(1.dp, borderBrush), shape)
            .then(clickableModifier),
        content = content
    )
}

/**
 * Subtle iOS spring press-scale animation for list cards and folder rows.
 */
fun Modifier.iosPressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iosPressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}