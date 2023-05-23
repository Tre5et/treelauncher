package net.treset.minecraftlauncher.ui.generic;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiElement;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ComponentChangerElement extends UiElement {
    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<String> changeBox;
    @FXML private Button changeButton;

    private List<LauncherManifest> components;
    private LauncherManifest currentComponent;
    private Consumer<LauncherManifest> changeCallback;
    private Supplier<Boolean> changeGetter;

    public void init(List<LauncherManifest> components, LauncherManifest currentComponent, Consumer<LauncherManifest> changeCallback, Supplier<Boolean> changeGetter) {
        this.components = components;
        this.currentComponent = currentComponent;
        this.changeCallback = changeCallback;
        this.changeGetter = changeGetter;
        changeBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onSelectionChanged);
        });
    }

    private void onSelectionChanged() {
        changeButton.setDisable(changeBox.getSelectionModel().getSelectedItem() == null || changeBox.getSelectionModel().getSelectedItem().equals(currentComponent.getName()));
    }

    @FXML
    private void onChangeButtonClicked() {
        if(changeGetter.get() && changeBox.getSelectionModel().getSelectedItem() != null && !changeBox.getSelectionModel().getSelectedItem().equals(currentComponent.getName())) {
            currentComponent = components.stream().filter(manifest -> manifest.getName().equals(changeBox.getSelectionModel().getSelectedItem())).findFirst().orElse(null);
            changeCallback.accept(currentComponent);
            onSelectionChanged();
        }
    }


    @Override
    public void beforeShow(Stage stage) {
        changeBox.getItems().clear();
        changeBox.getItems().addAll(components.stream().map(LauncherManifest::getName).toList());
        changeBox.getSelectionModel().select(currentComponent.getName());
    }

    @Override
    public void afterShow(Stage stage) {
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
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
