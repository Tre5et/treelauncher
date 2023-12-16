package net.treset.minecraftlauncher.ui.create;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.minecraft.MinecraftGame;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.treelauncher.backend.creation.ModsCreator;
import net.treset.minecraftlauncher.ui.generic.ErrorWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModsCreatorElement extends CreatorElement {
    private final ComboBox<MinecraftVersion> cbVersion = new ComboBox<>();
    private final CheckBox chSnapshots = new CheckBox();
    private final HBox hbVersion = new HBox();
    private final ErrorWrapper ewVersion = new ErrorWrapper();

    private List<Pair<LauncherManifest, LauncherModsDetails>> components;
    private LauncherManifest gameManifest;
    private String gameVersion;
    private String modsType;

    private boolean selectVersion = false;

    public ModsCreatorElement() {
        super();

        cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.mods.prompt.version"));
        cbVersion.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> ewVersion.showError(false));

        ewVersion.setErrorMessage("creator.version.label.error.version");
        ewVersion.getChildren().add(cbVersion);

        chSnapshots.setText(LauncherApplication.stringLocalizer.get("creator.version.checkbox.snapshots"));
        chSnapshots.setOnAction(this::onSelectSnapshots);

        hbVersion.setSpacing(5);
        hbVersion.setAlignment(Pos.CENTER_LEFT);
        hbVersion.getChildren().addAll(ewVersion, chSnapshots);

        populateVersionChoice();
    }

    public void init(List<Pair<LauncherManifest, LauncherModsDetails>> components, Map<String, LauncherManifestType> typeConversion, LauncherManifest topManifest, LauncherManifest gameManifest) {
        super.init(components.stream().map(Pair::getKey).toList(), typeConversion, topManifest);
        this.components = components;
        this.gameManifest = gameManifest;

        ewVersion.showError(false);
    }

    @Override
    public boolean isCreateReady() {
        return super.isCreateReady() && modsType != null && (!rbCreate.isSelected() || (selectVersion ? cbVersion.getValue() != null : gameVersion != null));

    }

    @Override
    public void clear() {
        super.clear();
        cbVersion.getSelectionModel().clearSelection();
        chSnapshots.setSelected(false);
        ewVersion.showError(false);
    }

    @Override
    public ModsCreator getCreator() {
        if(!isCreateReady()) {
            throw new IllegalStateException("Creator not ready");
        }

        if(rbCreate.isSelected()) {
            return new ModsCreator(tfCreate.getText(), typeConversion, topManifest, modsType, selectVersion ? cbVersion.getValue().getId() : gameVersion, gameManifest);
        } else if(rbInherit.isSelected()) {
            Optional<Pair<LauncherManifest, LauncherModsDetails>> component = components.stream().filter(c -> c.getKey().equals(cbInherit.getValue())).findFirst();
            if(component.isEmpty()) {
                throw new IllegalStateException("Could not find component: name=" + cbInherit.getValue());
            }
            return new ModsCreator(tfInherit.getText(), component.get(), topManifest, gameManifest);
        } else if(rbUse.isSelected()) {
            Optional<Pair<LauncherManifest, LauncherModsDetails>> component = components.stream().filter(c -> c.getKey().equals(cbUse.getValue())).findFirst();
            if(component.isEmpty()) {
                throw new IllegalStateException("Could not find component: name=" + cbInherit.getValue());
            }
            return new ModsCreator(component.get());
        } else {
            throw new IllegalStateException("No radio button selected");
        }
    }

    @Override
    protected void deselectAll() {
        super.deselectAll();
        if(ewVersion != null) {
            ewVersion.showError(false);
            ewVersion.setDisable(true);
        }
    }

    @Override
    protected void onRadioCreate(ActionEvent event) {
        super.onRadioCreate(event);
        if(ewVersion != null) {
            ewVersion.setDisable(false);
        }
    }

    @Override
    public void showError(boolean show) {
        super.showError(show);
        ewVersion.showError(false);
        if(show && selectVersion && rbCreate.isSelected() && cbVersion.getValue() == null) {
            ewVersion.showError(true);
        }
    }

    public boolean isSelectVersion() {
        return selectVersion;
    }

    public void setSelectVersion(boolean selectVersion) {
        if(selectVersion == this.selectVersion) {
            return;
        }
        this.selectVersion = selectVersion;
        if(selectVersion) {
            this.getChildren().add(2, hbVersion);
        } else {
            this.getChildren().remove(hbVersion);
        }
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getModsType() {
        return modsType;
    }

    public void setModsType(String modsType) {
        this.modsType = modsType;
    }

    private void onSelectSnapshots(ActionEvent event) {
        populateVersionChoice();
    }

    private void populateVersionChoice() {
        gameVersion = null;
        cbVersion.getItems().clear();
        cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
        cbVersion.setDisable(true);
        new Thread(() -> {
            List<MinecraftVersion> minecraftVersions;
            try {
                minecraftVersions = chSnapshots.isSelected() ? MinecraftGame.getVersions() : MinecraftGame.getReleases();
            } catch (FileDownloadException e) {
                LauncherApplication.displayError(e);
                return;
            }

            Platform.runLater(() -> {
                cbVersion.getItems().addAll(minecraftVersions);
                cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                cbVersion.setDisable(false);
            });
        }).start();
    }
}
