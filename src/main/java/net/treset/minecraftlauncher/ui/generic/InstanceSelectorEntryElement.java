package net.treset.minecraftlauncher.ui.generic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.UiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class InstanceSelectorEntryElement extends SelectorEntryElement {
    @FXML private Label lbTime;

    private BiFunction<InstanceData, Boolean, Boolean> selectionInstanceAcceptor;
    private List<BiConsumer<InstanceData, Boolean>> selectionInstanceListeners;
    private InstanceData instanceData;

    @Override
    public boolean select(boolean select, boolean force, boolean callback) {
        if(instanceData != null && selectionInstanceAcceptor.apply(instanceData, select)) {
            if(select) {
                entryContainer.getStyleClass().add("selected");
                lbTitle.getStyleClass().add("selected");
                lbDetails.getStyleClass().add("selected");
            } else {
                entryContainer.getStyleClass().remove("selected");
                lbTitle.getStyleClass().remove("selected");
                lbDetails.getStyleClass().remove("selected");
            }

            if(callback) {
                for (BiConsumer<InstanceData, Boolean> selectionListener : selectionInstanceListeners) {
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
        lbTitle.setText(instanceData.getInstance().getKey().getName());
        lbDetails.setText(instanceData.getVersionComponents().get(0).getValue().getVersionId());
        setTime(instanceData.getInstance().getValue().getTotalTime());
    }

    public void updateTime() {
        setTime(instanceData.getInstance().getValue().getTotalTime());
    }

    public void setTime(long seconds) {
        lbTime.setText(FormatUtil.formatSeconds(seconds));
    }

    public BiFunction<InstanceData, Boolean, Boolean> getSelectionInstanceAcceptor() {
        return selectionInstanceAcceptor;
    }

    public void setSelectionInstanceAcceptor(BiFunction<InstanceData, Boolean, Boolean> selectionInstanceAcceptor) {
        this.selectionInstanceAcceptor = selectionInstanceAcceptor;
    }

    public List<BiConsumer<InstanceData, Boolean>> getSelectionInstanceListeners() {
        return selectionInstanceListeners;
    }

    public void setSelectionInstanceListeners(List<BiConsumer<InstanceData, Boolean>> selectionInstanceListeners) {
        this.selectionInstanceListeners = selectionInstanceListeners;
    }

    public void addSelectionListener(BiConsumer<InstanceData, Boolean> selectionListener) {
        this.selectionInstanceListeners = new ArrayList<>(selectionInstanceListeners);
        this.selectionInstanceListeners.add(selectionListener);
    }

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
    }

    public static Pair<InstanceSelectorEntryElement, AnchorPane> from(InstanceData instanceData) throws IOException {
        Pair<InstanceSelectorEntryElement, AnchorPane> result = newInstance();
        result.getKey().setInstanceData(instanceData);
        return result;
    }

    private static Pair<InstanceSelectorEntryElement, AnchorPane> newInstance() throws IOException {
        FXMLLoader loader = UiUtil.getFXMLLoader("generic/InstanceSelectorEntryElement");
        AnchorPane element = UiUtil.loadFXML(loader);
        InstanceSelectorEntryElement listElementController = loader.getController();
        return new Pair<>(listElementController, element);
    }
}
