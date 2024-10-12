package dev.treset.treelauncher.generic

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.util.HsvColor
import kotlin.math.*

/* Heavily derived from https://github.com/godaddy/compose-color-picker */

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    color: HsvColor,
    onColorChanged: (HsvColor) -> Unit
) {
    BoxWithConstraints(modifier) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            val updatedColor by rememberUpdatedState(color)
            val updatedOnValueChanged by rememberUpdatedState(onColorChanged)

            HarmonyColorPickerWithMagnifiers(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f),
                hsvColor = updatedColor,
                onColorChanged = {
                    updatedOnValueChanged(it)
                }
            )

            BrightnessBar(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .weight(0.2f),
                onValueChanged = { value ->
                    updatedOnValueChanged(updatedColor.copy(value = value))
                },
                currentColor = updatedColor
            )
        }
    }
}

@Composable
private fun BrightnessBar(
    modifier: Modifier = Modifier,
    currentColor: HsvColor,
    onValueChanged: (Float) -> Unit
) {
    val currentColorToAlphaBrush = remember(currentColor) {
        Brush.horizontalGradient(
            listOf(
                Color(0xff000000),
                currentColor.copy(value = 1f).toColor()
            )
        )
    }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onValueChanged(
                        (down.position.x / size.width).coerceIn(0f, 1f)
                    )
                    drag(down.id) { change ->
                        if (change.positionChange() != Offset.Zero) change.consume()
                        onValueChanged(
                            (change.position.x / size.width).coerceIn(0f, 1f)
                        )
                    }
                }
            }
    ) {
        drawBar(currentColorToAlphaBrush)

        val position = currentColor.value * size.width
        drawHorizontalSelector(amount = position)
    }
}

private fun DrawScope.drawHorizontalSelector(amount: Float) {
    val halfIndicatorThickness = 4.dp.toPx()
    val strokeThickness = 1.dp.toPx()

    val offset =
        Offset(
            x = amount - halfIndicatorThickness,
            y = -strokeThickness
        )

    val selectionSize = Size(halfIndicatorThickness * 2f, this.size.height + strokeThickness * 2)
    drawSelectorIndicator(
        offset = offset,
        selectionSize = selectionSize,
        strokeThicknessPx = strokeThickness
    )
}

private fun DrawScope.drawSelectorIndicator(
    offset: Offset,
    selectionSize: Size,
    strokeThicknessPx: Float
) {
    val selectionStyle = Stroke(strokeThicknessPx)
    drawRect(
        Color.Gray,
        topLeft = offset,
        size = selectionSize,
        style = selectionStyle
    )
    drawRect(
        Color.White,
        topLeft = offset + Offset(strokeThicknessPx, strokeThicknessPx),
        size = selectionSize.inset(2 * strokeThicknessPx),
        style = selectionStyle
    )
}

private fun DrawScope.drawBar(barBrush: Brush) {
    drawRect(barBrush)
}

private fun Size.inset(amount: Float): Size {
    return Size(width - amount, height - amount)
}

@Composable
private fun HarmonyColorPickerWithMagnifiers(
    modifier: Modifier = Modifier,
    hsvColor: HsvColor,
    onColorChanged: (HsvColor) -> Unit,
) {
    val hsvColorUpdated by rememberUpdatedState(hsvColor)
    BoxWithConstraints(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp)
            .wrapContentSize()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)

    ) {
        val updatedOnColorChanged by rememberUpdatedState(onColorChanged)
        val diameterPx by remember(constraints.maxWidth) {
            mutableStateOf(constraints.maxWidth)
        }

        var animateChanges by remember {
            mutableStateOf(false)
        }
        var currentlyChangingInput by remember {
            mutableStateOf(false)
        }

        fun updateColorWheel(newPosition: Offset, animate: Boolean) {
            // Work out if the new position is inside the circle we are drawing, and has a
            // valid color associated to it. If not, keep the current position
            val newColor = colorForPosition(newPosition, IntSize(diameterPx, diameterPx), hsvColorUpdated.value)
            if (newColor != null) {
                animateChanges = animate
                updatedOnColorChanged(newColor)
            }
        }

        val inputModifier = Modifier.pointerInput(diameterPx) {
            awaitEachGesture {
                val down = awaitFirstDown(false)
                currentlyChangingInput = true
                updateColorWheel(down.position, animate = true)
                drag(down.id) { change ->
                    updateColorWheel(change.position, animate = false)
                    if (change.positionChange() != Offset.Zero) change.consume()
                }
                currentlyChangingInput = false
            }
        }

        Box(inputModifier.fillMaxSize()) {
            ColorWheel(hsvColor = hsvColor, diameter = diameterPx)
            HarmonyColorMagnifiers(
                diameterPx,
                hsvColor,
                animateChanges,
                currentlyChangingInput,
            )
        }
    }
}

