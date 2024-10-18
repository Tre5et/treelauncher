package dev.treset.treelauncher.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.backend.config.AppSettings
import io.github.oshai.kotlinlogging.KotlinLogging
import dev.treset.treelauncher.backend.update.UpdateService
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun UpdateUrl() {
    var tfValue by remember(AppSettings.updateUrl.value) { mutableStateOf(AppSettings.updateUrl.value) }

    var popupContent: PopupData? by remember { mutableStateOf(null) }

    if(AppSettings.isDebug) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp)
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(
                    Strings.settings.updateUrl.title(),
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
                        placeholder = Strings.settings.updateUrl.title()
                    )

                    IconButton(
                        onClick = {
                            try {
                                UpdateService(tfValue).update()
                                LOGGER.debug { "Updating URL to: $tfValue" }
                                AppSettings.updateUrl.value = tfValue
                            } catch (e: IOException) {
                                LOGGER.debug(e) { "Unable to update URL" }
                                popupContent = PopupData(
                                    type = PopupType.ERROR,
                                    titleRow = { Text(Strings.settings.updateUrl.popupTitle()) },
                                    content = { Text(Strings.settings.updateUrl.popupMessage(e)) },
                                    buttonRow = {
                                        Button(
                                            onClick = { popupContent = null }
                                        ) {
                                            Text(Strings.settings.updateUrl.popupClose())
                                        }
                                    }
                                )
                            }
                        },
                        icon = icons().change,
                        tooltip = Strings.settings.updateUrl.apply(),
                        enabled = tfValue != AppSettings.updateUrl.value
                    )
                }
            }
        }
    }

    popupContent?.let {
        PopupOverlay(it)
    }
}

private val LOGGER = KotlinLogging.logger {}