package net.treset.minecraftlauncher.ui.create;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.minecraft.MinecraftUtil;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.creation.ModsCreator;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModsCreatorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsCreatorElement.class);

    @FXML
    private VBox rootPane;
    @FXML private RadioButton rbCreate;
    @FXML private RadioButton rbUse;
    @FXML private RadioButton rbInherit;
    @FXML private TextField tfCreateName;
    @FXML private TextField lbInheritName;
    @FXML private ComboBox<String> cbCreateVersion;
    @FXML private CheckBox chCreateSnapshots;
    @FXML private ComboBox<String> cbUse;
    @FXML private ComboBox<String> cbInherit;
    @FXML private Label lbCreateError;
    @FXML private Label lbCreateVersionError;
    @FXML private Label lbUseError;
    @FXML private Label lbInheritErrorName;
    @FXML private Label lbInheritErrorSelect;
    @FXML private VBox bvCreate;
    @FXML private VBox vbUse;
    @FXML private VBox vbInherit;

    private List<Pair<LauncherManifest, LauncherModsDetails>> modsComponents;
    private Map<String, LauncherManifestType> typeConversion;
    private LauncherManifest modsManifest;
    private LauncherManifest gameManifest;
    private String gameVersion;
    private String modsType;
    private boolean selectVersion;

    public void setPrerequisites(List<Pair<LauncherManifest, LauncherModsDetails>> modsComponents, Map<String, LauncherManifestType> typeConversion, LauncherManifest savesManifest, LauncherManifest gameManifest) {
        this.modsComponents = modsComponents;
        this.typeConversion = typeConversion;
        this.modsManifest = savesManifest;
        this.gameManifest = gameManifest;
    }

    @FXML private void onRadioCreate() {
        bvCreate.setDisable(false);
        vbUse.setDisable(true);
        vbInherit.setDisable(true);
    }
    @FXML private void onRadioUse() {
        bvCreate.setDisable(true);
        vbUse.setDisable(false);
        vbInherit.setDisable(true);
    }
    @FXML private void onRadioInherit() {
        bvCreate.setDisable(true);
        vbUse.setDisable(true);
        vbInherit.setDisable(false);
    }
    @FXML private void onSnapshotsCheck() {
        populateVersionChoice();
    }
    private void onVersionUpdated() {
        gameVersion = cbCreateVersion.getSelectionModel().getSelectedItem();
    }

    @Override
    public void beforeShow(Stage stage) {
        rbCreate.fire();
        rbCreate.setSelected(true);
        bvCreate.setDisable(false);
        tfCreateName.setText("");
        tfCreateName.getStyleClass().remove("error");
        lbCreateError.setVisible(false);
        cbCreateVersion.getItems().clear();
        cbCreateVersion.getStyleClass().remove("error");
        lbCreateVersionError.setVisible(false);
        vbUse.setDisable(true);
        cbUse.getItems().clear();
        cbUse.getStyleClass().remove("error");
        lbUseError.setVisible(false);
        vbInherit.setDisable(true);
        lbInheritName.setText("");
        lbInheritName.getStyleClass().remove("error");
        lbInheritErrorName.setVisible(false);
        cbInherit.getItems().clear();
        cbInherit.getStyleClass().remove("error");
        lbInheritErrorSelect.setVisible(false);
        for(Pair<LauncherManifest, LauncherModsDetails> manifest : modsComponents) {
            cbUse.getItems().add(manifest.getKey().getName());
            cbInherit.getItems().add(manifest.getKey().getName());
        }
        if(selectVersion) {
            populateVersionChoice();
            cbCreateVersion.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::onVersionUpdated));
        }
    }

    private void populateVersionChoice() {
        gameVersion = null;
        cbCreateVersion.getItems().clear();
        cbCreateVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
        cbCreateVersion.setDisable(true);
        new Thread(() -> {
            List<MinecraftVersion> minecraftVersions;
            try {
                minecraftVersions = chCreateSnapshots.isSelected() ? MinecraftUtil.getVersions() : MinecraftUtil.getReleases();
            } catch (FileDownloadException e) {
                LOGGER.error("Failed to get versions", e);
                return;
            }
            List<String> names = new ArrayList<>();
            for (MinecraftVersion version : minecraftVersions) {
                names.add(version.getId());
            }
            Platform.runLater(() -> {
                cbCreateVersion.getItems().addAll(names);
                cbCreateVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                cbCreateVersion.setDisable(false);
            });
        }).start();
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void create() throws ComponentCreationException {
        if(!checkCreateReady()) {
            throw new ComponentCreationException("Not ready to create mods!");
        }
        ModsCreator creator = getCreator();
        creator.getId();
    }

    public ModsCreator getCreator() throws ComponentCreationException {
        if(!checkCreateReady()) {
            throw new ComponentCreationException("Not ready to create mods!");
        }
        if(rbCreate.isSelected()) {
            return new ModsCreator(tfCreateName.getText(), typeConversion, modsManifest, modsType, gameVersion, gameManifest);
        } else if(rbUse.isSelected()) {
            Pair<LauncherManifest, LauncherModsDetails> manifest = getModsFromName(cbUse.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                throw new ComponentCreationException("Could not find mods: name=" + cbUse.getSelectionModel().getSelectedItem());
            }
            return new ModsCreator(manifest);
        } else if(rbInherit.isSelected()) {
            Pair<LauncherManifest, LauncherModsDetails> manifest = getModsFromName(cbInherit.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                throw new ComponentCreationException("Could not find mods: name=" + cbInherit.getSelectionModel().getSelectedItem());
            }
            return new ModsCreator(lbInheritName.getText(), manifest, modsManifest, gameManifest);
        }
        throw new ComponentCreationException("No radio button selected!");
    }

    private Pair<LauncherManifest, LauncherModsDetails> getModsFromName(String name) {
        for(Pair<LauncherManifest, LauncherModsDetails> manifest : modsComponents) {
            if(name.equals(manifest.getKey().getName())) {
                return manifest;
            }
        }
        return null;
    }

    public void showError(boolean show) {
        lbCreateError.setVisible(false);
        tfCreateName.getStyleClass().remove("error");
        lbCreateVersionError.setVisible(false);
        cbCreateVersion.getStyleClass().remove("error");
        lbUseError.setVisible(false);
        cbUse.getStyleClass().remove("error");
        lbInheritErrorName.setVisible(false);
        lbInheritName.getStyleClass().remove("error");
        lbInheritErrorSelect.setVisible(false);
        cbInherit.getStyleClass().remove("error");
        if(show) {
            if(rbCreate.isSelected()) {
                if(tfCreateName.getText().isBlank()) {
                    lbCreateError.setVisible(true);
                    tfCreateName.getStyleClass().add("error");
                }
                if(selectVersion && (gameVersion == null || gameVersion.isBlank())) {
                    lbCreateVersionError.setVisible(true);
                    cbCreateVersion.getStyleClass().add("error");
                }
            } else if(rbUse.isSelected() && cbUse.getSelectionModel().isEmpty()) {
                lbUseError.setVisible(true);
                cbUse.getStyleClass().add("error");
            } else if(rbInherit.isSelected()) {
                if(lbInheritName.getText().isBlank()) {
                    lbInheritErrorName.setVisible(true);
                    lbInheritName.getStyleClass().add("error");
                }
                if(cbInherit.getSelectionModel().isEmpty()) {
                    lbInheritErrorSelect.setVisible(true);
                    cbInherit.getStyleClass().add("error");
                }
            }
        }
    }

    public void enableUse(boolean enable) {
        rbUse.setVisible(enable);
        vbUse.setVisible(enable);
    }

    public void enableVersionSelect(boolean enable) {
        selectVersion = true;
        cbCreateVersion.setVisible(enable);
        chCreateSnapshots.setVisible(enable);
    }

    public boolean checkCreateReady() {
        return (modsComponents != null && typeConversion != null && modsManifest != null && gameManifest != null && (((gameVersion != null || (selectVersion && cbCreateVersion.getSelectionModel().getSelectedItem() != null)) && modsType != null && rbCreate.isSelected() && !tfCreateName.getText().isBlank()) || (rbUse.isSelected() && !cbUse.getSelectionModel().isEmpty()) || (rbInherit.isSelected() && !lbInheritName.getText().isBlank() && !cbInherit.getSelectionModel().isEmpty())));
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
}