private fun colorForPosition(position: Offset, size: IntSize, value: Float): HsvColor? {
    val centerX: Double = size.width / 2.0
    val centerY: Double = size.height / 2.0
    val radius: Double = min(centerX, centerY)
    val xOffset: Double = position.x - centerX
    val yOffset: Double = position.y - centerY
    val centerOffset = hypot(xOffset, yOffset)
    val rawAngle = atan2(yOffset, xOffset).toDegree()
    val centerAngle = (rawAngle + 360.0) % 360.0
    return if (centerOffset <= radius) {
        HsvColor(
            hue = centerAngle.toFloat(),
            saturation = (centerOffset / radius).toFloat(),
            value = value,
        )
    } else {
        null
    }
}

@Composable
private fun ColorWheel(
    hsvColor: HsvColor,
    diameter: Int
) {
    val saturation = 1.0f
    val value = hsvColor.value

    val radius = diameter / 2f
    val colorSweepGradientBrush = remember(hsvColor.value, diameter) {
        val wheelColors = arrayOf(
            HsvColor(0f, saturation, value),
            HsvColor(60f, saturation, value),
            HsvColor(120f, saturation, value),
            HsvColor(180f, saturation, value),
            HsvColor(240f, saturation, value),
            HsvColor(300f, saturation, value),
            HsvColor(360f, saturation, value)
        ).map {
            it.toColor()
        }
        Brush.sweepGradient(wheelColors, Offset(radius, radius))
    }
    val saturationGradientBrush = remember(diameter) {
        Brush.radialGradient(
            listOf(Color.White, Color.Transparent),
            Offset(radius, radius),
            radius,
            TileMode.Clamp
        )
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        // draw the hue bar
        drawCircle(colorSweepGradientBrush)
        // draw saturation radial overlay
        drawCircle(saturationGradientBrush)
        // account for "brightness/value" slider
        drawCircle(
            hsvColor.copy(
                hue = 0f,
                saturation = 0f
            ).toColor(),
            blendMode = BlendMode.Modulate
        )
    }
}

@Composable
private fun HarmonyColorMagnifiers(
    diameterPx: Int,
    hsvColor: HsvColor,
    animateChanges: Boolean,
    currentlyChangingInput: Boolean,
) {
    val size = IntSize(diameterPx, diameterPx)
    val position = remember(hsvColor, size) {
        positionForColor(hsvColor, size)
    }

    val positionAnimated = remember {
        Animatable(position, typeConverter = Offset.VectorConverter)
    }
    LaunchedEffect(hsvColor, size, animateChanges) {
        if (!animateChanges) {
            positionAnimated.snapTo(positionForColor(hsvColor, size))
        } else {
            positionAnimated.animateTo(
                positionForColor(hsvColor, size),
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
            )
        }
    }

    val diameterDp = with(LocalDensity.current) {
        diameterPx.toDp()
    }

    val animatedDiameter = animateDpAsState(
        targetValue = if (!currentlyChangingInput) {
            diameterDp * diameterMainColorDragging
        } else {
            diameterDp * diameterMainColor
        }
    )

    Magnifier(position = positionAnimated.value, color = hsvColor, diameter = animatedDiameter.value)
}

@Composable
private fun Magnifier(position: Offset, color: HsvColor, diameter: Dp) {
    val offset = with(LocalDensity.current) {
        Modifier.offset(
            position.x.toDp() - diameter / 2,
            // Align with the center of the selection circle
            position.y.toDp() - diameter / 2
        )
    }

    Column(offset.size(width = diameter, height = diameter)) {
        MagnifierSelectionCircle(Modifier.size(diameter), color)
    }
}

@Composable
private fun MagnifierSelectionCircle(modifier: Modifier, color: HsvColor) {
    Surface(
        modifier,
        shape = CircleShape,
        elevation = 4.dp,
        color = color.toColor(),
        border = BorderStroke(2.dp, SolidColor(Color.White)),
        content = {}
    )
}

private fun positionForColor(color: HsvColor, size: IntSize): Offset {
    val radians = color.hue.toRadian()
    val phi = color.saturation
    val x: Float = ((phi * cos(radians)) + 1) / 2f
    val y: Float = ((phi * sin(radians)) + 1) / 2f
    return Offset(
        x = (x * size.width),
        y = (y * size.height)
    )
}

private fun Float.toRadian(): Float = this / 180.0f * PI.toFloat()
private fun Double.toDegree(): Double = this * 180 / PI

private const val diameterMainColorDragging = 0.18f
private const val diameterMainColor = 0.15f
