package dev.treset.treelauncher.backend.data.manifest

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.config.appConfig
import dev.treset.treelauncher.backend.util.EmptyingJobQueue
import dev.treset.treelauncher.backend.util.copyTo
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import dev.treset.treelauncher.backend.util.sort.LauncherModNameComparator
import dev.treset.treelauncher.backend.util.sort.Sort
import dev.treset.treelauncher.backend.data.ModProviderData
import dev.treset.treelauncher.backend.data.ModProviderList
import dev.treset.treelauncher.backend.data.deepCopy
import dev.treset.treelauncher.backend.util.MutableStateList
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.generic.VersionType
import dev.treset.treelauncher.util.ListDisplay
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.IOException
import kotlin.jvm.Throws

@Serializable
class ModsComponent(
    override val id: MutableDataState<String>,
    override val name: MutableDataState<String>,
    val types: MutableDataStateList<String>,
    val versions: MutableDataStateList<String>,
    @Transient override val file: MutableDataState<LauncherFile> = mutableStateOf(LauncherFile.of("")),
    val autoUpdate: MutableDataState<Boolean> = mutableStateOf(AppSettings.modsDefaultAutoUpdate.value),
    val enableOnUpdate: MutableDataState<Boolean> = mutableStateOf(AppSettings.modsDefaultEnableOnUpdate.value),
    val disableOnNoVersion: MutableDataState<Boolean> = mutableStateOf(AppSettings.modsDefaultDisableOnNoVersion.value),
    val providers: ModProviderList = AppSettings.modsDefaultProviders.deepCopy(),
    override val includedFiles: MutableDataStateList<String> = appConfig().modsDefaultIncludedFiles.toMutableStateList(),
    override val lastUsed: MutableDataState<String> = mutableStateOf(""),
    override val active: MutableDataState<Boolean> = mutableStateOf(false),
    @Transient val mods: MutableStateList<LauncherMod> = mutableStateListOf(),
    val sort: Sort<LauncherMod> = Sort(
        LauncherModNameComparator,
        false
    ),
    override val listDisplay: MutableDataState<ListDisplay?> = mutableStateOf(null)
): Component() {
    override val type = LauncherManifestType.MODS_COMPONENT
    @Transient override var expectedType = LauncherManifestType.MODS_COMPONENT

    val modsDirectory: LauncherFile
        get() = directory.child("mods")

    @Transient private val jobQueue = EmptyingJobQueue(
        onEmptied = {
            try {
                write()
            } catch (e: IOException) {
                AppContext.error(e)
            }
        }
    ) {
        mods
    }

    constructor(
        id: String,
        name: String,
        types: List<String>,
        versions: List<String>,
        file: LauncherFile,
        autoUpdate: Boolean = AppSettings.modsDefaultAutoUpdate.value,
        enable: Boolean = AppSettings.modsDefaultEnableOnUpdate.value,
        disable: Boolean = AppSettings.modsDefaultDisableOnNoVersion.value,
        providers: List<ModProviderData> = AppSettings.modsDefaultProviders.deepCopy(),
        active: Boolean = false,
        lastUsed: String = "",
        includedFiles: List<String> = appConfig().modsDefaultIncludedFiles,
        mods: List<LauncherMod> = emptyList(),
        sort: Sort<LauncherMod> = Sort(
            LauncherModNameComparator,
            false
        ),
        listDisplay: ListDisplay? = null
    ): this(
        mutableStateOf(id),
        mutableStateOf(name),
        types.toMutableStateList(),
        versions.toMutableStateList(),
        mutableStateOf(file),
        mutableStateOf(autoUpdate),
        mutableStateOf(enable),
        mutableStateOf(disable),
        providers.toMutableStateList(),
        includedFiles.toMutableStateList(),
        mutableStateOf(lastUsed),
        mutableStateOf(active),
        mods.toMutableStateList(),
        sort,
        mutableStateOf(listDisplay)
    )

    fun loadMods() {
        val files = modsDirectory.listFiles{f -> f.name.endsWith(".jar")  || f.name.endsWith(".jar.disabled")}

        files ?: run {
            mods.clear()
            return
        }

        val loadedMods = files.mapNotNull { f ->
            LauncherMod.loadOrRawFile(LauncherFile.of(f), modsDirectory)
        }

        mods.assignFrom(loadedMods)
    }

    fun registerJob(job: (MutableList<LauncherMod>) -> Unit) {
        jobQueue.add(job)
    }

    override fun copyData(other: Component) {
        super.copyData(other)

        if (other is ModsComponent) {
            types.copyTo(other.types)
            versions.copyTo(other.versions)
            mods.copyTo(other.mods)
        }
    }

    @Throws(IOException::class)
    override fun write() {
        super.write()

        mods.forEach(LauncherMod::write)
    }

    companion object {
        val LOGGER = KotlinLogging.logger {  }
    }
}

fun List<String>.toVersionTypes(): List<VersionType> {
    return VersionType.fromIds(this)
}