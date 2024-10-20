package dev.treset.treelauncher.instances

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.data.InstanceData
import dev.treset.treelauncher.backend.data.manifest.*
import dev.treset.treelauncher.backend.util.assignFrom
import dev.treset.treelauncher.backend.util.exception.FileLoadException
import dev.treset.treelauncher.backend.util.file.LauncherFile

@Composable
fun InstanceDataProvider(
    instance: InstanceComponent,
    content: @Composable (InstanceData) -> Unit
) {
    val instanceData = remember(instance) {
        try {
            InstanceData.of(instance, AppContext.files)
        } catch (e: FileLoadException) {
            AppContext.severeError(e)
            InstanceData(
                AppContext.files.mainManifest,
                instance,
                emptyList(),
                JavaComponent("", "", LauncherFile.of()),
                OptionsComponent("", "", LauncherFile.of()),
                ResourcepackComponent("", "", LauncherFile.of()),
                SavesComponent("", "", LauncherFile.of()),
                ModsComponent("", "", emptyList(), emptyList(), LauncherFile.of()),
                AppContext.files.gameDataDir,
                AppContext.files.assetsDir,
                AppContext.files.librariesDir
            )
        }
    }

    LaunchedEffect(instance.versionComponent.value) {
        try {
            val versions = InstanceData.getVersionComponents(instance, AppContext.files)
            instanceData.versionComponents.assignFrom(versions)
            instanceData.javaComponent.value = InstanceData.getJavaComponent(versions, AppContext.files)
        } catch (e: FileLoadException) {
            AppContext.severeError(e)
        }
    }

    LaunchedEffect(instance.savesComponent.value) {
        try {
            instanceData.savesComponent.value = InstanceData.getSavesComponent(instance, AppContext.files)
        } catch (e: FileLoadException) {
            AppContext.severeError(e)
        }
    }

    LaunchedEffect(instance.resourcepacksComponent.value) {
        try {
            instanceData.resourcepacksComponent.value = InstanceData.getResourcepacksComponent(instance, AppContext.files)
        } catch (e: FileLoadException) {
            AppContext.severeError(e)
        }
    }

    LaunchedEffect(instance.optionsComponent.value) {
        try {
            instanceData.optionsComponent.value = InstanceData.getOptionsComponent(instance, AppContext.files)
        } catch (e: FileLoadException) {
            AppContext.severeError(e)
        }
    }

    LaunchedEffect(instance.modsComponent.value) {
        try {
            instanceData.modsComponent.value = InstanceData.getModsComponent(instance, AppContext.files)
        } catch (e: FileLoadException) {
            AppContext.severeError(e)
        }
    }

    content(instanceData)
}