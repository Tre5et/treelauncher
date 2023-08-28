package net.treset.minecraftlauncher.ui.generic;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.fabric.FabricProfile;
import net.treset.mc_version_loader.fabric.FabricUtil;
import net.treset.mc_version_loader.fabric.FabricVersionDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.minecraft.MinecraftUtil;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.VersionCreator;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;

import java.util.List;
import java.util.Map;

public abstract class VersionSelectorElement extends StackPane {
    public enum LayoutType {
        HORIZONTAL,
        VERTICAL,
        GRID
    }


    protected final ComboBox<MinecraftVersion> cbVersion = new ComboBox<>();
    protected final CheckBox chSnapshots = new CheckBox();
    protected final ErrorWrapper ewVersion = new ErrorWrapper();
    protected final ComboBox<String> cbType = new ComboBox<>();
    protected final ErrorWrapper ewType = new ErrorWrapper();
    protected final ComboBox<FabricVersionDetails> cbLoader = new ComboBox<>();
    protected final ErrorWrapper ewLoader = new ErrorWrapper();

    protected List<MinecraftVersion> vanillaVersions;
    protected List<FabricVersionDetails> fabricVersions;

    protected Map<String, LauncherManifestType> typeConversion;
    protected LauncherFiles launcherFiles;
    protected String librariesDir;
    protected LauncherManifest versionManifest;

    protected LayoutType layoutType = LayoutType.GRID;

    public VersionSelectorElement() {

        cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
        cbVersion.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateLoaderChoice));

        chSnapshots.setText(LauncherApplication.stringLocalizer.get("creator.version.checkbox.snapshots"));
        chSnapshots.setOnAction(this::onCheckSnapshots);

        ewVersion.setErrorMessage("creator.version.label.error.version");
        ewVersion.getChildren().add(cbVersion);

        cbType.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.type"));
        cbType.getItems().addAll("Vanilla", "Fabric");
        cbType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::updateLoaderChoice));
        cbType.getSelectionModel().select(0);

        ewType.setErrorMessage("creator.version.label.error.type");
        ewType.getChildren().add(cbType);

        cbLoader.setPromptText(LauncherApplication.stringLocalizer.get("selector.loader.prompt"));
        cbLoader.setVisible(false);

        ewLoader.setErrorMessage("creator.version.label.error.loader");
        ewLoader.getChildren().add(cbLoader);

        this.setPadding(new Insets(5));
    }

    public void init(LauncherFiles launcherFiles, Map<String, LauncherManifestType> typeConversion, String librariesDir, LauncherManifest versionManifest) {
        this.typeConversion = typeConversion;
        this.launcherFiles = launcherFiles;
        this.librariesDir = librariesDir;
        this.versionManifest = versionManifest;
    }

    private void onCheckSnapshots(ActionEvent event) {
        populateVersionChoice();
    }

    public abstract boolean isCreateReady();

    protected VersionCreator getCreator() throws ComponentCreationException {
        if(!isCreateReady()) {
            throw new ComponentCreationException("Not ready to create version");
        }
        if("Vanilla".equals(cbType.getValue())) {
            MinecraftVersion version = cbVersion.getValue();
            if(version == null) {
                throw new ComponentCreationException("Could not get Minecraft version");
            }
            MinecraftVersionDetails details;
            try {
                details = MinecraftUtil.getVersionDetails(version.getUrl());
            } catch (FileDownloadException e) {
                throw new ComponentCreationException("Could not get Minecraft version details", e);
            }
            return new VersionCreator(typeConversion, versionManifest, details, launcherFiles, librariesDir);
        } else if("Fabric".equals(cbType.getValue())) {
            FabricVersionDetails details = cbLoader.getValue();
            if(details == null) {
                throw new ComponentCreationException("Could not get Fabric version");
            }
            FabricProfile profile;
            try {
                profile = FabricUtil.getFabricProfile(cbVersion.getValue().getId(), details.getLoader().getVersion());
            } catch (FileDownloadException e) {
                throw new ComponentCreationException("Could not get Fabric profile", e);
            }
            return new VersionCreator(typeConversion, versionManifest, details, profile, launcherFiles, librariesDir);
        }
        throw new ComponentCreationException("Invalid version type");
    }

    public LayoutType getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(LayoutType layoutType) {
        this.layoutType = layoutType;
        updateLayout();
    }

    private void updateLayout() {
        this.getChildren().clear();
        switch(layoutType) {
            case HORIZONTAL -> layoutHorizontal();
            case VERTICAL -> layoutVertical();
            case GRID -> layoutGrid();
        }
    }

    private void layoutHorizontal() {
        this.getChildren().clear();
        this.getChildren().add(getHorizontalContainer());
    }

    protected HBox getHorizontalContainer() {
        HBox container = new HBox();
        container.setSpacing(5);
        container.getChildren().addAll(ewVersion, chSnapshots, ewType, ewLoader);
        return container;
    }

    private void layoutVertical() {
        this.getChildren().clear();
        this.getChildren().add(getVerticalContainer());
    }

    protected VBox getVerticalContainer() {
        VBox container = new VBox();
        container.setSpacing(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(ewVersion, chSnapshots, ewType, ewLoader);
        return container;
    }

    private void layoutGrid() {
        this.getChildren().clear();
        this.getChildren().add(getGridContainer());
    }

    protected GridPane getGridContainer() {
        HBox versionContainer = new HBox();
        versionContainer.setSpacing(5);
        versionContainer.setAlignment(Pos.CENTER_LEFT);
        versionContainer.getChildren().addAll(cbVersion, chSnapshots);

        HBox typeContainer = new HBox();
        typeContainer.setSpacing(5);
        typeContainer.setAlignment(Pos.CENTER_LEFT);
        typeContainer.getChildren().addAll(cbType, cbLoader);

        GridPane.setRowIndex(typeContainer, 1);
        GridPane container = new GridPane();
        container.setVgap(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(versionContainer, typeContainer);
        return container;
    }

    void populateVersionChoice() {
        cbVersion.getItems().clear();
        cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
        cbVersion.setDisable(true);
        updateLoaderChoice();
        new Thread(() -> {
            try {
                vanillaVersions = chSnapshots.isSelected() ? MinecraftUtil.getVersions() : MinecraftUtil.getReleases();
            } catch (FileDownloadException e) {
                LauncherApplication.displayError(e);
                return;
            }
            Platform.runLater(() -> {
                cbVersion.getItems().addAll(vanillaVersions);
                selectVersion();
                cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                cbVersion.setDisable(false);
            });
        }).start();
    }

    protected void selectVersion() {}

    private void updateLoaderChoice() {
        cbLoader.setVisible(false);
        if(cbVersion.getValue() != null && "Fabric".equals(cbType.getValue())) {
            cbLoader.setVisible(true);
            cbLoader.getItems().clear();
            cbLoader.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
            cbLoader.setDisable(true);
            cbLoader.setVisible(true);
            new Thread(() -> {
                try {
                    fabricVersions = FabricUtil.getFabricVersions(cbVersion.getValue().getId());
                } catch (FileDownloadException e) {
                    LauncherApplication.displayError(e);
                    return;
                }
                Platform.runLater(() -> {
                    cbLoader.getItems().addAll(fabricVersions);
                    selectLoader();
                    cbLoader.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loaderversion"));
                    cbLoader.setDisable(false);
                });
            }).start();
        }
    }

    protected void selectLoader() {}
}
