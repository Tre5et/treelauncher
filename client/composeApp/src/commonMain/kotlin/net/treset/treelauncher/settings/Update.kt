package net.treset.treelauncher.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.string.openInBrowser
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.PopupData
import net.treset.treelauncher.generic.PopupOverlay
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.util.onUpdate
import java.io.IOException

@Composable
fun Update() {
    val coroutineScope = rememberCoroutineScope()

    var popupContent: PopupData? by remember { mutableStateOf(null) }

    val update = remember {
        try {
            updater().getUpdate()
        } catch(e: IOException) {
            AppContext.error(e)
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().launcher.name(),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                strings().settings.version()
            )
        }


        IconButton(
            onClick = {
                onUpdate(coroutineScope) { popupContent = it }
            },
            icon = icons().update,
            highlighted = update?.id != null,
            tooltip = strings().settings.update.tooltip(),
            enabled = AppContext.runningInstance == null
        )

        if (update?.id != null) {
            Text(
                strings().settings.update.available()
            )
        }

        IconButton(
            onClick = {
                "https://github.com/Tre5et/treelauncher".openInBrowser()
            },
            painter = icons().gitHub,
            tooltip = strings().settings.sourceTooltip()
        )
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}