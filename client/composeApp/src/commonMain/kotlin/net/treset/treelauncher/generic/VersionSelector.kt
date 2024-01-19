package net.treset.treelauncher.generic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.fabric.FabricLoader
import net.treset.mc_version_loader.fabric.FabricVersionDetails
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.VersionCreator
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

class VersionState(
    val minecraftVersion: MinecraftVersion?,
    val versionType: VersionType,
    val fabricVersion: FabricVersionDetails?
) {
    fun isValid(): Boolean = when(versionType) {
        VersionType.VANILLA -> minecraftVersion != null
        VersionType.FABRIC -> minecraftVersion != null && fabricVersion != null
    }
}

@Composable
fun VersionSelector(
    onDone: (VersionCreator) -> Unit = {},
    appContext: AppContext,
    defaultVersionId: String? = null,
    defaultVersionType: VersionType = VersionType.VANILLA,
    defaultFabricVersion: String? = null,
    showChange: Boolean = true,
    setCurrentState: (VersionState) -> Unit = {_->}
) {
    var showSnapshots by remember { mutableStateOf(false) }
    var minecraftVersions: List<MinecraftVersion> by remember(showSnapshots) { mutableStateOf(emptyList()) }
    var minecraftVersion: MinecraftVersion? by remember { mutableStateOf(null) }
    var versionType: VersionType by remember { mutableStateOf(defaultVersionType) }
    var fabricVersions: List<FabricVersionDetails> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var fabricVersion: FabricVersionDetails? by remember { mutableStateOf(null) }

    setCurrentState(VersionState(minecraftVersion, versionType, fabricVersion))

    LaunchedEffect(showSnapshots) {
        minecraftVersions = if (showSnapshots) {
            MinecraftGame.getVersions()
        } else {
            MinecraftGame.getReleases()
        }.also { versions ->
            defaultVersionId?.let { default ->
                minecraftVersion = minecraftVersion?.let { current ->
                    versions.firstOrNull { it.id == current.id }
                } ?: versions.firstOrNull { it.id == default }
            }
        }
    }

    LaunchedEffect(minecraftVersion) {
        minecraftVersion?.also { mcVersion ->
            fabricVersions = FabricLoader.getFabricVersions(mcVersion.id)
                .also { versions ->
                    defaultFabricVersion?.let { default ->
                        fabricVersion = fabricVersion?.let { current ->
                            versions.firstOrNull { it.loader.version == current.loader.version }
                        } ?: versions.firstOrNull { it.loader.version == default }
                    }
                }

        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        showSnapshots.let {
            TitledComboBox(
                title = strings().creator.version.version(),
                items = minecraftVersions,
                loading = minecraftVersions.isEmpty(),
                defaultSelected = minecraftVersion,
                onSelected = { minecraftVersion = it },
                placeholder = strings().creator.version.version()
            )
        }

        TitledCheckBox(
            title = strings().creator.version.showSnapshots(),
            checked = showSnapshots,
            onCheckedChange = { showSnapshots = it }
        )

        TitledComboBox(
            title = strings().creator.version.type(),
            items = VersionType.entries,
            onSelected = { versionType = it },
            defaultSelected = versionType,
        )

        minecraftVersion?.let {
            if (versionType == VersionType.FABRIC) {
                    TitledComboBox(
                        title = strings().creator.version.loader(),
                        items = fabricVersions,
                        defaultSelected = fabricVersion,
                        onSelected = { fabricVersion = it },
                        loading = fabricVersions.isEmpty(),
                        placeholder = strings().creator.version.loader(),
                        loadingPlaceholder = strings().creator.version.loading(),
                    )
            }
        }

        if(showChange) {
            IconButton(
                onClick = {
                    getVersionCreator(
                        VersionState(minecraftVersion, versionType, fabricVersion),
                        appContext
                    )?.let {
                        onDone(it)
                    }               },
                enabled = minecraftVersion != null && (versionType == VersionType.VANILLA || fabricVersion != null),
                tooltip = strings().changer.apply()
            ) {
                Icon(
                    icons().change,
                    "Change Version"
                )
            }
        }
    }
}

fun getVersionCreator(
    versionState: VersionState,
    appContext: AppContext
): VersionCreator? {
    versionState.minecraftVersion?.let { mcVersion ->
        when(versionState.versionType) {
            VersionType.VANILLA -> {
                return VersionCreator(
                    appContext.files.launcherDetails.typeConversion,
                    appContext.files.versionManifest,
                    MinecraftGame.getVersionDetails(mcVersion.url),
                    appContext.files,
                    LauncherFile.ofData(appContext.files.launcherDetails.librariesDir)
                )
            }
            VersionType.FABRIC -> {
                versionState.fabricVersion?.let {
                    return VersionCreator(
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.versionManifest,
                        it,
                        FabricLoader.getFabricProfile(mcVersion.id, it.loader.version),
                        appContext.files,
                        LauncherFile.ofData(appContext.files.launcherDetails.librariesDir)
                    )
                }
            }
        }
    }

    return null
}

enum class VersionType {
    VANILLA,
    FABRIC
}