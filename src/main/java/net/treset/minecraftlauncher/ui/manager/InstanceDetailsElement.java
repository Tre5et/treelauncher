package net.treset.minecraftlauncher.ui.manager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.ui.base.UiElement;

import java.util.function.BiConsumer;

public class InstanceDetailsElement extends UiElement {
    public enum SelectedType {
        VERSION,
        SAVES,
        RESOURCEPACKS,
        OPTIONS,
        MODS
    }

    @FXML public VBox container;
    @FXML public Label versionName;
    @FXML public Label savesName;
    @FXML public Label resourcepacksName;
    @FXML public Label optionsName;
    @FXML public Label modsName;
    @FXML private GridPane versionContainer;
    @FXML private GridPane savesContainer;
    @FXML private GridPane resourcepacksContainer;
    @FXML private GridPane optionsContainer;
    @FXML private GridPane modsContainer;

    private BiConsumer<Boolean, SelectedType> selectionCallback;
    private SelectedType currentSelected = null;

    public void init(BiConsumer<Boolean, SelectedType> selectionCallback) {
        this.selectionCallback = selectionCallback;
    }

    @Override
    public void setRootVisible(boolean visible) {
        container.setVisible(visible);
    }

    public void populate(InstanceData instanceData) {
        versionName.setText(instanceData.getVersionComponents().get(0).getKey().getName());
        savesName.setText(instanceData.getSavesComponent().getName());
        resourcepacksName.setText(instanceData.getResourcepacksComponent().getName());
        optionsName.setText(instanceData.getOptionsComponent().getName());
        if(instanceData.getModsComponent() != null) {
            modsContainer.setVisible(true);
            modsName.setText(instanceData.getModsComponent().getKey().getName());
        } else {
            modsContainer.setVisible(false);
        }
    }

    @FXML
    private void onVersionClicked() {
        unselectAll();
        if(currentSelected == SelectedType.VERSION) {
            selectionCallback.accept(false, SelectedType.VERSION);
            currentSelected = null;
        } else {
            versionContainer.getStyleClass().add("selected");
            currentSelected = SelectedType.VERSION;
            selectionCallback.accept(true, SelectedType.VERSION);
        }
    }

    @FXML
    private void onSavesClicked() {
        unselectAll();
        if(currentSelected == SelectedType.SAVES) {
            selectionCallback.accept(false, SelectedType.SAVES);
            currentSelected = null;
        } else {
            savesContainer.getStyleClass().add("selected");
            currentSelected = SelectedType.SAVES;
            selectionCallback.accept(true, SelectedType.SAVES);
        }
    }

    @FXML
    private void onResourcepacksClicked() {
        unselectAll();
        if(currentSelected == SelectedType.RESOURCEPACKS) {
            selectionCallback.accept(false, SelectedType.RESOURCEPACKS);
            currentSelected = null;
        } else {
            resourcepacksContainer.getStyleClass().add("selected");
            currentSelected = SelectedType.RESOURCEPACKS;
            selectionCallback.accept(true, SelectedType.RESOURCEPACKS);
        }
    }

    @FXML
    private void onOptionsClicked() {
        unselectAll();
        if(currentSelected == SelectedType.OPTIONS) {
            selectionCallback.accept(false, SelectedType.OPTIONS);
            currentSelected = null;
        } else {
            optionsContainer.getStyleClass().add("selected");
            currentSelected = SelectedType.OPTIONS;
            selectionCallback.accept(true, SelectedType.OPTIONS);
        }
    }

    @FXML
    private void onModsClicked() {
        unselectAll();
        if(currentSelected == SelectedType.MODS) {
            selectionCallback.accept(false, SelectedType.MODS);
            currentSelected = null;
        } else {
            modsContainer.getStyleClass().add("selected");
            currentSelected = SelectedType.MODS;
            selectionCallback.accept(true, SelectedType.MODS);
        }
    }

    private void unselectAll() {
        versionContainer.getStyleClass().remove("selected");
        savesContainer.getStyleClass().remove("selected");
        resourcepacksContainer.getStyleClass().remove("selected");
        optionsContainer.getStyleClass().remove("selected");
        modsContainer.getStyleClass().remove("selected");
    }

    public void clearSelection() {
        unselectAll();
        currentSelected = null;
    }

    public SelectedType getCurrentSelected() {
        return currentSelected;
    }

    @Override
    public void beforeShow(Stage stage) {}

    @Override
    public void afterShow(Stage stage) {}
}
