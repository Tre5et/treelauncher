package net.treset.minecraftlauncher.ui.manager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.ui.base.UiElement;

import java.util.function.BiConsumer;

public class InstanceManagerElement extends UiElement {
    public enum SelectedType {
        VERSION,
        SAVES,
        RESOURCEPACKS,
        OPTIONS,
        MODS
    }

    @FXML public VBox container;
    @FXML public Label lbVersion;
    @FXML public Label lbSaves;
    @FXML public Label lbResourcepacks;
    @FXML public Label lbOptions;
    @FXML public Label lbMods;
    @FXML private GridPane gpVersion;
    @FXML private GridPane gpSaves;
    @FXML private GridPane gpResourcepacks;
    @FXML private GridPane gpOptions;
    @FXML private GridPane gpMods;

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
        lbVersion.setText(instanceData.getVersionComponents().get(0).getKey().getName());
        lbSaves.setText(instanceData.getSavesComponent().getName());
        lbResourcepacks.setText(instanceData.getResourcepacksComponent().getName());
        lbOptions.setText(instanceData.getOptionsComponent().getName());
        if(instanceData.getModsComponent() != null) {
            gpMods.setVisible(true);
            lbMods.setText(instanceData.getModsComponent().getKey().getName());
        } else {
            gpMods.setVisible(false);
        }
    }

    @FXML
    private void onSelectVersion() {
        unselectAll();
        if(currentSelected == SelectedType.VERSION) {
            selectionCallback.accept(false, SelectedType.VERSION);
            currentSelected = null;
        } else {
            gpVersion.getStyleClass().add("selected");
            currentSelected = SelectedType.VERSION;
            selectionCallback.accept(true, SelectedType.VERSION);
        }
    }

    @FXML
    private void onSelectSaves() {
        unselectAll();
        if(currentSelected == SelectedType.SAVES) {
            selectionCallback.accept(false, SelectedType.SAVES);
            currentSelected = null;
        } else {
            gpSaves.getStyleClass().add("selected");
            currentSelected = SelectedType.SAVES;
            selectionCallback.accept(true, SelectedType.SAVES);
        }
    }

    @FXML
    private void onSelectResourcepacks() {
        unselectAll();
        if(currentSelected == SelectedType.RESOURCEPACKS) {
            selectionCallback.accept(false, SelectedType.RESOURCEPACKS);
            currentSelected = null;
        } else {
            gpResourcepacks.getStyleClass().add("selected");
            currentSelected = SelectedType.RESOURCEPACKS;
            selectionCallback.accept(true, SelectedType.RESOURCEPACKS);
        }
    }

    @FXML
    private void onSelectOptions() {
        unselectAll();
        if(currentSelected == SelectedType.OPTIONS) {
            selectionCallback.accept(false, SelectedType.OPTIONS);
            currentSelected = null;
        } else {
            gpOptions.getStyleClass().add("selected");
            currentSelected = SelectedType.OPTIONS;
            selectionCallback.accept(true, SelectedType.OPTIONS);
        }
    }

    @FXML
    private void onSelectMods() {
        unselectAll();
        if(currentSelected == SelectedType.MODS) {
            selectionCallback.accept(false, SelectedType.MODS);
            currentSelected = null;
        } else {
            gpMods.getStyleClass().add("selected");
            currentSelected = SelectedType.MODS;
            selectionCallback.accept(true, SelectedType.MODS);
        }
    }

    private void unselectAll() {
        gpVersion.getStyleClass().remove("selected");
        gpSaves.getStyleClass().remove("selected");
        gpResourcepacks.getStyleClass().remove("selected");
        gpOptions.getStyleClass().remove("selected");
        gpMods.getStyleClass().remove("selected");
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
