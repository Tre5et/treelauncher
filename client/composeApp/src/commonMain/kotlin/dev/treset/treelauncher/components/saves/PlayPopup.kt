package dev.treset.treelauncher.components.saves

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.manifest.InstanceComponent
import dev.treset.treelauncher.backend.data.manifest.SavesComponent
import dev.treset.treelauncher.backend.util.QuickPlayData
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Strings
import java.io.IOException

@Composable
fun PlayPopup(
    component: SavesComponent,
    quickPlayData: QuickPlayData,
    onClose: () -> Unit,
    onConfirm: (QuickPlayData, InstanceComponent) -> Unit
) {
    var instances: List<InstanceComponent> by remember(component) { mutableStateOf(listOf()) }

    LaunchedEffect(component) {
        try {
            AppContext.files.reload()
        } catch (e: IOException) {
            AppContext.severeError(e)
        }

        instances = AppContext.files.instanceComponents
            .filter {
                it.savesId.value == component.id.value
            }
    }

    if (instances.isEmpty()) {
        PopupOverlay(
            type = PopupType.ERROR,
            titleRow = { Text(Strings.selector.saves.play.noTitle()) },
            content = { Text(Strings.selector.saves.play.noMessage()) },
            buttonRow = {
                Button(
                    onClick = onClose
                ) {
                    Text(Strings.selector.saves.play.noClose())
                }
            }
        )
    } else if (instances.size > 1) {
        var selectedInstance by remember { mutableStateOf(instances[0]) }

        PopupOverlay(
            type = PopupType.NONE,
            titleRow = { Text(Strings.selector.saves.play.multipleTitle()) },
            content = {
                Text(Strings.selector.saves.play.multipleMessage())
                ComboBox(
                    items = instances,
                    selected = selectedInstance,
                    onSelected = { selectedInstance = it },
                    toDisplayString = { name.value }
                )
            },
            buttonRow = {
                Button(
                    onClick = onClose,
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(Strings.selector.saves.play.multipleClose())
                }
                Button(
                    onClick = { onConfirm(quickPlayData, selectedInstance) },
                ) {
                    Text(Strings.selector.saves.play.multiplePlay())
                }
            }
        )
    } else {
        onConfirm(quickPlayData, instances[0])
    }
}