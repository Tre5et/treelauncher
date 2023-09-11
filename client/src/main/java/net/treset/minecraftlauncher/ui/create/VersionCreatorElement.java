package net.treset.minecraftlauncher.ui.create;

import javafx.application.Platform;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.generic.VersionSelectorElement;

import java.util.Map;
import java.util.function.Consumer;

public class VersionCreatorElement extends VersionSelectorElement {

    private Consumer<Boolean> modsActivateCallback;

    public void init(Map<String, LauncherManifestType> typeConversion, LauncherManifest versionManifest, LauncherFiles launcherFiles, String librariesDir, Consumer<Boolean> modsActivateCallback) {
        super.init(launcherFiles, typeConversion, librariesDir, versionManifest);

        this.modsActivateCallback = modsActivateCallback;

        cbType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateLoaderChoice));

        populateVersionChoice();
    }

    public void showError(boolean show) {
        ewVersion.showError(false);
        ewType.showError(false);
        ewLoader.showError(false);
        if(show) {
            if(cbVersion.getValue() == null) {
                ewVersion.showError(true);
            }
            if(!"Vanilla".equals(cbType.getValue()) && !"Fabric".equals(cbType.getValue())) {
                ewType.showError(true);
            }
            if("Fabric".equals(cbType.getValue()) && cbLoader.getValue() == null) {
                ewLoader.showError(true);
            }
        }
    }

    public String getGameVersion() {
        MinecraftVersion version = cbVersion.getValue();
        return version == null ? null : version.getId();
    }

    public String getVersionType() {
        if("Fabric".equals(cbType.getValue())) {
            return "fabric";
        }
        return null;
    }

    @Override
    public boolean isCreateReady() {
        return cbVersion.getValue() != null && cbType.getValue() != null && ("Vanilla".equals(cbType.getValue()) || "Fabric".equals(cbType.getValue()) && cbLoader.getValue() != null);
    }

    @Override
    protected void updateLoaderChoice() {
        super.updateLoaderChoice();
        if(modsActivateCallback == null) return;
        if("Fabric".equals(cbType.getValue())) {
            modsActivateCallback.accept(true);
        } else {
            modsActivateCallback.accept(false);
        }
    }

}
