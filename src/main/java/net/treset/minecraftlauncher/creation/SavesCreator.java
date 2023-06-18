package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.CreationStatus;

import java.util.Map;

public class SavesCreator extends GenericComponentCreator {
    private LauncherManifest gameManifest;

    public SavesCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest, LauncherManifest gameManifest) {
        super(LauncherManifestType.SAVES_COMPONENT, null, null, name, typeConversion, LauncherApplication.config.SAVES_DEFAULT_INCLUDED_FILES, null, componentsManifest);
        this.gameManifest = gameManifest;
        setDefaultStatus(new CreationStatus(CreationStatus.DownloadStep.RESOURCEPACKS, null));
    }

    public SavesCreator(String name, LauncherManifest inheritsFrom, LauncherManifest componentsManifest, LauncherManifest gameManifest) {
        super(LauncherManifestType.SAVES_COMPONENT, null, inheritsFrom, name, null, null, null, componentsManifest);
        this.gameManifest = gameManifest;
        setDefaultStatus(new CreationStatus(CreationStatus.DownloadStep.RESOURCEPACKS, null));
    }

    public SavesCreator(LauncherManifest uses) {
        super(LauncherManifestType.SAVES_COMPONENT, uses, null, null, null, null, null, null);
        setDefaultStatus(new CreationStatus(CreationStatus.DownloadStep.SAVES, null));
    }

    @Override
    protected String getParentManifestFileName() {
        return gameManifest.getComponents().get(1);
    }
}
