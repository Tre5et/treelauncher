package dev.treset.treelauncher.generic

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipProvider(
    tooltip: String,
    interactionSource: MutableInteractionSource? = null,
    delay: Long = 1000,
    content: @Composable () -> Unit
) {
    val actualInteractionSource = remember(interactionSource) {
        interactionSource ?: MutableInteractionSource()
    }

    val modifier = remember(interactionSource) {
        interactionSource?.let {
            Modifier
        } ?: run {
            Modifier
                .hoverable(actualInteractionSource)
                .focusable(true, actualInteractionSource)
        }
    }

    val pressed by actualInteractionSource.collectIsPressedAsState()
    val hovered by actualInteractionSource.collectIsHoveredAsState()
    val nativeFocused by actualInteractionSource.collectIsFocusedAsState()
    //Prevent focus on click
    val focused by remember(nativeFocused) { mutableStateOf(if(pressed) false else nativeFocused) }

    val tooltipState = rememberTooltipState(
        isPersistent = true
    )

    LaunchedEffect(focused) {
        if(focused) {
            tooltipState.show(MutatePriority.UserInput)
        } else {
            tooltipState.dismiss()
        }
    }

    var job: Job? = remember { null }
    LaunchedEffect(hovered) {
        if(hovered) {
            job = launch {
                delay(delay)
                tooltipState.show(MutatePriority.UserInput)
            }
        } else {
            job?.cancel()
            tooltipState.dismiss()
        }
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltip)
            }
        },
        state = tooltipState,
        enableUserInput = false,
        modifier = modifier
    ) {
        content()
    }
}