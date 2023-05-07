package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.config.Config;

import java.util.Map;

public class ResourcepackCreator extends GenericComponentCreator {
    public ResourcepackCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest) {
        super(LauncherManifestType.RESOURCEPACKS_COMPONENT, null, null, name, typeConversion, Config.RESOURCEPACK_DEFAULT_INCLUDED_FILES, null, componentsManifest);
    }

    public ResourcepackCreator(String name, LauncherManifest inheritsFrom, LauncherManifest componentsManifest) {
        super(LauncherManifestType.RESOURCEPACKS_COMPONENT, null, inheritsFrom, name, null, null, null, componentsManifest);
    }

    public ResourcepackCreator(LauncherManifest uses) {
        super(LauncherManifestType.RESOURCEPACKS_COMPONENT, uses, null, null, null, null, null, null);
    }
}
