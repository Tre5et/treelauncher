package dev.treset.treelauncher.components

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import dev.treset.treelauncher.backend.data.manifest.Component
import dev.treset.treelauncher.backend.util.MutableStateList
import dev.treset.treelauncher.backend.util.file.LauncherFile

open class SharedAddableComponentData<T: Component>(
    component: T,
    reload: () -> Unit,
    val showAdd: MutableState<Boolean> = mutableStateOf(false),
    val filesToAdd: MutableStateList<LauncherFile> = mutableStateListOf()
) : SharedComponentData<T>(
    component,
    reload
)