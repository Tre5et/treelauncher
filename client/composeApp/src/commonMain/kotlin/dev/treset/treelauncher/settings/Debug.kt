package dev.treset.treelauncher.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.debug.DebugPopup
import dev.treset.treelauncher.generic.Button

@Composable
fun Debug() {
    if(AppSettings.isDebug.value) {
        var showPopup: Boolean by remember { mutableStateOf(false) }

        Button(
            onClick = {
                showPopup = true
            }
        ) {
            Text("Open Debug Popup")
        }

        if(showPopup) {
            DebugPopup { showPopup = false }
        }
    }
}