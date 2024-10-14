package dev.treset.treelauncher.instances

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.InstanceDataSortType
import dev.treset.treelauncher.backend.config.appSettings
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.generic.SortBox
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.generic.TitledColumn
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.style.icons
import java.io.IOException

@Composable
fun Instances() {
    var selectedInstance: InstanceData? by remember { mutableStateOf(null) }
    var instances: List<InstanceData> by remember { mutableStateOf(emptyList()) }
    var selectedSort: InstanceDataSortType by remember { mutableStateOf(appSettings().instanceSortType) }
    var sortReversed: Boolean by remember { mutableStateOf(appSettings().isInstanceSortReverse) }

    var loading by remember { mutableStateOf(true) }

    val reloadInstances = {
        try {
            AppContext.files.reload()
        } catch (e: IOException) {
            AppContext.severeError(e)
        }
        selectedInstance = null
        instances = AppContext.files.instanceComponents
            .mapNotNull {
                try {
                    InstanceData.of(it, AppContext.files)
                } catch (e: FileLoadException) {
                    AppContext.severeError(e)
                    null
                }
            }
        loading = false
    }

    val redrawSelected: () -> Unit = {
        selectedInstance?.let {
            selectedInstance = null
            selectedInstance = it
        }
    }

    LaunchedEffect(Unit) {
        reloadInstances()
    }

    LaunchedEffect(instances, selectedSort, sortReversed, AppContext.runningInstance) {
        val newInst = instances
            .sortedWith(selectedSort.comparator)
        instances = if(sortReversed) newInst.reversed() else newInst
    }

    if(instances.isEmpty() && !loading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().selector.instance.emptyTitle(),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                strings().selector.instance.empty().let {
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
                Text(strings().selector.instance.title())
                SortBox(
                    sorts = InstanceDataSortType.entries,
                    selected = selectedSort,
                    reversed = sortReversed,
                    onSelected = {
                        selectedSort = it
                        appSettings().instanceSortType = it
                    },
                    onReversed = {
                        sortReversed = !sortReversed
                        appSettings().isInstanceSortReverse = sortReversed
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        ) {
            instances.forEach {
                InstanceButton(
                    instance = it,
                    selected = selectedInstance == it,
                    enabled = AppContext.runningInstance?.instance?.id != it.instance.id,
                    onClick = { selectedInstance = if(selectedInstance == it) null else it }
                )
            }
        }

        selectedInstance?.let {
            InstanceDetails(
                it,
                redrawSelected,
                reloadInstances
            ) {
                selectedInstance = null
            }
        }
    }
}