package dev.treset.treelauncher.instances

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.util.sort.InstanceSortType
import dev.treset.treelauncher.backend.util.sort.sorted
import dev.treset.treelauncher.backend.util.toggle
import dev.treset.treelauncher.generic.SortBox
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TitledColumn
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun Instances() {
    var selectedInstance: InstanceData? by remember { mutableStateOf(null) }
    val instances = AppContext.files.instanceComponents.toList()

    var loading by remember { mutableStateOf(true) }

    val reloadInstances = {
        try {
            AppContext.files.reload()
        } catch (e: IOException) {
            AppContext.severeError(e)
        }
        if(!AppContext.files.instanceComponents.contains(selectedInstance?.instance)) {
            selectedInstance = null
        }
        loading = false
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                AppContext.files.instanceManifest.write()
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
        }
    }

    LaunchedEffect(AppContext.files.instanceManifest.components.toList()) {
        reloadInstances()
    }

    val actualInstances = remember(instances, AppContext.files.instanceManifest.sort.type.value, AppContext.files.instanceManifest.sort.reverse.value) {
        instances.sorted(AppContext.files.instanceManifest.sort)
    }

    if(actualInstances.isEmpty() && !loading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                Strings.selector.instance.emptyTitle(),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Strings.selector.instance.empty().let {
                    Text(it.first)
                    Icon(
                        icons().add,
                        "Add",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(it.second)
                }
            }
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {

        TitledColumn(
            modifier = Modifier.padding(12.dp),
            parentModifier = Modifier.fillMaxWidth(1/3f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            headerContent = {
                Text(Strings.selector.instance.title())
                SortBox(
                    sorts = InstanceSortType.entries.map { it.comparator },
                    selected = AppContext.files.instanceManifest.sort.type.value,
                    reversed = AppContext.files.instanceManifest.sort.reverse.value,
                    onSelected = {
                        AppContext.files.instanceManifest.sort.type.value = it
                    },
                    onReversed = {
                        AppContext.files.instanceManifest.sort.reverse.toggle()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        ) {
            actualInstances.forEach {
                InstanceDataProvider(it) {
                    InstanceButton(
                        instance = it,
                        selected = selectedInstance == it,
                        enabled = AppContext.runningInstance?.instance?.id != it.instance.id,
                        onClick = { selectedInstance = if (selectedInstance == it) null else it }
                    )
                }
            }
        }

        selectedInstance?.let {
            InstanceDetails(
                it,
                reloadInstances
            ) {
                selectedInstance = null
            }
        }
    }
}