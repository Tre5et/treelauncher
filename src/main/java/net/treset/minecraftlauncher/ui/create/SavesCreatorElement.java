package net.treset.minecraftlauncher.ui.create;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.creation.SavesCreator;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class SavesCreatorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(SavesCreatorElement.class);

    @FXML private VBox rootPane;
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

    private List<LauncherManifest> savesComponents;
    private Map<String, LauncherManifestType> typeConversion;
    private LauncherManifest savesManifest;
    private LauncherManifest gameManifest;

    public void setPrerequisites(List<LauncherManifest> savesComponents, Map<String, LauncherManifestType> typeConversion, LauncherManifest savesManifest, LauncherManifest gameManifest) {
        this.savesComponents = savesComponents;
        this.typeConversion = typeConversion;
        this.savesManifest = savesManifest;
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
        for(LauncherManifest manifest : savesComponents) {
            useChoice.getItems().add(manifest.getName());
            inheritChoice.getItems().add(manifest.getName());
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
            LOGGER.warn("Not ready to create saves!");
            return false;
        }
        SavesCreator creator = getCreator();
        if(creator == null) {
            LOGGER.warn("Could not create saves!");
            return false;
        }
        return creator.getId() != null;
    }

    public SavesCreator getCreator() {
        if(!checkCreateReady()) {
            LOGGER.warn("Not ready to create saves!");
            return null;
        }
        if(radioCreate.isSelected()) {
            return new SavesCreator(createName.getText(), typeConversion, savesManifest, gameManifest);
        } else if(radioUse.isSelected()) {
            LauncherManifest manifest = getSaveFromName(useChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find save from name: " + useChoice.getSelectionModel().getSelectedItem() + "!");
                return null;
            }
            return new SavesCreator(manifest);
        } else if(radioInherit.isSelected()) {
            LauncherManifest manifest = getSaveFromName(inheritChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find save from name: " + inheritChoice.getSelectionModel().getSelectedItem() + "!");
                return null;
            }
            return new SavesCreator(inheritName.getText(), manifest, savesManifest, gameManifest);
        }
        LOGGER.warn("No radio button selected!");
        return null;
    }

    private LauncherManifest getSaveFromName(String name) {
        for(LauncherManifest manifest : savesComponents) {
            if(name.equals(manifest.getName())) {
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
        boolean result = (savesComponents != null && typeConversion != null && savesManifest != null && gameManifest != null && ((radioCreate.isSelected() && !createName.getText().isBlank()) || (radioUse.isSelected() && !useChoice.getSelectionModel().isEmpty()) || (radioInherit.isSelected() && !inheritName.getText().isBlank() && !inheritChoice.getSelectionModel().isEmpty())));
        return result;
    }
}
