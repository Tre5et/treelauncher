package net.treset.minecraftlauncher.ui.manager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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
    @FXML private HBox hbVersion;
    @FXML private HBox hbSaves;
    @FXML private HBox hbResourcepacks;
    @FXML private HBox hbOptions;
    @FXML private HBox hbMods;

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
            hbMods.setVisible(true);
            lbMods.setText(instanceData.getModsComponent().getKey().getName());
        } else {
            hbMods.setVisible(false);
        }
    }

    @FXML
    private void onSelectVersion() {
        unselectAll();
        if(currentSelected == SelectedType.VERSION) {
            selectionCallback.accept(false, SelectedType.VERSION);
            currentSelected = null;
        } else {
            hbVersion.getStyleClass().add("selected");
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
            hbSaves.getStyleClass().add("selected");
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
            hbResourcepacks.getStyleClass().add("selected");
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
            hbOptions.getStyleClass().add("selected");
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
            hbMods.getStyleClass().add("selected");
            currentSelected = SelectedType.MODS;
            selectionCallback.accept(true, SelectedType.MODS);
        }
    }

    private void unselectAll() {
        hbVersion.getStyleClass().remove("selected");
        hbSaves.getStyleClass().remove("selected");
        hbResourcepacks.getStyleClass().remove("selected");
        hbOptions.getStyleClass().remove("selected");
        hbMods.getStyleClass().remove("selected");
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
