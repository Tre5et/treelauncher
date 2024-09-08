package net.treset.treelauncher.generic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import net.treset.mcdl.fabric.FabricProfile
import net.treset.mcdl.fabric.FabricVersion
import net.treset.mcdl.forge.ForgeVersion
import net.treset.mcdl.minecraft.MinecraftVersion
import net.treset.mcdl.minecraft.MinecraftVersionDetails
import net.treset.mcdl.quiltmc.QuiltProfile
import net.treset.mcdl.quiltmc.QuiltVersion
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.*
import net.treset.treelauncher.backend.util.file.LauncherFile
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

class VersionState(
    val minecraftVersion: MinecraftVersion?,
    val versionType: VersionType,
    val fabricVersion: FabricVersion?,
    val forgeVersion: String?,
    val quiltVersion: QuiltVersion?
) {
    fun isValid(): Boolean = when(versionType) {
        VersionType.VANILLA -> minecraftVersion != null
        VersionType.FABRIC -> minecraftVersion != null && fabricVersion != null
        VersionType.FORGE -> minecraftVersion != null && forgeVersion != null
        VersionType.QUILT -> quiltVersion != null
    }
}

@Composable
fun VersionSelector(
    onDone: (VersionCreator) -> Unit = {},
    defaultVersionId: String? = null,
    defaultVersionType: VersionType = VersionType.VANILLA,
    defaultLoaderVersion: String? = null,
    showChange: Boolean = true,
    setCurrentState: (VersionState) -> Unit = {_->}
) {
    var showSnapshots by remember { mutableStateOf(defaultVersionId?.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+")) == false) }
    var minecraftVersions: List<MinecraftVersion> by remember(showSnapshots) { mutableStateOf(emptyList()) }
    var minecraftVersion: MinecraftVersion? by remember { mutableStateOf(null) }
    var versionType: VersionType by remember { mutableStateOf(defaultVersionType) }
    var fabricVersions: List<FabricVersion> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var fabricVersion: FabricVersion? by remember { mutableStateOf(null) }
    var forgeVersions: List<String> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var forgeVersion: String? by remember { mutableStateOf(null) }
    var quiltVersions: List<QuiltVersion> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var quiltVersion: QuiltVersion? by remember { mutableStateOf(null) }

    val currentState = remember(minecraftVersion, versionType, fabricVersion, forgeVersion, quiltVersion) {
        VersionState(minecraftVersion, versionType, fabricVersion, forgeVersion, quiltVersion)
            .also(setCurrentState)
    }

    LaunchedEffect(showSnapshots) {
        Thread {
            minecraftVersions = if (showSnapshots) {
                MinecraftVersion.getAll()
            } else {
                MinecraftVersion.getAll().filter { it.isRelease }
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
                fabricVersions = FabricVersion.getAll(mcVersion.id)
                    .also { versions ->
                        defaultLoaderVersion?.let { default ->
                            fabricVersion = prevFabric?.let { current ->
                                versions.firstOrNull { it.loader.version == current.loader.version }
                            } ?: versions.firstOrNull { it.loader.version == default }
                        }
                    }
            }.start()

            Thread {
                quiltVersions = QuiltVersion.getAll(mcVersion.id)
                    .also { versions ->
                        defaultLoaderVersion?.let { default ->
                            quiltVersion = quiltVersion?.let { current ->
                                versions.firstOrNull { it.loader.version == current.loader.version }
                            } ?: versions.firstOrNull { it.loader.version == default }
                        }
                    }
            }.start()

            Thread {
                forgeVersions = ForgeVersion.getAll(mcVersion.id)
                    .also { versions ->
                        defaultLoaderVersion?.let { default ->
                            forgeVersion = forgeVersion?.let { current ->
                                versions.firstOrNull { it == current }
                            } ?: versions.firstOrNull { it == default }
                        }
                    }
            }
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

            if(versionType == VersionType.QUILT) {
                TitledComboBox(
                    title = strings().creator.version.quilt(),
                    items = quiltVersions,
                    selected = quiltVersion,
                    onSelected = { quiltVersion = it },
                    loading = quiltVersions.isEmpty(),
                    placeholder = strings().creator.version.quilt(),
                    loadingPlaceholder = strings().creator.version.loading(),
                    toDisplayString = { loader.version }
                )
            }
        }

        if(showChange) {
            IconButton(
                onClick = {
                    getVersionCreator(
                        currentState
                    )?.let {
                        onDone(it)
                    }
                },
                icon = icons().change,
                enabled = currentState.isValid()
                        && (
                            minecraftVersions.isNotEmpty() && minecraftVersion?.let { it.id != defaultVersionId } ?: false
                            || versionType != defaultVersionType
                            || versionType == VersionType.FABRIC && fabricVersion?.let { it.loader.version != defaultLoaderVersion } ?: false
                            || versionType == VersionType.FORGE && forgeVersion != defaultLoaderVersion
                            || versionType == VersionType.QUILT && quiltVersion?.let { it.loader.version != defaultLoaderVersion } ?: false
                        ),
                tooltip = strings().changer.apply()
            )
        }
    }
}

fun getVersionCreator(
    versionState: VersionState
): VersionCreator? {
    versionState.minecraftVersion?.let { mcVersion ->
        when(versionState.versionType) {
            VersionType.VANILLA -> {
                return VanillaVersionCreator(
                    AppContext.files.launcherDetails.typeConversion,
                    AppContext.files.versionManifest,
                    MinecraftVersionDetails.get(mcVersion.url),
                    AppContext.files,
                    LauncherFile.ofData(AppContext.files.launcherDetails.librariesDir)
                )
            }
            VersionType.FABRIC -> {
                versionState.fabricVersion?.let {
                    return FabricVersionCreator(
                        AppContext.files.launcherDetails.typeConversion,
                        AppContext.files.versionManifest,
                        it,
                        FabricProfile.get(mcVersion.id, it.loader.version),
                        AppContext.files,
                        LauncherFile.ofData(AppContext.files.launcherDetails.librariesDir)
                    )
                }
            }
            VersionType.FORGE -> {
                versionState.forgeVersion?.let { forgeVersion ->
                    return ForgeVersionCreator(
                        AppContext.files.launcherDetails.typeConversion,
                        AppContext.files.versionManifest,
                        forgeVersion,
                        AppContext.files,
                        LauncherFile.ofData(AppContext.files.launcherDetails.librariesDir)
                    )
                }
            }
            VersionType.QUILT -> {
                versionState.quiltVersion?.let {
                    return QuiltVersionCreator(
                        AppContext.files.launcherDetails.typeConversion,
                        AppContext.files.versionManifest,
                        it,
                        QuiltProfile.get(mcVersion.id, it.loader.version),
                        AppContext.files,
                        LauncherFile.ofData(AppContext.files.launcherDetails.librariesDir)
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
    FORGE("forge", { strings().version.forge() }),
    QUILT("quilt", { strings().version.quilt() });

    override fun toString(): String {
        return displayName()
    }

    companion object {
        fun fromId(id: String): VersionType {
            return entries.firstOrNull { it.id == id } ?: VANILLA
        }

        fun fromIds(ids: List<String>): List<VersionType> {
            return ids.map { fromId(it) }
        }
    }
}