package net.treset.treelauncher.backend.creation

import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.CreationStatus

class OptionsCreator : GenericComponentCreator {
    constructor(
        name: String,
        typeConversion: Map<String, LauncherManifestType>,
        componentsManifest: LauncherManifest
    ) : super(
        LauncherManifestType.OPTIONS_COMPONENT,
        null,
        null,
        name,
        typeConversion,
        appConfig().OPTIONS_DEFAULT_INCLUDED_FILES,
        null,
        componentsManifest
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.OPTIONS, null)
    }

    constructor(name: String, inheritsFrom: LauncherManifest, componentsManifest: LauncherManifest) : super(
        LauncherManifestType.OPTIONS_COMPONENT,
        null,
        inheritsFrom,
        name,
        null,
        null,
        null,
        componentsManifest
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.OPTIONS, null)
    }

    constructor(uses: LauncherManifest) : super(
        LauncherManifestType.OPTIONS_COMPONENT,
        uses,
        null,
        null,
        null,
        null,
        null,
        null
    ) {
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.OPTIONS, null)
    }
}
