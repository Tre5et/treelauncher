package net.treset.treelauncher.backend.creation

import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.CreationStatus

class ResourcepackCreator : GenericComponentCreator {
    constructor(
        name: String,
        typeConversion: Map<String, LauncherManifestType>,
        componentsManifest: LauncherManifest
    ) : super(
        LauncherManifestType.RESOURCEPACKS_COMPONENT,
        null,
        null,
        name,
        typeConversion,
        appConfig().RESOURCEPACK_DEFAULT_INCLUDED_FILES,
        null,
        componentsManifest
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.RESOURCEPACKS, null)
    }

    constructor(name: String, inheritsFrom: LauncherManifest, componentsManifest: LauncherManifest) : super(
        LauncherManifestType.RESOURCEPACKS_COMPONENT,
        null,
        inheritsFrom,
        name,
        null,
        null,
        null,
        componentsManifest
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.RESOURCEPACKS, null)
    }

    constructor(uses: LauncherManifest) : super(
        LauncherManifestType.RESOURCEPACKS_COMPONENT,
        uses,
        null,
        null,
        null,
        null,
        null,
        null
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.RESOURCEPACKS, null)
    }
}
