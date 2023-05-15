package net.treset.minecraftlauncher.ui.create;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.minecraftlauncher.creation.ModsCreator;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class ModsCreatorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsCreatorElement.class);

    @FXML
    private VBox rootPane;
    @FXML private RadioButton radioCreate;
    @FXML private RadioButton radioUse;
    @FXML private RadioButton radioInherit;
    @FXML private TextField createName;
    @FXML private TextField inheritName;
    @FXML private ComboBox<String> useChoice;
    @FXML private ComboBox<String> inheritChoice;
    @FXML private Label createError;
    @FXML private Label useError;
    @FXML private Label inheritErrorName;
    @FXML private Label inheritErrorSelect;
    @FXML private VBox createBox;
    @FXML private VBox useBox;
    @FXML private VBox inheritBox;

    private List<Pair<LauncherManifest, LauncherModsDetails>> modsComponents;
    private Map<String, LauncherManifestType> typeConversion;
    private LauncherManifest modsManifest;
    private LauncherManifest gameManifest;
    private String gameVersion;
    private String modsType;

    public void setPrerequisites(List<Pair<LauncherManifest, LauncherModsDetails>> modsComponents, Map<String, LauncherManifestType> typeConversion, LauncherManifest savesManifest, LauncherManifest gameManifest) {
        this.modsComponents = modsComponents;
        this.typeConversion = typeConversion;
        this.modsManifest = savesManifest;
        this.gameManifest = gameManifest;
    }

    @FXML private void onRadioCreateSelect() {
        createBox.setDisable(false);
        useBox.setDisable(true);
        inheritBox.setDisable(true);
    }
    @FXML private void onRadioUseSelect() {
        createBox.setDisable(true);
        useBox.setDisable(false);
        inheritBox.setDisable(true);
    }
    @FXML private void onRadioInheritSelect() {
        createBox.setDisable(true);
        useBox.setDisable(true);
        inheritBox.setDisable(false);
    }

    @Override
    public void beforeShow(Stage stage) {
        radioCreate.fire();
        createBox.setDisable(false);
        createName.setText("");
        createName.getStyleClass().remove("error");
        createError.setVisible(false);
        useBox.setDisable(true);
        useChoice.getItems().clear();
        useChoice.getStyleClass().remove("error");
        useError.setVisible(false);
        inheritBox.setDisable(true);
        inheritName.setText("");
        inheritName.getStyleClass().remove("error");
        inheritErrorName.setVisible(false);
        inheritChoice.getItems().clear();
        inheritChoice.getStyleClass().remove("error");
        inheritErrorSelect.setVisible(false);
        for(Pair<LauncherManifest, LauncherModsDetails> manifest : modsComponents) {
            useChoice.getItems().add(manifest.getKey().getName());
            inheritChoice.getItems().add(manifest.getKey().getName());
        }
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public boolean create() {
        if(!checkCreateReady()) {
            LOGGER.warn("Not ready to create mods!");
            return false;
        }
        ModsCreator creator = getCreator();
        return creator.getId() != null;
    }

    public ModsCreator getCreator() {
        if(radioCreate.isSelected()) {
            return new ModsCreator(createName.getText(), typeConversion, modsManifest, modsType, gameVersion, gameManifest);
        } else if(radioUse.isSelected()) {
            Pair<LauncherManifest, LauncherModsDetails> manifest = getModsFromName(useChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find mods from name: " + useChoice.getSelectionModel().getSelectedItem() + "!");
                return null;
            }
            return new ModsCreator(manifest);
        } else if(radioInherit.isSelected()) {
            Pair<LauncherManifest, LauncherModsDetails> manifest = getModsFromName(inheritChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find mods from name: " + inheritChoice.getSelectionModel().getSelectedItem() + "!");
                return null;
            }
            return new ModsCreator(inheritName.getText(), manifest, modsManifest, gameManifest);
        } else {
            LOGGER.warn("No radio button selected!");
            return null;
        }
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
        createError.setVisible(false);
        createName.getStyleClass().remove("error");
        useError.setVisible(false);
        useChoice.getStyleClass().remove("error");
        inheritErrorName.setVisible(false);
        inheritName.getStyleClass().remove("error");
        inheritErrorSelect.setVisible(false);
        inheritChoice.getStyleClass().remove("error");
        if(show) {
            if(radioCreate.isSelected() && createName.getText().isBlank()) {
                createError.setVisible(true);
                createName.getStyleClass().add("error");
            } else if(radioUse.isSelected() && useChoice.getSelectionModel().isEmpty()) {
                useError.setVisible(true);
                useChoice.getStyleClass().add("error");
            } else if(radioInherit.isSelected()) {
                if(inheritName.getText().isBlank()) {
                    inheritErrorName.setVisible(true);
                    inheritName.getStyleClass().add("error");
                }
                if(inheritChoice.getSelectionModel().isEmpty()) {
                    inheritErrorSelect.setVisible(true);
                    inheritChoice.getStyleClass().add("error");
                }
            }
        }
    }

    public boolean checkCreateReady() {
        boolean result = (modsComponents != null && typeConversion != null && modsManifest != null && gameManifest != null && gameVersion != null && modsType != null && ((radioCreate.isSelected() && !createName.getText().isBlank()) || (radioUse.isSelected() && !useChoice.getSelectionModel().isEmpty()) || (radioInherit.isSelected() && !inheritName.getText().isBlank() && !inheritChoice.getSelectionModel().isEmpty())));
        return result;
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
