package net.treset.minecraftlauncher.ui.generic;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.ui.cellfactory.ManifestListCellFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ComponentChangerElement extends HBox {
    private final ComboBox<LauncherManifest> cbChange = new ComboBox<>();
    private final IconButton btChange = new IconButton();

    private List<LauncherManifest> components;
    private LauncherManifest currentComponent;
    private Consumer<LauncherManifest> changeCallback;
    private Supplier<Boolean> changeGetter;

    public ComponentChangerElement() {
        cbChange.setPromptText("There should be something here...");
        cbChange.getSelectionModel().selectedItemProperty().addListener(this::onSelectionChanged);
        cbChange.setCellFactory(new ManifestListCellFactory());
        cbChange.setButtonCell(new ManifestListCellFactory().call(null));
        setHgrow(cbChange, Priority.ALWAYS);

        btChange.getStyleClass().addAll("sync", "highlight");
        btChange.setIconSize(32);
        btChange.setTooltipText(LauncherApplication.stringLocalizer.get("selector.change.tooltip"));
        btChange.setOnAction(this::onChange);

        this.setPadding(new Insets(5, 15, 15, 15));
        this.setSpacing(5);
        this.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().addAll(cbChange, btChange);
    }

    public void init(List<LauncherManifest> components, LauncherManifest currentComponent, Consumer<LauncherManifest> changeCallback, Supplier<Boolean> changeGetter) {
        this.components = components;
        this.currentComponent = currentComponent;
        this.changeCallback = changeCallback;
        this.changeGetter = changeGetter;

        cbChange.getItems().clear();
        cbChange.getItems().addAll(components);
        cbChange.getSelectionModel().select(currentComponent);
    }

    private void onSelectionChanged(ObservableValue<? extends LauncherManifest> observable, LauncherManifest oldValue, LauncherManifest newValue) {
        btChange.setDisable(newValue == null || newValue.equals(currentComponent));
    }

    @FXML
    private void onChange(ActionEvent event) {
        if(changeGetter.get() && cbChange.getSelectionModel().getSelectedItem() != null && !cbChange.getSelectionModel().getSelectedItem().equals(currentComponent)) {
            currentComponent = cbChange.getSelectionModel().getSelectedItem();
            changeCallback.accept(currentComponent);
            btChange.setDisable(true);
        }
    }

    public LauncherManifest getCurrentComponent() {
        return currentComponent;
    }

    public void setCurrentComponent(LauncherManifest currentComponent) {
        this.currentComponent = currentComponent;
    }

    public List<LauncherManifest> getComponents() {
        return components;
    }

    public void setComponents(List<LauncherManifest> components) {
        this.components = components;
    }

    public Consumer<LauncherManifest> getChangeCallback() {
        return changeCallback;
    }

    public void setChangeCallback(Consumer<LauncherManifest> changeCallback) {
        this.changeCallback = changeCallback;
    }

    public Supplier<Boolean> getChangeGetter() {
        return changeGetter;
    }

    public void setChangeGetter(Supplier<Boolean> changeGetter) {
        this.changeGetter = changeGetter;
    }
}
