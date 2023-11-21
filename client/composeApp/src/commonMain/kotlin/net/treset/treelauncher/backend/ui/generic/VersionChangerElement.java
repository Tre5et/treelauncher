package net.treset.minecraftlauncher.ui.generic;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.file.LauncherFile;

import java.util.Map;
import java.util.function.Consumer;

public class VersionChangerElement extends VersionSelectorElement {
    private final IconButton btChange = new IconButton();

    private Consumer<VersionCreator> changeCallback;
    private Consumer<Exception> changeFailCallback;

    private LauncherVersionDetails currentVersion;

    public VersionChangerElement() {
        btChange.getStyleClass().addAll("sync", "highlight");
        btChange.setIconSize(32);
        btChange.setTooltipText(LauncherApplication.stringLocalizer.get("selector.change.tooltip"));
        btChange.setOnAction(this::onChange);

        GridPane.setColumnIndex(btChange, 1);
        GridPane.setRowSpan(btChange, 2);
    }

    public void init(LauncherFiles launcherFiles, Map<String, LauncherManifestType> typeConversion, LauncherFile librariesDir, LauncherManifest versionManifest, Consumer<VersionCreator> changeCallback, Consumer<Exception> changeFailCallback) {
        super.init(launcherFiles, typeConversion, librariesDir, versionManifest);

        this.changeCallback = changeCallback;
        this.changeFailCallback = changeFailCallback;

        cbVersion.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateButtonState));
        cbType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateButtonState));
        cbLoader.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateButtonState));
    }

    @Override
    public boolean isCreateReady() {
        return cbVersion.getValue() != null
                && cbType.getValue() != null
                && (
                        "Vanilla".equals(cbType.getValue())
                        || "Fabric".equals(cbType.getValue()) && cbLoader.getValue() != null
                )
                && (
                        !currentVersion.getVersionNumber().equals(cbVersion.getValue().getId())
                        || !currentVersion.getVersionType().equals(cbType.getValue().toLowerCase())
                        || (currentVersion.getLoaderVersion() != null && !currentVersion.getLoaderVersion().equals(cbLoader.getValue().getLoader().getVersion()))
                );
    }

    public LauncherVersionDetails getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(LauncherVersionDetails currentVersion) {
        this.currentVersion = currentVersion;
        if(currentVersion != null) {
            switch (currentVersion.getVersionType()) {
                case "vanilla" -> cbType.getSelectionModel().select("Vanilla");
                case "fabric" -> cbType.getSelectionModel().select("Fabric");
            }
        }
        populateVersionChoice();
    }

    @Override
    protected void selectVersion() {
        if(currentVersion != null) {
            cbVersion.getSelectionModel().select(vanillaVersions.stream().map(MinecraftVersion::getId).toList().indexOf(currentVersion.getVersionNumber()));
        }
    }

    @Override
    protected void selectLoader() {
        if(currentVersion != null) {
            cbLoader.getSelectionModel().select(fabricVersions.stream().map((v) -> v.getLoader().getVersion()).toList().indexOf(currentVersion.getLoaderVersion()));
        }
    }

    @Override
    protected HBox getHorizontalContainer() {
        HBox container = super.getHorizontalContainer();
        container.getChildren().add(btChange);
        return container;
    }

    @Override
    protected VBox getVerticalContainer() {
        VBox container = super.getVerticalContainer();
        container.getChildren().add(btChange);
        return container;
    }

    @Override
    protected GridPane getGridContainer() {
        GridPane container = super.getGridContainer();
        container.getChildren().add(btChange);
        return container;
    }

    private void updateButtonState() {
        btChange.setDisable(!isCreateReady());
    }

    private void onChange(ActionEvent event) {
        if(isCreateReady()) {
            try {
                VersionCreator creator = getCreator();
                changeCallback.accept(creator);
            } catch (ComponentCreationException e) {
                changeFailCallback.accept(e);
            }
        }
    }
}
