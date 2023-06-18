package net.treset.minecraftlauncher.util;

import net.treset.mc_version_loader.files.DownloadStatus;
import net.treset.minecraftlauncher.LauncherApplication;

public class CreationStatus {
    private final DownloadStep currentStep;
    private final DownloadStatus downloadStatus;

    public CreationStatus(DownloadStep currentStep, DownloadStatus downloadStatus) {
        this.currentStep = currentStep;
        this.downloadStatus = downloadStatus;
    }

    public DownloadStep getCurrentStep() {
        return currentStep;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public enum DownloadStep {
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
        private final String translationKey;
        DownloadStep(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getMessage() {
            return LauncherApplication.stringLocalizer.get(translationKey);
        }
    }
}
