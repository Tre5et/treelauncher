package net.treset.minecraftlauncher.creation;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.CreationStatus;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ModsCreator extends GenericComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(ModsCreator.class);

    private final String modsType;
    private final String modsVersion;
    private LauncherManifest gameManifest;

    public ModsCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest, String modsType, String modsVersion, LauncherManifest gameManifest) {
        super(LauncherManifestType.MODS_COMPONENT, null, null, name, typeConversion, LauncherApplication.config.MODS_DEFAULT_INCLUDED_FILES, LauncherApplication.config.MODS_DEFAULT_DETAILS, componentsManifest);
        this.modsType = modsType;
        this.modsVersion = modsVersion;
        this.gameManifest = gameManifest;
        setDefaultStatus(new CreationStatus(CreationStatus.DownloadStep.MODS, null));
    }

    public ModsCreator(String name, Pair<LauncherManifest, LauncherModsDetails> inheritsFrom, LauncherManifest componentsManifest, LauncherManifest gameManifest) {
        super(LauncherManifestType.MODS_COMPONENT, null, inheritsFrom.getKey(), name, null, null, null, componentsManifest);
        modsType = null;
        modsVersion = null;
        this.gameManifest = gameManifest;
        setDefaultStatus(new CreationStatus(CreationStatus.DownloadStep.MODS, null));
    }

    public ModsCreator(Pair<LauncherManifest, LauncherModsDetails> uses) {
        super(LauncherManifestType.MODS_COMPONENT, uses.getKey(), null, null, null, null, null, null);
        modsType = null;
        modsVersion = null;
        setDefaultStatus(new CreationStatus(CreationStatus.DownloadStep.MODS, null));
    }

    @Override
    public String createComponent() throws ComponentCreationException {
        String result = super.createComponent();
        if(result == null || getNewManifest() == null) {
            LOGGER.error("Failed to create mods component: invalid data");
            attemptCleanup();
            return null;
        }
        LauncherModsDetails details = new LauncherModsDetails(modsType, modsVersion, List.of());
        try {
            details.writeToFile(getNewManifest().getDirectory() + LauncherApplication.config.MODS_DEFAULT_DETAILS);
        } catch (IOException e) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create mods component: failed to write details", e);
        }
        return result;
    }

    @Override
    protected String getParentManifestFileName() {
        return gameManifest.getComponents().get(0);
    }
}
