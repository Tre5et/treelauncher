package dev.treset.treelauncher.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DroppableArea(
    onDrop: (DragData) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onExternalDrag(
                onDragStart = {
                    isDragging = true
                },
                onDragExit = {
                    isDragging = false
                },
                onDrop = {
                    isDragging = false
                    val dragData = it.dragData
                    onDrop(dragData)
                }
            )
    ) {
        content()

        if(isDragging) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = LocalContentColor.current.copy(alpha = 0.5f))
                    .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            )
        }
    }
}