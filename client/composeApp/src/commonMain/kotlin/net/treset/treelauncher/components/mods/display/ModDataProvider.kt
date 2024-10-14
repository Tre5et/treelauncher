package net.treset.treelauncher.components.mods.display

import androidx.compose.runtime.*

@Composable
fun ModDataProvider(
    element: ModDisplay,
    content: @Composable ModDisplayData.() -> Unit
) {
    var displayData by remember { mutableStateOf(element.recomposeData()) }
    LaunchedEffect(element) {
        element.onRecomposeData = { displayData = it }
    }

    displayData.content()
}