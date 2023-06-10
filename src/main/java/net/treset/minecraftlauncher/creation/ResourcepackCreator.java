package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.CreationStatus;

import java.util.Map;

public class ResourcepackCreator extends GenericComponentCreator {
    public ResourcepackCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest) {
        super(LauncherManifestType.RESOURCEPACKS_COMPONENT, null, null, name, typeConversion, LauncherApplication.config.RESOURCEPACK_DEFAULT_INCLUDED_FILES, null, componentsManifest);
        setDefaultStatus(CreationStatus.RESOURCEPACKS);
    }

    public ResourcepackCreator(String name, LauncherManifest inheritsFrom, LauncherManifest componentsManifest) {
        super(LauncherManifestType.RESOURCEPACKS_COMPONENT, null, inheritsFrom, name, null, null, null, componentsManifest);
        setDefaultStatus(CreationStatus.RESOURCEPACKS);
    }

    public ResourcepackCreator(LauncherManifest uses) {
        super(LauncherManifestType.RESOURCEPACKS_COMPONENT, uses, null, null, null, null, null, null);
        setDefaultStatus(CreationStatus.RESOURCEPACKS);
    }
}
