package dev.treset.treelauncher.generic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.mcdl.fabric.FabricProfile
import dev.treset.mcdl.fabric.FabricVersion
import dev.treset.mcdl.forge.ForgeVersion
import dev.treset.mcdl.minecraft.MinecraftProfile
import dev.treset.mcdl.minecraft.MinecraftVersion
import dev.treset.mcdl.neoforge.NeoForgeDL
import dev.treset.mcdl.quiltmc.QuiltProfile
import dev.treset.mcdl.quiltmc.QuiltVersion
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.creation.*
import dev.treset.treelauncher.backend.data.manifest.VersionComponent
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StringProvider
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.disabledContent
import dev.treset.treelauncher.style.icons
import java.io.IOException

class VersionCreationContent(
    val versionType: VersionType,
    val minecraftVersion: MinecraftVersion?,
    val fabricVersion: FabricVersion?,
    val forgeVersion: String?,
    val neoForgeVersion: String?,
    val quiltVersion: QuiltVersion?
) {
    fun isValid(): Boolean {
        return when(versionType) {
            VersionType.VANILLA -> minecraftVersion != null
            VersionType.FABRIC -> minecraftVersion != null && fabricVersion != null
            VersionType.QUILT -> minecraftVersion != null && quiltVersion != null
            VersionType.FORGE -> minecraftVersion != null && forgeVersion != null
            VersionType.NEO_FORGE -> minecraftVersion != null && neoForgeVersion != null
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
    setContent: (VersionCreationContent) -> Unit = {},
    onChange: (execute: () -> VersionComponent) -> Unit = { it() },
    onDone: (VersionComponent) -> Unit = {}
) {
    var showSnapshots by remember { mutableStateOf(defaultVersionId?.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+")) == false) }
    var minecraftVersions: List<MinecraftVersion> by remember(showSnapshots) { mutableStateOf(emptyList()) }
    var minecraftVersion: MinecraftVersion? by remember { mutableStateOf(null) }
    var versionType: VersionType by remember { mutableStateOf(defaultVersionType) }
    var fabricVersions: List<FabricVersion> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var fabricVersion: FabricVersion? by remember { mutableStateOf(null) }
    var quiltVersions: List<QuiltVersion> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var quiltVersion: QuiltVersion? by remember { mutableStateOf(null) }
    var forgeVersions: List<String> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var forgeVersion: String? by remember { mutableStateOf(null) }
    var neoForgeVersions: List<String> by remember(minecraftVersion) { mutableStateOf(emptyList()) }
    var neoForgeVersion: String? by remember { mutableStateOf(null) }

    var creationStatus: Status? by remember { mutableStateOf(null) }

    val creationContent: VersionCreationContent = remember(minecraftVersion, versionType, fabricVersion, forgeVersion, quiltVersion, neoForgeVersion) {
        VersionCreationContent(
            versionType = versionType,
            minecraftVersion = minecraftVersion,
            fabricVersion = fabricVersion,
            quiltVersion = quiltVersion,
            forgeVersion = forgeVersion,
            neoForgeVersion = neoForgeVersion
        ).also {
            setContent(it)
        }
    }

    val execute: (onStatus: (Status) -> Unit) -> VersionComponent = remember(minecraftVersion, versionType, fabricVersion, forgeVersion, quiltVersion) {
        @Throws(IOException::class) { onStatus ->
            if(!creationContent.isValid()) {
                throw IOException("Invalid version creation content")
            }

            onStatus(Status(CreationStep.STARTING, object : StringProvider { override fun get() = "" }))

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

    val valid by derivedStateOf { creationContent.isValid() }

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
        minecraftVersion?.also { mcVersion ->
            Thread {
                try {
                    fabricVersions = FabricVersion.getAll(mcVersion.id)
                        .also { versions ->
                            fabricVersion = defaultLoaderVersion?.let { default ->
                                fabricVersion?.let { current ->
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
                        quiltVersion = defaultLoaderVersion?.let { default ->
                            quiltVersion?.let { current ->
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
                        forgeVersion = defaultLoaderVersion?.let { default ->
                            forgeVersion?.let { current ->
                                versions.firstOrNull { it == current }
                            } ?: versions.firstOrNull { it == default }
                        }
                    }
                } catch (_: IOException) { }
            }.start()

            Thread {
                try {
                    neoForgeVersions = NeoForgeDL.getNeoForgeVersionsList(mcVersion.id)
                        .also { versions ->
                            neoForgeVersion = defaultLoaderVersion?.let { default ->
                                neoForgeVersion?.let { current ->
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
            title = Strings.creator.version.version(),
            items = minecraftVersions,
            loading = minecraftVersions.isEmpty(),
            selected = minecraftVersion,
            onSelected = { minecraftVersion = it },
            placeholder = Strings.creator.version.version(),
            allowSearch = true
        )

        TitledCheckBox(
            title = Strings.creator.version.showSnapshots(),
            checked = showSnapshots,
            onCheckedChange = { showSnapshots = it }
        )

        TitledComboBox(
            title = Strings.creator.version.type(),
            items = VersionType.entries,
            onSelected = { versionType = it },
            selected = versionType,
        )

        minecraftVersion?.let {
            if (versionType == VersionType.FABRIC) {
                    TitledComboBox(
                        title = Strings.creator.version.fabric(),
                        items = fabricVersions,
                        selected = fabricVersion,
                        onSelected = { fabricVersion = it },
                        loading = fabricVersions.isEmpty(),
                        placeholder = Strings.creator.version.fabric(),
                        loadingPlaceholder = Strings.creator.version.loading(),
                    )
            }

            if(versionType == VersionType.QUILT) {
                TitledComboBox(
                    title = Strings.creator.version.quilt(),
                    items = quiltVersions,
                    selected = quiltVersion,
                    onSelected = { quiltVersion = it },
                    loading = quiltVersions.isEmpty(),
                    placeholder = Strings.creator.version.quilt(),
                    loadingPlaceholder = Strings.creator.version.loading(),
                    toDisplayString = { loader.version }
                )
            }

            if(versionType == VersionType.FORGE) {
                TooltipProvider(
                    tooltip = Strings.version.forgeTooltip(),
                    delay = 0
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            icons().warning,
                            "",
                            tint = LocalContentColor.current.disabledContent(),
                            modifier = Modifier.size(18.dp).offset(y = (-1).dp)
                        )
                        Text(
                            Strings.version.forgeHint(),
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalContentColor.current.disabledContent()
                        )
                    }
                }

                TitledComboBox(
                    title = Strings.creator.version.forge(),
                    items = forgeVersions,
                    selected = forgeVersion,
                    onSelected = { forgeVersion = it },
                    loading = forgeVersions.isEmpty(),
                    placeholder = Strings.creator.version.forge(),
                    loadingPlaceholder = Strings.creator.version.loading(),
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

            if(versionType == VersionType.NEO_FORGE) {
                TitledComboBox(
                    title = Strings.creator.version.neoForge(),
                    items = neoForgeVersions,
                    selected = neoForgeVersion,
                    onSelected = { neoForgeVersion = it },
                    loading = neoForgeVersions.isEmpty(),
                    placeholder = Strings.creator.version.neoForge(),
                    loadingPlaceholder = Strings.creator.version.loading(),
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
                                    }.also {
                                        creationStatus = null
                                    }
                                }
                            } catch (e: IOException) {
                                AppContext.error(e)
                            }
                        }.start()
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
                tooltip = Strings.changer.apply()
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
    VANILLA("vanilla", { Strings.version.vanilla() }),
    FABRIC("fabric", { Strings.version.fabric() }),
    QUILT("quilt", { Strings.version.quilt() }),
    FORGE("forge", { Strings.version.forge() }),
    NEO_FORGE("neoforge", { Strings.version.neoForge() });

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
        VersionType.QUILT -> return QuiltVersionCreator.get(data, onStatus)
        VersionType.FORGE -> return ForgeVersionCreator.get(data, onStatus)
        VersionType.NEO_FORGE -> return NeoForgeVersionCreator.get(data, onStatus)
    }
}

@Throws(IOException::class)
fun VanillaVersionCreator.Companion.get(data: VersionCreationContent, onStatus: (Status) -> Unit): VersionCreator<out VersionCreationData> {
    return VanillaVersionCreator(
        VanillaCreationData(MinecraftProfile.get(data.minecraftVersion!!.url), AppContext.files),
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

@Throws(IOException::class)
fun ForgeVersionCreator.Companion.get(data: VersionCreationContent, onStatus: (Status) -> Unit): VersionCreator<out VersionCreationData> {
    return ForgeVersionCreator(
        ForgeCreationData(
            minecraftVersion = data.minecraftVersion!!.id,
            version = data.forgeVersion!!,
            files = AppContext.files
        ),
        onStatus
    )
}

fun NeoForgeVersionCreator.Companion.get(data: VersionCreationContent, onStatus: (Status) -> Unit): VersionCreator<out VersionCreationData> {
    return NeoForgeVersionCreator(
        NeoForgeCreationData(
            minecraftVersion = data.minecraftVersion!!.id,
            version = data.neoForgeVersion!!,
            files = AppContext.files
        ),
        onStatus
    )
}
