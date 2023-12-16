package net.treset.minecraftlauncher.ui.create;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.treelauncher.backend.creation.ComponentCreator;
import net.treset.minecraftlauncher.ui.generic.ErrorWrapper;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.ui.cellfactory.ManifestListCellFactory;

import java.util.List;
import java.util.Map;

public abstract class CreatorElement extends VBox {
    protected final RadioButton rbCreate = new RadioButton();
    protected final TextField tfCreate = new TextField();
    protected final ErrorWrapper ewCreate = new ErrorWrapper();
    protected final RadioButton rbInherit = new RadioButton();
    protected final TextField tfInherit = new TextField();
    protected final ErrorWrapper ewInherit = new ErrorWrapper();
    protected final ComboBox<LauncherManifest> cbInherit = new ComboBox<>();
    protected final ErrorWrapper ewInheritSelect = new ErrorWrapper();
    protected final RadioButton rbUse = new RadioButton();
    protected final ComboBox<LauncherManifest> cbUse = new ComboBox<>();
    protected final ErrorWrapper ewUse = new ErrorWrapper();

    protected List<LauncherManifest> components;
    protected Map<String, LauncherManifestType> typeConversion;
    protected LauncherManifest topManifest;

    protected boolean allowUse = true;

    public CreatorElement() {
        super();

        ToggleGroup tg = new ToggleGroup();

        rbCreate.setToggleGroup(tg);
        rbCreate.setText(LauncherApplication.stringLocalizer.get("creator.radio.create"));
        rbCreate.setOnAction(this::onRadioCreate);

        tfCreate.setPromptText(LauncherApplication.stringLocalizer.get("creator.prompt.name"));
        tfCreate.textProperty().addListener((observable, oldValue, newValue) -> ewCreate.showError(false));

        ewCreate.setErrorMessage("creator.label.error.name");
        ewCreate.getChildren().add(tfCreate);

        rbInherit.setToggleGroup(tg);
        rbInherit.setText(LauncherApplication.stringLocalizer.get("creator.radio.inherit"));
        rbInherit.setOnAction(this::onRadioInherit);

        tfInherit.setPromptText(LauncherApplication.stringLocalizer.get("creator.prompt.name"));
        tfInherit.textProperty().addListener((observable, oldValue, newValue) -> ewInherit.showError(false));

        ewInherit.setErrorMessage("creator.label.error.name");
        ewInherit.getChildren().add(tfInherit);

        cbInherit.setPromptText(LauncherApplication.stringLocalizer.get("creator.prompt.component"));
        cbInherit.valueProperty().addListener((observable, oldValue, newValue) -> ewInheritSelect.showError(false));
        cbInherit.setCellFactory(new ManifestListCellFactory());
        cbInherit.setButtonCell(new ManifestListCellFactory().call(null));

        ewInheritSelect.setErrorMessage("creator.label.error.select");
        ewInheritSelect.getChildren().add(cbInherit);

        rbUse.setToggleGroup(tg);
        rbUse.setText(LauncherApplication.stringLocalizer.get("creator.radio.use"));
        rbUse.setOnAction(this::onRadioUse);

        cbUse.setPromptText(LauncherApplication.stringLocalizer.get("creator.prompt.component"));
        cbUse.valueProperty().addListener((observable, oldValue, newValue) -> ewUse.showError(false));
        cbUse.setCellFactory(new ManifestListCellFactory());
        cbUse.setButtonCell(new ManifestListCellFactory().call(null));

        ewUse.setErrorMessage("creator.label.error.select");
        ewUse.getChildren().add(cbUse);

        this.setSpacing(5);
        this.setPadding(new Insets(10));
        this.getChildren().addAll(rbCreate, ewCreate, rbInherit, ewInherit, ewInheritSelect, rbUse, ewUse);

        rbCreate.fire();
    }

    public void init(List<LauncherManifest> components, Map<String, LauncherManifestType> typeConversion, LauncherManifest topManifest) {
        this.components = components;
        this.typeConversion = typeConversion;
        this.topManifest = topManifest;

        cbInherit.getItems().clear();
        cbUse.getItems().clear();

        for(LauncherManifest manifest : components) {
            cbInherit.getItems().add(manifest);
            cbUse.getItems().add(manifest);
        }

        clear();
    }

    public boolean isCreateReady() {
        return rbCreate.isSelected() && !tfCreate.getText().isEmpty()
                || rbInherit.isSelected() && !tfInherit.getText().isEmpty() && cbInherit.getValue() != null
                || rbUse.isSelected() && cbUse.getValue() != null;
    }

    public void showError(boolean show) {
        ewCreate.showError(false);
        ewInherit.showError(false);
        ewInheritSelect.showError(false);
        ewUse.showError(false);
        if(show) {
            if(rbCreate.isSelected() && tfCreate.getText().isEmpty()) {
                ewCreate.showError(true);
            } else if(rbInherit.isSelected()) {
                if(tfInherit.getText().isEmpty()) {
                    ewInherit.showError(true);
                }
                if(cbInherit.getValue() == null) {
                    ewInheritSelect.showError(true);
                }
            } else if(rbUse.isSelected() && cbUse.getValue() == null) {
                ewUse.showError(true);
            }
        }
    }

    public void create() throws ComponentCreationException {
        if(!isCreateReady()) {
            throw new ComponentCreationException("Not ready to create component!");
        }
        ComponentCreator creator = getCreator();
        creator.getId();
    }

    public void clear() {
        tfCreate.setText("");
        tfInherit.setText("");
        cbInherit.getSelectionModel().clearSelection();
        cbUse.getSelectionModel().clearSelection();
        rbCreate.fire();
    }

    public abstract ComponentCreator getCreator();

    protected void onRadioCreate(ActionEvent event) {
        deselectAll();
        ewCreate.setDisable(false);
    }

    protected void onRadioInherit(ActionEvent event) {
        deselectAll();
        ewInherit.setDisable(false);
        ewInheritSelect.setDisable(false);
    }

    protected void onRadioUse(ActionEvent event) {
        deselectAll();
        ewUse.setDisable(false);
    }

    protected void deselectAll() {
        ewCreate.setDisable(true);
        ewCreate.showError(false);
        ewInherit.setDisable(true);
        ewInherit.showError(false);
        ewInheritSelect.setDisable(true);
        ewInheritSelect.showError(false);
        ewUse.setDisable(true);
        ewUse.showError(false);
    }

    public boolean isAllowUse() {
        return allowUse;
    }

    public void setAllowUse(boolean allowUse) {
        if(this.allowUse == allowUse) return;
        this.allowUse = allowUse;
        if(allowUse) {
            this.getChildren().addAll(rbUse, ewUse);
        } else {
            this.getChildren().removeAll(rbUse, ewUse);
        }
    }
}
