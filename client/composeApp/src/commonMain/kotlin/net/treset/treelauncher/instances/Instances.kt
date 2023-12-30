package net.treset.treelauncher.instances

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.localization.strings

@Composable
fun Instances(
    appContext: AppContext
) {
    var selectedInstance: InstanceData? by remember { mutableStateOf(null) }
    var instances: List<InstanceData> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(Unit) {
        appContext.files.reloadAll()
        selectedInstance = null
        instances = appContext.files.instanceComponents
            .map { InstanceData.of(it, appContext.files) }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {

        TitledColumn(
            title = strings().nav.home(),
            modifier = Modifier.fillMaxWidth(1/3f)
        ) {
            instances.forEach {
                InstanceButton(
                    instance = it,
                    selected = selectedInstance == it,
                    onClick = {
                        selectedInstance = it
                    }
                )
            }
        }
    }
}