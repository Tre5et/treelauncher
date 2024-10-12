package net.treset.treelauncher.generic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.fabric.FabricProfile
import dev.treset.mcdl.fabric.FabricVersion
import dev.treset.mcdl.forge.ForgeInstaller
import dev.treset.mcdl.forge.ForgeVersion
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.mcdl.minecraft.MinecraftVersionDetails
import dev.treset.mcdl.quiltmc.QuiltProfile
import dev.treset.mcdl.quiltmc.QuiltVersion
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.backend.creation.*
import net.treset.treelauncher.backend.data.manifest.VersionComponent
import net.treset.treelauncher.backend.util.Status
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons
import java.io.IOException

class VersionCreationContent(
    val versionType: VersionType,
    val minecraftVersion: MinecraftVersion?,
    val fabricVersion: FabricVersion?,
    val forgeVersion: String?,
    val quiltVersion: QuiltVersion?
) {
    fun isValid(): Boolean {
        return when(versionType) {
            VersionType.VANILLA -> minecraftVersion != null
            VersionType.FABRIC -> minecraftVersion != null && fabricVersion != null
            VersionType.FORGE -> minecraftVersion != null && forgeVersion != null
            VersionType.QUILT -> minecraftVersion != null && quiltVersion != null
        }
    }
}

@Composable
fun VersionSelector(
    defaultVersionId: String? = null,
    defaultVersionType: VersionType = VersionType.VANILLA,
    defaultLoaderVersion: String? = null,
    showChange: Boolean = true,
    getCreator: (data: VersionCreationContent, onStatus: (Status) -> Unit) -> VersionCreator<*> = {d,s -> VersionCreator.get(d,s) },
    setExecute: (((onStatus: (Status) -> Unit) -> VersionComponent)?) -> Unit = {},
    setContent: (VersionCreationContent) -> Unit = {},
    onChange: (execute: () -> Unit) -> Unit = { it() },
    onDone: (VersionComponent) -> Unit = {}
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

    var creationStatus: Status? by remember { mutableStateOf(null) }

    val creationContent: VersionCreationContent = remember(minecraftVersion, versionType, fabricVersion, forgeVersion, quiltVersion) {
        VersionCreationContent(
            versionType = versionType,
            minecraftVersion = minecraftVersion,
            fabricVersion = fabricVersion,
            forgeVersion = forgeVersion,
            quiltVersion = quiltVersion
        ).also {
            setContent(it)
        }
    }

    val execute: (onStatus: (Status) -> Unit) -> VersionComponent = remember(minecraftVersion, versionType, fabricVersion, forgeVersion, quiltVersion) {
        @Throws(IOException::class) { onStatus ->
            if(!creationContent.isValid()) {
                throw IOException("Invalid version creation content")
            }

            val creator = getCreator(
                creationContent,
                onStatus
            )

            try {
                val component = creator.create()
                onDone(component)
                component
            } catch (e: IOException) {
                throw IOException("Unable to create version component", e)
            }
        }
    }

    val valid = remember(minecraftVersion, versionType, fabricVersion, forgeVersion, quiltVersion) {
        when(versionType) {
            VersionType.VANILLA -> minecraftVersion != null
            VersionType.FABRIC -> minecraftVersion != null && fabricVersion != null
            VersionType.FORGE -> minecraftVersion != null && forgeVersion != null
            VersionType.QUILT -> minecraftVersion != null && quiltVersion != null
        }.also { setExecute(if(it) execute else null) }
    }

    LaunchedEffect(showSnapshots) {
        Thread {
            try {
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
            } catch (e: IOException) {
                AppContext.errorIfOnline(e)
            }
        }.start()
    }

    LaunchedEffect(minecraftVersion) {
        val prevFabric = fabricVersion
        fabricVersion = null
        minecraftVersion?.also { mcVersion ->
            Thread {
                try {
                fabricVersions = FabricVersion.getAll(mcVersion.id)
                    .also { versions ->
                        defaultLoaderVersion?.let { default ->
                            fabricVersion = prevFabric?.let { current ->
                                versions.firstOrNull { it.loader.version == current.loader.version }
                            } ?: versions.firstOrNull { it.loader.version == default }
                        }
                    }
                } catch (_: IOException) { }
            }.start()

            Thread {
                try {
                quiltVersions = QuiltVersion.getAll(mcVersion.id)
                    .also { versions ->
                        defaultLoaderVersion?.let { default ->
                            quiltVersion = quiltVersion?.let { current ->
                                versions.firstOrNull { it.loader.version == current.loader.version }
                            } ?: versions.firstOrNull { it.loader.version == default }
                        }
                    }
                } catch (_: IOException) { }
            }.start()

            Thread {
                try {
                forgeVersions = ForgeVersion.getAll(mcVersion.id)
                    .also { versions ->
                        defaultLoaderVersion?.let { default ->
                            forgeVersion = forgeVersion?.let { current ->
                                versions.firstOrNull { it == current }
                            } ?: versions.firstOrNull { it == default }
                        }
                    }
                } catch (_: IOException) { }
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
                    if(valid) {
                        Thread {
                            try {
                                onChange {
                                    execute {
                                        creationStatus = it
                                    }
                                }
                            } catch (e: IOException) {
                                AppContext.error(e)
                            }
                        }
                    }
                },
                icon = icons().change,
                enabled = valid
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

        creationStatus?.let {
            StatusPopup(it)
        }
    }
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

@Throws(IOException::class)
fun VersionCreator.Companion.get(data: VersionCreationContent, onStatus: (Status) -> Unit): VersionCreator<out VersionCreationData> {
    when(data.versionType) {
        VersionType.VANILLA -> return VanillaVersionCreator.get(data, onStatus)
        VersionType.FABRIC -> return FabricVersionCreator.get(data, onStatus)
        VersionType.FORGE -> return ForgeVersionCreator.get(data, onStatus)
        VersionType.QUILT -> return QuiltVersionCreator.get(data, onStatus)
    }
}

@Throws(IOException::class)
fun VanillaVersionCreator.Companion.get(data: VersionCreationContent, onStatus: (Status) -> Unit): VersionCreator<out VersionCreationData> {
    return VanillaVersionCreator(
        VanillaCreationData(MinecraftVersionDetails.get(data.minecraftVersion!!.url), AppContext.files),
        onStatus
    )
}

@Throws(IOException::class)
fun FabricVersionCreator.Companion.get(data: VersionCreationContent, onStatus: (Status) -> Unit): VersionCreator<out VersionCreationData> {
    return FabricVersionCreator(
        FabricCreationData(
            version = data.fabricVersion!!,
            profile = FabricProfile.get(data.minecraftVersion!!.id, data.fabricVersion.loader.version),
            files = AppContext.files
        ),
        onStatus
    )
}

@Throws(IOException::class)
fun ForgeVersionCreator.Companion.get(data: VersionCreationContent, onStatus: (Status) -> Unit): VersionCreator<out VersionCreationData> {
    return ForgeVersionCreator(
        ForgeCreationData(
            installer = ForgeInstaller.getForVersion(data.forgeVersion!!),
            files = AppContext.files
        ),
        onStatus
    )
}

@Throws(IOException::class)
fun QuiltVersionCreator.Companion.get(data: VersionCreationContent, onStatus: (Status) -> Unit): VersionCreator<out VersionCreationData> {
    return QuiltVersionCreator(
        QuiltCreationData(
            version = data.quiltVersion!!,
            profile = QuiltProfile.get(data.minecraftVersion!!.id, data.quiltVersion.loader.version),
            files = AppContext.files
        ),
        onStatus
    )
}