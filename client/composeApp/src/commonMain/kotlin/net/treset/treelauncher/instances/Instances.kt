package net.treset.treelauncher.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.config.InstanceDataSortType
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.data.InstanceData
import net.treset.treelauncher.generic.SortBox
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.localization.strings

@Composable
fun Instances(
    appContext: AppContext
) {
    var selectedInstance: InstanceData? by remember { mutableStateOf(null) }
    var instances: List<InstanceData> by remember { mutableStateOf(emptyList()) }
    var selectedSort: InstanceDataSortType by remember { mutableStateOf(appSettings().instanceSortType) }
    var sortReversed: Boolean by remember { mutableStateOf(appSettings().isInstanceSortReverse) }

    LaunchedEffect(Unit) {
        appContext.files.reloadAll()
        selectedInstance = null
        instances = appContext.files.instanceComponents
            .map { InstanceData.of(it, appContext.files) }
    }

    LaunchedEffect(selectedSort, sortReversed) {
        val newInst = instances
            .sortedWith(selectedSort.comparator)
        instances = if(sortReversed) newInst.reversed() else newInst
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {

        TitledColumn(
            modifier = Modifier.padding(12.dp),
            parentModifier = Modifier .fillMaxWidth(1/3f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            headerContent = {
                Text(strings().nav.home())
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
                    onClick = { selectedInstance = if(selectedInstance == it) null else it }
                )
            }
        }



        TitledColumn(
            title = selectedInstance?.instance?.first?.name ?: strings().manager.instance.details.title(),
            modifier = Modifier.padding(12.dp),
            parentModifier = Modifier.fillMaxWidth(1/2f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            selectedInstance?.let {
                InstanceDetails(it)
            }
        }
    }
}

enum class InstanceDetails {
    VERSION,
    SAVES,
    RESOURCE_PACKS,
    OPTIONS,
    MODS,
    SETTINGS
}