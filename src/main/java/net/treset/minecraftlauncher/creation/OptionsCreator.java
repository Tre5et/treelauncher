package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class OptionsCreator extends GenericComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(OptionsCreator.class);

    private String name;
    private LauncherManifest inheritsFrom;
    private LauncherManifest uses;
    private Map<String, LauncherManifestType> typeConversion;

    public OptionsCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest) {
        super(componentsManifest);
        this.name = name;
        this.typeConversion = typeConversion;
    }

    public OptionsCreator(String name, LauncherManifest inheritsFrom, LauncherManifest componentsManifest) {
        super(componentsManifest);
        this.name = name;
        this.inheritsFrom = inheritsFrom;
    }

    public OptionsCreator(LauncherManifest uses, LauncherManifest componentsManifest) {
        super(componentsManifest);
        this.uses = uses;
    }

    @Override
    public String getId() {
        if(uses != null) {
            return useComponent();
        }

        if(name == null || inheritsFrom == null || inheritsFrom.getType() != LauncherManifestType.OPTIONS_COMPONENT) {
            LOGGER.warn("Unable to create options component: invalid parameters");
            return null;
        }

        if(inheritsFrom != null) {
            return inheritComponent();
        }

        return createComponent();
    }

    public String createComponent() {
        if(typeConversion == null) {
            LOGGER.warn("Unable to create options component: invalid parameters");
            return null;
        }

        String manifestType = getManifestType(LauncherManifestType.OPTIONS_COMPONENT, typeConversion);
        if(manifestType == null) {
            LOGGER.warn("Unable to create options component: unable to get manifest type");
            return null;
        }
        LauncherManifest manifest = new LauncherManifest(manifestType, typeConversion, null, null, null, name, Config.OPTIONS_DEFAULT_INCLUDED_FILES, null);
        manifest.setId(FormatUtil.hash(manifest));
        if(!writeManifest(manifest)) {
            LOGGER.warn("Unable to create options component: unable to write manifest");
            return null;
        }
        return manifest.getId();
    }

    public String useComponent() {
        if(uses.getType() != LauncherManifestType.OPTIONS_COMPONENT || uses.getId() == null) {
            LOGGER.warn("Unable to use options component: invalid component specified");
            return null;
        }
        return uses.getId();
    }

    public String inheritComponent() {
        if(inheritsFrom.getType() != LauncherManifestType.OPTIONS_COMPONENT) {
            LOGGER.warn("Unable to inherit options component: invalid component specified");
            return null;
        }
        String manifestType = getManifestType(LauncherManifestType.OPTIONS_COMPONENT, inheritsFrom.getTypeConversion());
        if(manifestType == null) {
            LOGGER.warn("Unable to inherit options component: unable to get manifest type");
            return null;
        }
        LauncherManifest manifest = new LauncherManifest(manifestType, inheritsFrom.getTypeConversion(), null, inheritsFrom.getDetails(), inheritsFrom.getPrefix(), name, inheritsFrom.getIncludedFiles(), inheritsFrom.getComponents());
        manifest.setId(FormatUtil.hash(manifest));
        if(!writeManifest(manifest)) {
            LOGGER.warn("Unable to inherit options component: unable to write manifest to file");
            return null;
        }

        if(!copyFiles(inheritsFrom, manifest)) {
            LOGGER.warn("Unable to inherit options component: unable to copy files");
            return null;
        }

        return manifest.getId();
    }
}
