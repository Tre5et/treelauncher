package net.treset.minecraftlauncher.ui.create;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.creation.ResourcepackCreator;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class ResourcepacksCreatorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ResourcepacksCreatorElement.class);

    @FXML
    private VBox rootPane;
    @FXML private RadioButton radioCreate;
    @FXML private RadioButton radioUse;
    @FXML private RadioButton radioInherit;
    @FXML private Label createLabel;
    @FXML private Label inheritLabel;
    @FXML private TextField createName;
    @FXML private TextField inheritName;
    @FXML private ChoiceBox<String> useChoice;
    @FXML private ChoiceBox<String> inheritChoice;
    @FXML private Label createError;
    @FXML private Label useError;
    @FXML private Label inheritErrorName;
    @FXML private Label inheritErrorSelect;
    @FXML private VBox createBox;
    @FXML private VBox useBox;
    @FXML private VBox inheritBox;

    private List<LauncherManifest> resourcepacksComponents;
    private Map<String, LauncherManifestType> typeConversion;
    private LauncherManifest resourcepacksManifest;

    public void setPrerequisites(List<LauncherManifest> savesComponents, Map<String, LauncherManifestType> typeConversion, LauncherManifest savesManifest) {
        this.resourcepacksComponents = savesComponents;
        this.typeConversion = typeConversion;
        this.resourcepacksManifest = savesManifest;
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
        for(LauncherManifest manifest : resourcepacksComponents) {
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
            LOGGER.warn("Not ready to create resourcepacks!");
            return false;
        }
        ResourcepackCreator creator = getCreator();
        if(creator == null) {
            LOGGER.warn("Could not create resourcepacks!");
            return false;
        }
        return creator.getId() != null;
    }

    public ResourcepackCreator getCreator() {
        if(!checkCreateReady()) {
            LOGGER.warn("Not ready to create resourcepacks!");
            return null;
        }
        if(radioCreate.isSelected()) {
            return new ResourcepackCreator(createName.getText(), typeConversion, resourcepacksManifest);
        } else if(radioUse.isSelected()) {
            LauncherManifest manifest = getResourcepacksFromName(useChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find resourcepacks from name: " + useChoice.getSelectionModel().getSelectedItem() + "!");
                return null;
            }
            return new ResourcepackCreator(manifest);
        } else if(radioInherit.isSelected()) {
            LauncherManifest manifest = getResourcepacksFromName(inheritChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find resourcepacks from name: " + inheritChoice.getSelectionModel().getSelectedItem() + "!");
                return null;
            }
            return new ResourcepackCreator(inheritName.getText(), manifest, resourcepacksManifest);
        }
        LOGGER.warn("No radio button selected!");
        return null;
    }

    private LauncherManifest getResourcepacksFromName(String name) {
        for(LauncherManifest manifest : resourcepacksComponents) {
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
        boolean result = (resourcepacksComponents != null && typeConversion != null && resourcepacksManifest != null && ((radioCreate.isSelected() && !createName.getText().isBlank()) || (radioUse.isSelected() && !useChoice.getSelectionModel().isEmpty()) || (radioInherit.isSelected() && !inheritName.getText().isBlank() && !inheritChoice.getSelectionModel().isEmpty())));
        return result;
    }
}
