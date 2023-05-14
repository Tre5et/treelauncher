package net.treset.minecraftlauncher.ui.generic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.UiLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SelectorEntryElement extends UiElement {

    @FXML private AnchorPane entryRoot;
    @FXML private GridPane entryContainer;
    @FXML private Label title;
    @FXML private Label details;

    private boolean selected = false;
    private InstanceData instanceData;
    private BiFunction<InstanceData, Boolean, Boolean> selectionAccepted;
    private List<BiConsumer<InstanceData, Boolean>> selectionListeners;

    @FXML
    public void onElementClicked() {
        select(!selected, false, true);
    }

    public boolean select(boolean select, boolean force, boolean callback) {
        if(selected != select && (force || selectionAccepted.apply(instanceData, select))) {
            if(select) {
                entryContainer.getStyleClass().add("selected");
                title.getStyleClass().add("selected");
                details.getStyleClass().add("selected");
            } else {
                entryContainer.getStyleClass().remove("selected");
                title.getStyleClass().remove("selected");
                details.getStyleClass().remove("selected");
            }
            if(callback) {
                for (BiConsumer<InstanceData, Boolean> selectionListener : selectionListeners) {
                    selectionListener.accept(instanceData, select);
                }
            }
            selected = select;
            return true;
        }
        return false;
    }

    @Override
    public void beforeShow(Stage stage) {
        title.setText(instanceData.getInstance().getKey().getName());
        details.setText(instanceData.getVersionComponents().get(0).getValue().getVersionId());
    }

    @Override
    public void afterShow(Stage stage) {}

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
    }

    public boolean isSelected() {
        return selected;
    }

    public BiFunction<InstanceData, Boolean, Boolean> getSelectionAccepted() {
        return selectionAccepted;
    }

    public void setSelectionAccepted(BiFunction<InstanceData, Boolean, Boolean> selectionAccepted) {
        this.selectionAccepted = selectionAccepted;
    }

    public List<BiConsumer<InstanceData, Boolean>> getSelectionListeners() {
        return selectionListeners;
    }

    public void setSelectionListeners(List<BiConsumer<InstanceData, Boolean>> selectionListeners) {
        this.selectionListeners = selectionListeners;
    }

    public void addSelectionListener(BiConsumer<InstanceData, Boolean> selectionListener) {
        this.selectionListeners = new ArrayList<>(selectionListeners);
        this.selectionListeners.add(selectionListener);
    }

    public static Pair<SelectorEntryElement, AnchorPane> from(InstanceData instanceData) throws IOException {
        FXMLLoader loader = UiLoader.getFXMLLoader("generic/SelectorEntryElement");
        AnchorPane element = UiLoader.loadFXML(loader);
        SelectorEntryElement listElementController = loader.getController();
        listElementController.setInstanceData(instanceData);
        return new Pair<>(listElementController, element);
    }

    @Override
    public void setRootVisible(boolean visible) {
        entryRoot.setVisible(visible);
    }
}
