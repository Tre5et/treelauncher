package dev.treset.treelauncher.backend.data.patcher

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import com.google.gson.annotations.SerializedName
import dev.treset.treelauncher.backend.data.LauncherModDownload
import dev.treset.treelauncher.backend.data.manifest.LauncherMod
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.backend.util.serialization.MutableDataState
import dev.treset.treelauncher.backend.util.serialization.MutableDataStateList
import kotlinx.serialization.Serializable
import java.io.IOException
import kotlin.jvm.Throws

@Serializable
class Pre3_1LauncherMod(
    val currentProvider: MutableDataState<String?> = mutableStateOf(null),
    val description: MutableDataState<String?> = mutableStateOf(null),
    @SerializedName("enabled", alternate = ["is_enabled"])
    val enabled: MutableDataState<Boolean>,
    val url: MutableDataState<String?> = mutableStateOf(null),
    val iconUrl: MutableDataState<String?> = mutableStateOf(null),
    val name: MutableDataState<String>,
    val fileName: MutableDataState<String>,
    val version: MutableDataState<String>,
    val downloads: MutableDataStateList<LauncherModDownload>
) {
    constructor(
        currentProvider: String?,
        description: String?,
        enabled: Boolean,
        url: String?,
        iconUrl: String?,
        name: String,
        fileName: String,
        version: String,
        downloads: List<LauncherModDownload>
    ): this(
        mutableStateOf(currentProvider),
        mutableStateOf(description),
        mutableStateOf(enabled),
        mutableStateOf(url),
        mutableStateOf(iconUrl),
        mutableStateOf(name),
        mutableStateOf(fileName),
        mutableStateOf(version),
        downloads.toMutableStateList()
    )

    @Throws(IOException::class)
    fun toLauncherMod(directory: LauncherFile): LauncherMod {
        return LauncherMod(
            currentProvider.value,
            description.value,
            enabled.value,
            url.value,
            iconUrl.value,
            name.value,
            version.value,
            downloads.toList(),
        ).apply {
            setModFile(LauncherFile.of(fileName.value))
        }
    }
}