package net.treset.treelauncher.util

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.data.DataUpgrader
import net.treset.treelauncher.localization.strings
import java.io.IOException

@Composable
fun DataUpgrade(
    content: @Composable () -> Unit
) {
    var upgraded by rememberSaveable{ mutableStateOf(false) }
    var error by remember { mutableStateOf<Exception?>(null) }

    LaunchedEffect(Unit) {
        if(!upgraded) {
            try {
                DataUpgrader(
                    strings().launcher.version(),
                    appSettings().version,
                ).performUpgrade { _ -> }
                AppContext.files.reloadAll()
            } catch (e: Exception) {
                error = IOException("Failed to upgrade launcher data", e)
            }
            upgraded = true
        }
    }

    if(error != null) {
        AppContext.severeError(error!!)
    } else if(upgraded) {
        content()
    }
}