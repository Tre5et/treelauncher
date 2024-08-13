package net.treset.treelauncher.backend.creation

import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.data.manifest.ComponentManifest
import net.treset.treelauncher.backend.data.manifest.LauncherManifestType
import net.treset.treelauncher.backend.data.manifest.ParentManifest
import net.treset.treelauncher.backend.util.CreationStatus

class SavesCreator : GenericComponentCreator {
    constructor(
        name: String,
        typeConversion: Map<String, LauncherManifestType>,
        componentsManifest: ParentManifest
    ) : super(
        LauncherManifestType.SAVES_COMPONENT,
        null,
        null,
        name,
        typeConversion,
        appConfig().savesDefaultIncludedFiles,
        null,
        componentsManifest
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.SAVES, null)
    }

    constructor(
        name: String,
        inheritsFrom: ComponentManifest,
        componentsManifest: ParentManifest,
    ) : super(LauncherManifestType.SAVES_COMPONENT, null, inheritsFrom, name, null, null, null, componentsManifest) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.SAVES, null)
    }

    constructor(uses: ComponentManifest) : super(
        LauncherManifestType.SAVES_COMPONENT,
        uses,
        null,
        null,
        null,
        null,
        null,
        null
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.SAVES, null)
    }
}
