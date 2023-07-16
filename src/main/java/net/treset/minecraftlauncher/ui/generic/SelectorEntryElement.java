package net.treset.minecraftlauncher.ui.generic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
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
    @FXML private Label lbTitle;
    @FXML private Label lbDetails;

    private boolean selected = false;
    private InstanceData instanceData;
    private LauncherManifest manifest;
    private BiFunction<InstanceData, Boolean, Boolean> selectionInstanceAcceptor;
    private BiFunction<LauncherManifest, Boolean, Boolean> selectionManifestAcceptor;
    private List<BiConsumer<InstanceData, Boolean>> selectionInstanceListeners;
    private List<BiConsumer<LauncherManifest, Boolean>> selectionManifestListener;

    @FXML
    public void onClick() {
        select(!selected, false, true);
    }

    public boolean select(boolean select, boolean force, boolean callback) {
        if(selected != select && (force || (instanceData != null && selectionInstanceAcceptor.apply(instanceData, select)) || (manifest != null && selectionManifestAcceptor.apply(manifest, select)))) {
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
                if(instanceData != null) {
                    for (BiConsumer<InstanceData, Boolean> selectionListener : selectionInstanceListeners) {
                        selectionListener.accept(instanceData, select);
                    }
                } else if(manifest != null) {
                    for (BiConsumer<LauncherManifest, Boolean> selectionListener : selectionManifestListener) {
                        selectionListener.accept(manifest, select);
                    }
                }
            }
            selected = select;
            return true;
        }
        return false;
    }

    @Override
    public void beforeShow(Stage stage) {
        if(instanceData != null) {
            lbTitle.setText(instanceData.getInstance().getKey().getName());
            lbDetails.setText(instanceData.getVersionComponents().get(0).getValue().getVersionId());
        } else if(manifest != null) {
            lbTitle.setText(manifest.getName());
            lbDetails.setText(manifest.getId());
        }
    }

    @Override
    public void afterShow(Stage stage) {}

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
    }

    public LauncherManifest getManifest() {
        return manifest;
    }

    public void setManifest(LauncherManifest manifest) {
        this.manifest = manifest;
    }

    public boolean isSelected() {
        return selected;
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

    public BiFunction<LauncherManifest, Boolean, Boolean> getSelectionManifestAcceptor() {
        return selectionManifestAcceptor;
    }

    public void setSelectionManifestAcceptor(BiFunction<LauncherManifest, Boolean, Boolean> selectionManifestAcceptor) {
        this.selectionManifestAcceptor = selectionManifestAcceptor;
    }

    public List<BiConsumer<LauncherManifest, Boolean>> getSelectionManifestListener() {
        return selectionManifestListener;
    }

    public void setSelectionManifestListener(List<BiConsumer<LauncherManifest, Boolean>> selectionManifestListener) {
        this.selectionManifestListener = selectionManifestListener;
    }

    public static Pair<SelectorEntryElement, AnchorPane> from(InstanceData instanceData) throws IOException {
        Pair<SelectorEntryElement, AnchorPane> result = newInstance();
        result.getKey().setInstanceData(instanceData);
        return result;
    }

    public static Pair<SelectorEntryElement, AnchorPane> from(LauncherManifest manifest) throws IOException {
        Pair<SelectorEntryElement, AnchorPane> result = newInstance();
        result.getKey().setManifest(manifest);
        return result;
    }

    public static Pair<SelectorEntryElement, AnchorPane> newInstance() throws IOException {
        FXMLLoader loader = UiLoader.getFXMLLoader("generic/SelectorEntryElement");
        AnchorPane element = UiLoader.loadFXML(loader);
        SelectorEntryElement listElementController = loader.getController();
        return new Pair<>(listElementController, element);
    }

    @Override
    public void setRootVisible(boolean visible) {
        entryRoot.setVisible(visible);
    }
}
