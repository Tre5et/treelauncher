package net.treset.treelauncher.backend.creation

import net.treset.mc_version_loader.launcher.LauncherManifest
import net.treset.mc_version_loader.launcher.LauncherManifestType
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.CreationStatus

class SavesCreator : GenericComponentCreator {
    private var gameManifest: LauncherManifest? = null

    constructor(
        name: String,
        typeConversion: Map<String, LauncherManifestType>,
        componentsManifest: LauncherManifest,
        gameManifest: LauncherManifest
    ) : super(
        LauncherManifestType.SAVES_COMPONENT,
        null,
        null,
        name,
        typeConversion,
        appConfig().SAVES_DEFAULT_INCLUDED_FILES,
        null,
        componentsManifest
    ) {
        this.gameManifest = gameManifest
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.SAVES, null)
    }

    constructor(
        name: String,
        inheritsFrom: LauncherManifest,
        componentsManifest: LauncherManifest,
        gameManifest: LauncherManifest
    ) : super(LauncherManifestType.SAVES_COMPONENT, null, inheritsFrom, name, null, null, null, componentsManifest) {
        this.gameManifest = gameManifest
        defaultStatus = CreationStatus(CreationStatus.DownloadStep.SAVES, null)
    }

    constructor(uses: LauncherManifest) : super(
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

    override val parentManifestFileName: String
        get() = gameManifest?.components?.get(1)?: super.parentManifestFileName
}
