package net.treset.minecraftlauncher.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.ui.UiLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ListElementController {

    @FXML
    public GridPane element;
    @FXML
    public Label title;
    @FXML
    public Label details;

    private boolean selected = false;
    private InstanceData instanceData;
    private BiFunction<InstanceData, Boolean, Boolean> selectionAccepted;
    private List<BiConsumer<InstanceData, Boolean>> selectionListeners;

    @FXML
    public void onElementClicked() {
        if(selectionAccepted.apply(instanceData, !selected)) {
            if(!selected) {
                title.getStyleClass().add("selected");
                details.getStyleClass().add("selected");
            } else {
                title.getStyleClass().remove("selected");
                details.getStyleClass().remove("selected");
            }
            for (BiConsumer<InstanceData, Boolean> selectionListener : selectionListeners) {
                selectionListener.accept(instanceData, !selected);
            }
            selected = !selected;
        }
    }

    public boolean select(boolean select, boolean force, boolean callback) {
        if(selected != select && (force || selectionAccepted.apply(instanceData, select))) {
            if(select) {
                title.getStyleClass().add("selected");
                details.getStyleClass().add("selected");
            } else {
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

    public void beforeShow() {
        title.setText(instanceData.getInstance().getKey().getName());
        details.setText(instanceData.getVersionComponents().get(0).getValue().getVersionId());
    }

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

    public static Pair<ListElementController, GridPane> from(InstanceData instanceData) throws IOException {
        FXMLLoader loader = UiLoader.getFXMLLoader("ListElement");
        GridPane element = UiLoader.loadFXML(loader);
        ListElementController listElementController = loader.getController();
        listElementController.setInstanceData(instanceData);
        return new Pair<>(listElementController, element);
    }
}
