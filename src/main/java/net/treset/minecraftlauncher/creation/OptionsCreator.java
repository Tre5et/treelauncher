package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.CreationStatus;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;

import java.util.Map;

public class OptionsCreator extends GenericComponentCreator {

    public OptionsCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest) {
        super(LauncherManifestType.OPTIONS_COMPONENT, null, null, name, typeConversion, LauncherApplication.config.OPTIONS_DEFAULT_INCLUDED_FILES, null, componentsManifest);
        setDefaultStatus(CreationStatus.OPTIONS);
    }

    public OptionsCreator(String name, LauncherManifest inheritsFrom, LauncherManifest componentsManifest) {
        super(LauncherManifestType.OPTIONS_COMPONENT, null, inheritsFrom, name, null, null, null, componentsManifest);
        setDefaultStatus(CreationStatus.OPTIONS);
    }

    public OptionsCreator(LauncherManifest uses) {
        super(LauncherManifestType.OPTIONS_COMPONENT, uses, null, null, null, null, null, null);
        setDefaultStatus(CreationStatus.OPTIONS);
    }
}
