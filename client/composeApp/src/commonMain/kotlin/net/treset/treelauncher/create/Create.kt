package net.treset.treelauncher.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherModsDetails
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.VersionCreator
import net.treset.treelauncher.creation.ComponentCreator
import net.treset.treelauncher.creation.CreationMode
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.TextBox
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.generic.VersionSelector
import net.treset.treelauncher.localization.strings

@Composable
fun Create(
    appContext: AppContext
) {
    var instanceName by remember { mutableStateOf("") }

    var getVersion: (() -> VersionCreator?) = remember { { null } }
    var getSaves: (() -> Triple<CreationMode, String?, LauncherManifest?>) = remember { { Triple(CreationMode.NEW, null, null) } }
    var getResourcepacks: (() -> Triple<CreationMode, String?, LauncherManifest?>) = remember { { Triple(CreationMode.NEW, null, null) } }
    var getOptions: (() -> Triple<CreationMode, String?, LauncherManifest?>) = remember { { Triple(CreationMode.NEW, null, null) } }
    var getMods: (() -> Triple<CreationMode, String?, Pair<LauncherManifest, LauncherModsDetails>?>) = remember { { Triple(CreationMode.NEW, null, null) } }

    TitledColumn(
        title = strings().creator.instance.title(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    strings().creator.instance.instance(),
                    style = MaterialTheme.typography.titleSmall
                )
                TextBox(
                    text = instanceName,
                    onChange = { instanceName = it },
                    placeholder = strings().creator.name()
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    strings().creator.instance.version(),
                    style = MaterialTheme.typography.titleSmall
                )
                getVersion = VersionSelector(
                    appContext = appContext,
                    showChange = false
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    strings().creator.instance.saves(),
                    style = MaterialTheme.typography.titleSmall
                )
                getSaves = ComponentCreator(
                    existing = appContext.files.savesComponents.toList(),
                    showCreate = false,
                    toDisplayString = { name },
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    strings().creator.instance.resourcepacks(),
                    style = MaterialTheme.typography.titleSmall
                )
                getResourcepacks = ComponentCreator(
                    existing = appContext.files.resourcepackComponents.toList(),
                    showCreate = false,
                    toDisplayString = { name },
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    strings().creator.instance.options(),
                    style = MaterialTheme.typography.titleSmall
                )
                getOptions = ComponentCreator(
                    existing = appContext.files.optionsComponents.toList(),
                    showCreate = false,
                    toDisplayString = { name },
                )
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(
                    strings().creator.instance.mods(),
                    style = MaterialTheme.typography.titleSmall
                )
                getMods = ComponentCreator(
                    existing = appContext.files.modsComponents.toList(),
                    showCreate = false,
                    toDisplayString = { first.name },
                )
            }
        }

        Button(
            onClick = {}
        ) {
            Text(
                strings().creator.buttonCreate()
            )
        }
    }
}

private fun <T> validateCreationData(
    mode: CreationMode,
    name: String?,
    existing: T?
): Boolean {
    return when(mode) {
        CreationMode.NEW -> name != null
        CreationMode.INHERIT -> name != null && existing != null
        CreationMode.USE -> existing != null
    }
}