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
import net.treset.treelauncher.login.LoginContext

@Composable
fun Instances(
    appContext: AppContext,
    loginContext: LoginContext
) {
    var selectedInstance: InstanceData? by remember { mutableStateOf(null) }
    var instances: List<InstanceData> by remember { mutableStateOf(emptyList()) }
    var selectedSort: InstanceDataSortType by remember { mutableStateOf(appSettings().instanceSortType) }
    var sortReversed: Boolean by remember { mutableStateOf(appSettings().isInstanceSortReverse) }

    val reloadInstances = {
        appContext.files.reloadAll()
        selectedInstance = null
        instances = appContext.files.instanceComponents
            .map { InstanceData.of(it, appContext.files) }
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

    LaunchedEffect(instances, selectedSort, sortReversed) {
        val newInst = instances
            .sortedWith(selectedSort.comparator)
        instances = if(sortReversed) newInst.reversed() else newInst
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
                    onClick = { selectedInstance = if(selectedInstance == it) null else it }
                )
            }
        }

        selectedInstance?.let {
            InstanceDetails(
                it,
                redrawSelected,
                reloadInstances,
                appContext,
                loginContext,
            )
        }
    }
}