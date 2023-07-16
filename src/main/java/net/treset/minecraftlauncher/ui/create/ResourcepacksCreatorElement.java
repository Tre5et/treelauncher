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
import net.treset.minecraftlauncher.creation.ResourcepackCreator;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;

import java.util.List;
import java.util.Map;

public class ResourcepacksCreatorElement extends UiElement {

    @FXML
    private VBox rootPane;
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

    private List<LauncherManifest> resourcepacksComponents;
    private Map<String, LauncherManifestType> typeConversion;
    private LauncherManifest resourcepacksManifest;

    public void setPrerequisites(List<LauncherManifest> resourcepacksComponents, Map<String, LauncherManifestType> typeConversion, LauncherManifest resoucepacksManifest) {
        this.resourcepacksComponents = resourcepacksComponents;
        this.typeConversion = typeConversion;
        this.resourcepacksManifest = resoucepacksManifest;
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
        for(LauncherManifest manifest : resourcepacksComponents) {
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
            throw new ComponentCreationException("Not ready to create resourcepacks!");
        }
        ResourcepackCreator creator = getCreator();
        creator.getId();
    }

    public ResourcepackCreator getCreator() throws ComponentCreationException {
        if(!checkCreateReady()) {
            throw new ComponentCreationException("Not ready to create resourcepacks!");
        }
        if(rbCreate.isSelected()) {
            return new ResourcepackCreator(tfCreateName.getText(), typeConversion, resourcepacksManifest);
        } else if(rbUse.isSelected()) {
            LauncherManifest manifest = getResourcepacksFromName(cbUse.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                throw new ComponentCreationException("Could not find resourcepacks: name=" + cbUse.getSelectionModel().getSelectedItem());
            }
            return new ResourcepackCreator(manifest);
        } else if(rbInherit.isSelected()) {
            LauncherManifest manifest = getResourcepacksFromName(cbInherit.getSelectionModel().getSelectedItem());
            if(manifest == null) {
                throw new ComponentCreationException("Could not find resourcepacks: name=" + cbInherit.getSelectionModel().getSelectedItem());
            }
            return new ResourcepackCreator(tfInheritName.getText(), manifest, resourcepacksManifest);
        }
        throw new ComponentCreationException("No radio button selected!");
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

    public void enableUse(boolean enabled) {
        rbUse.setVisible(enabled);
        vbUse.setVisible(enabled);
    }

    public boolean checkCreateReady() {
        return (resourcepacksComponents != null && typeConversion != null && resourcepacksManifest != null && ((rbCreate.isSelected() && !tfCreateName.getText().isBlank()) || (rbUse.isSelected() && !cbUse.getSelectionModel().isEmpty()) || (rbInherit.isSelected() && !tfInheritName.getText().isBlank() && !cbInherit.getSelectionModel().isEmpty())));
    }
}
