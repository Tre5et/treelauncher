package net.treset.minecraftlauncher.ui.create;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.creation.OptionsCreator;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class OptionsCreatorElement extends UiElement {
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

    private List<LauncherManifest> optionsComponents;
    private Map<String, LauncherManifestType> typeConversion;
    private LauncherManifest ortionsManifest;

    public void setPrerequisites(List<LauncherManifest> savesComponents, Map<String, LauncherManifestType> typeConversion, LauncherManifest savesManifest) {
        this.optionsComponents = savesComponents;
        this.typeConversion = typeConversion;
        this.ortionsManifest = savesManifest;
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
        for(LauncherManifest manifest : optionsComponents) {
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
            LOGGER.warn("Not ready to create options!");
            return false;
        }
        OptionsCreator creator = getCreator();
        return creator.getId() != null;
    }

    public OptionsCreator getCreator() {
        if(!checkCreateReady()) {
            LOGGER.warn("Not ready to create options!");
            return null;
        }

        if(radioCreate.isSelected()) {
            return new OptionsCreator(createName.getText(), typeConversion, ortionsManifest);
        } else if(radioUse.isSelected()) {
            LauncherManifest manifest = getOptionsFromName(useChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find options from name: " + useChoice.getSelectionModel().getSelectedItem() + "!");
                return null;
            }
            return new OptionsCreator(manifest);
        } else if(radioInherit.isSelected()) {
            LauncherManifest manifest = getOptionsFromName(inheritChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find options from name: " + inheritChoice.getSelectionModel().getSelectedItem() + "!");
                return null;
            }
            return new OptionsCreator(inheritName.getText(), manifest, ortionsManifest);
        }
        LOGGER.warn("No radio button selected!");
        return null;
    }

    private LauncherManifest getOptionsFromName(String name) {
        for(LauncherManifest manifest : optionsComponents) {
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

    public void enableUse(boolean enable) {
        radioUse.setVisible(enable);
        useBox.setVisible(enable);
    }

    public boolean checkCreateReady() {
        boolean result = (optionsComponents != null && typeConversion != null && ortionsManifest != null && ((radioCreate.isSelected() && !createName.getText().isBlank()) || (radioUse.isSelected() && !useChoice.getSelectionModel().isEmpty()) || (radioInherit.isSelected() && !inheritName.getText().isBlank() && !inheritChoice.getSelectionModel().isEmpty())));
        return result;
    }


}
