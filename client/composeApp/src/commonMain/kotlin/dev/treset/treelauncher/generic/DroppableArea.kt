package dev.treset.treelauncher.generic

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlin.reflect.KClass


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FilesListDroppableArea(
    onDrop: (DragData.FilesList) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) = DroppableArea(
    {
        if(it is DragData.FilesList) {
            onDrop(it)
            true
        } else {
            false
        }
    },
    {
        it is DragData.FilesList
    },
    modifier,
    content
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DroppableArea(
    onDrop: (DragData) -> Boolean,
    allowDrop: (DragData) -> Boolean = { true },
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDragging = remember { mutableStateOf(false) }

    val dragAndDropTarget = rememberDragAndDropTarget(
        onDrop = onDrop,
        dragging = isDragging
    )

    Box(
        modifier = modifier
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    allowDrop(event.dragData())
                },
                target = dragAndDropTarget
            )
    ) {
        content()

        if(isDragging.value) {
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberDragAndDropTarget(
    onDrop: (DragData) -> Boolean,
    dragging: MutableState<Boolean>
) : DragAndDropTarget {
    return remember(onDrop) {
        object: DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                dragging.value = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                dragging.value = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                return onDrop(event.dragData())
            }
        }
    }
}

enum class DragType {
    FILES,
    TEXT,
    IMAGE,
    NONE;

    companion object {
        @OptIn(ExperimentalComposeUiApi::class)
        fun fromDragData(data: DragData?): DragType {
            return when(data) {
                is DragData.FilesList -> FILES
                is DragData.Text -> TEXT
                is DragData.Image -> IMAGE
                else -> NONE
            }
        }

        @OptIn(ExperimentalComposeUiApi::class)
        fun fromClass(clazz: KClass<out DragData>): DragType {
            return when(clazz) {
                DragData.FilesList::class -> FILES
                DragData.Text::class -> TEXT
                DragData.Image::class -> IMAGE
                else -> NONE
            }
        }
    }
}