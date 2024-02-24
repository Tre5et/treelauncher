package net.treset.treelauncher.generic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.fabric.FabricLoader
import net.treset.mc_version_loader.fabric.FabricVersionDetails
import net.treset.mc_version_loader.forge.ForgeMetaVersion
import net.treset.mc_version_loader.forge.MinecraftForge
import net.treset.mc_version_loader.minecraft.MinecraftGame
import net.treset.mc_version_loader.minecraft.MinecraftVersion
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.FabricVersionCreator
import net.treset.treelauncher.backend.creation.ForgeVersionCreator
import net.treset.treelauncher.backend.creation.VanillaVersionCreator
import net.treset.treelauncher.backend.creation.VersionCreator
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

class VersionState(
    val minecraftVersion: MinecraftVersion?,
    val versionType: VersionType,
    val fabricVersion: FabricVersionDetails?,
    val forgeVersion: String?
) {
    fun isValid(): Boolean = when(versionType) {
        VersionType.VANILLA -> minecraftVersion != null
        VersionType.FABRIC -> minecraftVersion != null && fabricVersion != null
        VersionType.FORGE -> minecraftVersion != null && forgeVersion != null
    }
}

@Composable
fun VersionSelector(
    onDone: (VersionCreator) -> Unit = {},
    appContext: AppContext,
    defaultVersionId: String? = null,
    defaultVersionType: VersionType = VersionType.VANILLA,
    defaultFabricVersion: String? = null,
    defaultForgeVersion: String? = null,
    showChange: Boolean = true,
    setCurrentState: (VersionState) -> Unit = {_->}
) {
    var showSnapshots by remember { mutableStateOf(false) }
    var minecraftVersions: List<MinecraftVersion> by remember(showSnapshots) { mutableStateOf(emptyList()) }
    var minecraftVersion: MinecraftVersion? by remember { mutableStateOf(null) }
    var versionType: VersionType by remember { mutableStateOf(defaultVersionType) }
    var fabricVersions: List<FabricVersionDetails> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var fabricVersion: FabricVersionDetails? by remember { mutableStateOf(null) }
    var allForgeVersions: List<ForgeMetaVersion>? by remember { mutableStateOf(null) }
    var forgeVersions: List<String> by remember(minecraftVersion, allForgeVersions) { mutableStateOf(emptyList()) }
    var forgeVersion: String? by remember { mutableStateOf(null) }

    val currentState = remember(minecraftVersion, versionType, fabricVersion, forgeVersion) {
        VersionState(minecraftVersion, versionType, fabricVersion, forgeVersion)
            .also(setCurrentState)
    }

    LaunchedEffect(Unit) {
        Thread {
            allForgeVersions = MinecraftForge.getForgeVersions()
        }.start()
    }

    LaunchedEffect(showSnapshots) {
        Thread {
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
        }.start()
    }

    LaunchedEffect(minecraftVersion) {
        val prevFabric = fabricVersion
        fabricVersion = null
        minecraftVersion?.also { mcVersion ->
            Thread {
                fabricVersions = FabricLoader.getFabricVersions(mcVersion.id)
                    .also { versions ->
                        defaultFabricVersion?.let { default ->
                            fabricVersion = prevFabric?.let { current ->
                                versions.firstOrNull { it.loader.version == current.loader.version }
                            } ?: versions.firstOrNull { it.loader.version == default }
                        }
                    }
            }.start()
        }
    }

    LaunchedEffect(minecraftVersion, allForgeVersions) {
        val prevForge = forgeVersion
        forgeVersion = null
        minecraftVersion?.also { mcVersion ->
            Thread {
                forgeVersions = (allForgeVersions?.firstOrNull { it.name == mcVersion.id }?.versions ?: emptyList<String>())
                    .also { versions ->
                        defaultForgeVersion?.let { default ->
                            forgeVersion = prevForge?.let { current ->
                                versions.firstOrNull { it == current }
                            } ?: versions.firstOrNull { it == default }
                        }
                    }
            }.start()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TitledComboBox(
            title = strings().creator.version.version(),
            items = minecraftVersions,
            loading = minecraftVersions.isEmpty(),
            selected = minecraftVersion,
            onSelected = { minecraftVersion = it },
            placeholder = strings().creator.version.version(),
            allowSearch = true
        )

        TitledCheckBox(
            title = strings().creator.version.showSnapshots(),
            checked = showSnapshots,
            onCheckedChange = { showSnapshots = it }
        )

        TitledComboBox(
            title = strings().creator.version.type(),
            items = VersionType.entries,
            onSelected = { versionType = it },
            selected = versionType,
        )

        minecraftVersion?.let {
            if (versionType == VersionType.FABRIC) {
                    TitledComboBox(
                        title = strings().creator.version.fabric(),
                        items = fabricVersions,
                        selected = fabricVersion,
                        onSelected = { fabricVersion = it },
                        loading = fabricVersions.isEmpty(),
                        placeholder = strings().creator.version.fabric(),
                        loadingPlaceholder = strings().creator.version.loading(),
                    )
            }

            if(versionType == VersionType.FORGE) {
                TitledComboBox(
                    title = strings().creator.version.forge(),
                    items = forgeVersions,
                    selected = forgeVersion,
                    onSelected = { forgeVersion = it },
                    loading = forgeVersions.isEmpty(),
                    placeholder = strings().creator.version.forge(),
                    loadingPlaceholder = strings().creator.version.loading(),
                    toDisplayString = {
                        val parts = this.split("-")
                        if(parts.size > 1) {
                            parts.last()
                        } else {
                            this
                        }
                    }
                )
            }
        }

        if(showChange) {
            IconButton(
                onClick = {
                    getVersionCreator(
                        currentState,
                        appContext
                    )?.let {
                        onDone(it)
                    }               },
                enabled = currentState.isValid()
                        && (
                            minecraftVersions.isNotEmpty() && minecraftVersion?.let { it.id != defaultVersionId } ?: false
                            || versionType != defaultVersionType
                            || versionType == VersionType.FABRIC && fabricVersion?.let { it.loader.version != defaultFabricVersion } ?: false
                            || versionType == VersionType.FORGE && forgeVersion != defaultForgeVersion
                        ),
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
                return VanillaVersionCreator(
                    appContext.files.launcherDetails.typeConversion,
                    appContext.files.versionManifest,
                    MinecraftGame.getVersionDetails(mcVersion.url),
                    appContext.files,
                    LauncherFile.ofData(appContext.files.launcherDetails.librariesDir)
                )
            }
            VersionType.FABRIC -> {
                versionState.fabricVersion?.let {
                    return FabricVersionCreator(
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.versionManifest,
                        it,
                        FabricLoader.getFabricProfile(mcVersion.id, it.loader.version),
                        appContext.files,
                        LauncherFile.ofData(appContext.files.launcherDetails.librariesDir)
                    )
                }
            }
            VersionType.FORGE -> {
                versionState.forgeVersion?.let { forgeVersion ->
                    return ForgeVersionCreator(
                        appContext.files.launcherDetails.typeConversion,
                        appContext.files.versionManifest,
                        forgeVersion,
                        appContext.files,
                        LauncherFile.ofData(appContext.files.launcherDetails.librariesDir)
                    )
                }
            }
        }
    }

    return null
}

enum class VersionType(
    val id: String,
    val displayName: () -> String
) {
    VANILLA("vanilla", { strings().version.vanilla() }),
    FABRIC("fabric", { strings().version.fabric() }),
    FORGE("forge", { strings().version.forge() });

    override fun toString(): String {
        return displayName()
    }

    companion object {
        fun fromId(id: String): VersionType {
            return entries.firstOrNull { it.id == id } ?: VANILLA
        }
    }
}