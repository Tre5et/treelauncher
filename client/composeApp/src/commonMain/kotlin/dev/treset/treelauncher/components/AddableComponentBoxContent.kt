package dev.treset.treelauncher.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.ListDisplayBox
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.ListDisplay

@Composable
fun SharedAddableComponentData<*>.BoxContent() {
    if(showAdd.value) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    showAdd.value = false
                },
                icon = icons().back,
                size = 32.dp,
                tooltip = Strings.manager.component.import.back(),
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            ListDisplayBox(
                ListDisplay.entries,
                component.listDisplay,
                AppContext.files.savesManifest.defaultListDisplay
            )
        }
    }
}