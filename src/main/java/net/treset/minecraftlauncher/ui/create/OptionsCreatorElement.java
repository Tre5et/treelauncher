package net.treset.minecraftlauncher.ui.create;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.creation.OptionsCreator;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;

import java.util.List;
import java.util.Map;

public class OptionsCreatorElement extends UiElement {

    @FXML private VBox rootPane;
    @FXML private RadioButton rbCreate;
    @FXML private RadioButton rbUse;
    @FXML private RadioButton rbInherit;
    @FXML private TextField tfCreateName;
    @FXML private TextField tfInheritName;
    @FXML private ComboBox<String> cbUse;
    @FXML private ComboBox<String> cbInherit;
    @FXML private Label lbCreateError;
    @FXML private Label lbUseError;
    @FXML private Label lbInheritErrorName;
    @FXML private Label lbInheritErrorSelect;
    @FXML private VBox vbCreate;
    @FXML private VBox vbUse;
    @FXML private VBox vbInherit;

    private List<LauncherManifest> optionsComponents;
    private Map<String, LauncherManifestType> typeConversion;
    private LauncherManifest ortionsManifest;

    public void setPrerequisites(List<LauncherManifest> savesComponents, Map<String, LauncherManifestType> typeConversion, LauncherManifest savesManifest) {
        this.optionsComponents = savesComponents;
        this.typeConversion = typeConversion;
        this.ortionsManifest = savesManifest;
    }

    @FXML private void onRadioCreate() {
        vbCreate.setDisable(false);
        vbUse.setDisable(true);
        vbInherit.setDisable(true);
    }
    @FXML private void onRadioUse() {
        vbCreate.setDisable(true);
        vbUse.setDisable(false);
        vbInherit.setDisable(true);
    }
    @FXML private void onRadioInherit() {
        vbCreate.setDisable(true);
        vbUse.setDisable(true);
        vbInherit.setDisable(false);
    }

    @Override
    public void beforeShow(Stage stage) {
        rbCreate.fire();
        vbCreate.setDisable(false);
        tfCreateName.setText("");
        tfCreateName.getStyleClass().remove("error");
        lbCreateError.setVisible(false);
        vbUse.setDisable(true);
        cbUse.getItems().clear();
        cbUse.getStyleClass().remove("error");
        lbUseError.setVisible(false);
        vbInherit.setDisable(true);
        tfInheritName.setText("");
        tfInheritName.getStyleClass().remove("error");
        lbInheritErrorName.setVisible(false);
        cbInherit.getItems().clear();
        cbInherit.getStyleClass().remove("error");
        lbInheritErrorSelect.setVisible(false);
        for(LauncherManifest manifest : optionsComponents) {
            cbUse.getItems().add(manifest.getName());
            cbInherit.getItems().add(manifest.getName());
        }
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void create() throws ComponentCreationException {
        if(!checkCreateReady()) {
            throw new ComponentCreationException("Not ready to create options!");
        }
        OptionsCreator creator = getCreator();
        creator.getId();
    }

    public OptionsCreator getCreator() throws ComponentCreationException {
        if(!checkCreateReady()) {
            throw new ComponentCreationException("Not ready to create options!");
        }

        if(rbCreate.isSelected()) {
            return new OptionsCreator(tfCreateName.getText(), typeConversion, ortionsManifest);
        } else if(rbUse.isSelected()) {
            LauncherManifest manifest = getOptionsFromName(cbUse.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                throw new ComponentCreationException("Could not find options: name=" + cbUse.getSelectionModel().getSelectedItem());
            }
            return new OptionsCreator(manifest);
        } else if(rbInherit.isSelected()) {
            LauncherManifest manifest = getOptionsFromName(cbInherit.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                throw new ComponentCreationException("Could not find options: name=" + cbInherit.getSelectionModel().getSelectedItem());
            }
            return new OptionsCreator(tfInheritName.getText(), manifest, ortionsManifest);
        }
        throw new ComponentCreationException("No radio button selected!");
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
        lbCreateError.setVisible(false);
        tfCreateName.getStyleClass().remove("error");
        lbUseError.setVisible(false);
        cbUse.getStyleClass().remove("error");
        lbInheritErrorName.setVisible(false);
        tfInheritName.getStyleClass().remove("error");
        lbInheritErrorSelect.setVisible(false);
        cbInherit.getStyleClass().remove("error");
        if(show) {
            if(rbCreate.isSelected() && tfCreateName.getText().isBlank()) {
                lbCreateError.setVisible(true);
                tfCreateName.getStyleClass().add("error");
            } else if(rbUse.isSelected() && cbUse.getSelectionModel().isEmpty()) {
                lbUseError.setVisible(true);
                cbUse.getStyleClass().add("error");
            } else if(rbInherit.isSelected()) {
                if(tfInheritName.getText().isBlank()) {
                    lbInheritErrorName.setVisible(true);
                    tfInheritName.getStyleClass().add("error");
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

    public boolean checkCreateReady() {
        return (optionsComponents != null && typeConversion != null && ortionsManifest != null && ((rbCreate.isSelected() && !tfCreateName.getText().isBlank()) || (rbUse.isSelected() && !cbUse.getSelectionModel().isEmpty()) || (rbInherit.isSelected() && !tfInheritName.getText().isBlank() && !cbInherit.getSelectionModel().isEmpty())));
    }


}
