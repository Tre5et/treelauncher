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
    @FXML private Label createLabel;
    @FXML private Label inheritLabel;
    @FXML private TextField createName;
    @FXML private TextField inheritName;
    @FXML private ChoiceBox<String> useChoice;
    @FXML private ChoiceBox<String> inheritChoice;

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
        createLabel.setVisible(true);
        createName.setVisible(true);
        useChoice.setVisible(false);
        inheritLabel.setVisible(false);
        inheritName.setVisible(false);
        inheritChoice.setVisible(false);
    }
    @FXML private void onRadioUseSelect() {
        createLabel.setVisible(false);
        createName.setVisible(false);
        useChoice.setVisible(true);
        inheritLabel.setVisible(false);
        inheritName.setVisible(false);
        inheritChoice.setVisible(false);
    }
    @FXML private void onRadioInheritSelect() {
        createLabel.setVisible(false);
        createName.setVisible(false);
        useChoice.setVisible(false);
        inheritLabel.setVisible(true);
        inheritName.setVisible(true);
        inheritChoice.setVisible(true);
    }

    @Override
    public void beforeShow(Stage stage) {
        radioCreate.fire();
        createLabel.setVisible(true);
        createName.setVisible(true);
        useChoice.setVisible(false);
        inheritLabel.setVisible(false);
        inheritName.setVisible(false);
        inheritChoice.setVisible(false);
        useChoice.getItems().clear();
        inheritChoice.getItems().clear();
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
        SavesCreator creator;
        if(radioCreate.isSelected()) {
            creator = new SavesCreator(createName.getText(), typeConversion, savesManifest, gameManifest);
        } else if(radioUse.isSelected()) {
            LauncherManifest manifest = getSaveFromName(useChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find save from name: " + useChoice.getSelectionModel().getSelectedItem() + "!");
                return false;
            }
            creator = new SavesCreator(manifest);
        } else if(radioInherit.isSelected()) {
            LauncherManifest manifest = getSaveFromName(inheritChoice.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                LOGGER.warn("Could not find save from name: " + inheritChoice.getSelectionModel().getSelectedItem() + "!");
                return false;
            }
            creator = new SavesCreator(inheritName.getText(), manifest, savesManifest, gameManifest);
        } else {
            LOGGER.warn("No radio button selected!");
            return false;
        }
        return creator.getId() != null;
    }

    private LauncherManifest getSaveFromName(String name) {
        for(LauncherManifest manifest : savesComponents) {
            if(name.equals(manifest.getName())) {
                return manifest;
            }
        }
        return null;
    }

    public boolean checkCreateReady() {
        boolean result = (savesComponents != null && typeConversion != null && savesManifest != null && gameManifest != null && ((radioCreate.isSelected() && !createName.getText().isBlank()) || (radioUse.isSelected() && !useChoice.getSelectionModel().isEmpty()) || (radioInherit.isSelected() && !inheritName.getText().isBlank() && !inheritChoice.getSelectionModel().isEmpty())));
        return result;
    }
}
