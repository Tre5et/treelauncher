package net.treset.minecraftlauncher.ui.generic;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.UiUtil;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SelectorEntryElement extends UiElement {

    @FXML protected AnchorPane entryRoot;
    @FXML protected GridPane entryContainer;
    @FXML protected Label lbTitle;
    @FXML protected Label lbDetails;

    protected boolean selected = false;
    private LauncherManifest manifest;
    private BiFunction<LauncherManifest, Boolean, Boolean> selectionManifestAcceptor;
    private List<BiConsumer<LauncherManifest, Boolean>> selectionManifestListener;

    @FXML
    public void onClick() {
        select(!selected, false, true);
    }

    public boolean select(boolean select, boolean force, boolean callback) {
        if(selected != select && (force || manifest != null && selectionManifestAcceptor.apply(manifest, select))) {
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
                for (BiConsumer<LauncherManifest, Boolean> selectionListener : selectionManifestListener) {
                    selectionListener.accept(manifest, select);
                }
            }
            selected = select;
            return true;
        }
        return false;
    }

    @Override
    public void beforeShow(Stage stage) {
        lbTitle.setText(manifest.getName());
        lbDetails.setText(manifest.getId());
    }

    @Override
    public void afterShow(Stage stage) {}

    public LauncherManifest getManifest() {
        return manifest;
    }

    public void setManifest(LauncherManifest manifest) {
        this.manifest = manifest;
    }

    public boolean isSelected() {
        return selected;
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

    public static Pair<SelectorEntryElement, AnchorPane> from(LauncherManifest manifest) throws IOException {
        Pair<SelectorEntryElement, AnchorPane> result = newInstance();
        result.getKey().setManifest(manifest);
        return result;
    }

    private static Pair<SelectorEntryElement, AnchorPane> newInstance() throws IOException {
        FXMLLoader loader = UiUtil.getFXMLLoader("generic/SelectorEntryElement");
        AnchorPane element = UiUtil.loadFXML(loader);
        SelectorEntryElement listElementController = loader.getController();
        return new Pair<>(listElementController, element);
    }

    @Override
    public void setRootVisible(boolean visible) {
        entryRoot.setVisible(visible);
    }
}
