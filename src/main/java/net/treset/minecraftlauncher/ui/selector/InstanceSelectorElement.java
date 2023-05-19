package net.treset.minecraftlauncher.ui.selector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.ui.manager.InstanceDetailsElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class InstanceSelectorElement extends UiElement {
    @FXML private SplitPane rootPane;
    @FXML public VBox instanceContainer;
    @FXML public Button playButton;
    @FXML public Label instanceDetailsTitle;
    @FXML public InstanceDetailsElement instanceDetailsController;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> instances = new ArrayList<>();
    private InstanceData currentInstance;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        files = new LauncherFiles();
    }

    public void reloadComponents() {
        files.reloadAll();
        instances = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherInstanceDetails> instance : files.getInstanceComponents()) {
            try {
                instances.add(SelectorEntryElement.from(InstanceData.of(instance, files)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        instanceContainer.getChildren().clear();
        for(Pair<SelectorEntryElement, AnchorPane> instance : instances) {
            instanceContainer.getChildren().add(instance.getValue());
            instance.getKey().setSelectionInstanceAcceptor(this::allowSelection);
            instance.getKey().setSelectionInstanceListeners(List.of(this::onSelected));
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
        for(Pair<SelectorEntryElement, AnchorPane> instance : instances) {
            instance.getKey().beforeShow(stage);
        }
    }
    @Override
    public void afterShow(Stage stage) {
        for(Pair<SelectorEntryElement, AnchorPane> instance : instances) {
            instance.getKey().afterShow(stage);
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    private void onSelected(InstanceData instanceData, boolean selected) {
        if(selected) {
            for(Pair<SelectorEntryElement, AnchorPane> instance : instances) {
                if(instance.getKey().getInstanceData() != instanceData) {
                    instance.getKey().select(false, true, false);
                }
            }
            playButton.setDisable(false);
            currentInstance = instanceData;
            instanceDetailsTitle.setText(instanceData.getInstance().getKey().getName());
            instanceDetailsTitle.setDisable(false);
            instanceDetailsController.populate(instanceData);
            instanceDetailsController.setVisible(true);
        } else {
            playButton.setDisable(true);
            currentInstance = null;
            instanceDetailsTitle.setText(LauncherApplication.stringLocalizer.get("instances.label.details.title"));
            instanceDetailsTitle.setDisable(true);
            instanceDetailsController.setVisible(false);
        }
    }

    private boolean allowSelection(InstanceData instanceData, boolean selected) {
        return !getLock();
    }

    public void onPlayButtonClicked() {
        if(currentInstance != null) {
            setLock(true);
            playButton.setDisable(true);
            GameLauncher launcher = new GameLauncher(currentInstance, files, LauncherApplication.userAuth.getMinecraftUser(), List.of(this::onGameExit));
            if(!launcher.launch(false)) {
                onGameExit(null);
            }
        }
    }

    private void onGameExit(String s) {
        setLock(false);
        Platform.runLater(() -> playButton.setDisable(false));
    }


}
