package net.treset.minecraftlauncher.util;

import net.treset.minecraftlauncher.LauncherApplication;

import java.util.List;

public enum CreationStatus {
    STARTING("creator.status.starting"),
    MODS("creator.status.mods"),
    OPTIONS("creator.status.options"),
    RESOURCEPACKS("creator.status.resourcepacks"),
    SAVES("creator.status.saves"),
    VERSION("creator.status.version"),
    VERSION_VANILLA("creator.status.version.vanilla"),
    VERSION_ASSETS("creator.status.version.assets"),
    VERSION_LIBRARIES("creator.status.version.libraries"),
    VERSION_FABRIC("creator.status.version.fabric"),
    JAVA("creator.status.java"),
    FINISHING("creator.status.finishing");

    private String translationKey;
    private CreationStatus(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getMessage() {
        return LauncherApplication.stringLocalizer.get(translationKey);
    }
}
