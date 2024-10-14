package dev.treset.treelauncher.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.treelauncher.backend.config.appSettings
import dev.treset.treelauncher.backend.update.UpdateService
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun UpdateUrl() {
    var currentUrl by remember { mutableStateOf(appSettings().updateUrl) }
    var tfValue by remember(currentUrl) { mutableStateOf(currentUrl) }

    var popupContent: PopupData? by remember { mutableStateOf(null) }

    if(appSettings().isDebug) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp)
        ) {

            Text(
                strings().settings.updateUrl.title(),
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextBox(
                    tfValue,
                    {
                        tfValue = it
                    },
                    placeholder = strings().settings.updateUrl.title()
                )

                IconButton(
                    onClick = {
                        try {
                            UpdateService(tfValue).update()
                            LOGGER.debug { "Updating URL to: $tfValue" }
                            appSettings().updateUrl = tfValue
                            currentUrl = tfValue
                        } catch (e: IOException) {
                            LOGGER.debug(e) { "Unable to update URL"}
                            popupContent = PopupData(
                                type = PopupType.ERROR,
                                titleRow = { Text(strings().settings.updateUrl.popupTitle()) },
                                content = { Text(strings().settings.updateUrl.popupMessage(e)) },
                                buttonRow = {
                                    Button(
                                        onClick = { popupContent = null }
                                    ) {
                                        Text(strings().settings.updateUrl.popupClose())
                                    }
                                }
                            )
                        }
                    },
                    icon = icons().change,
                    tooltip = strings().settings.updateUrl.apply(),
                    enabled = tfValue != currentUrl
                )
            }
        }
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}

private val LOGGER = KotlinLogging.logger {}