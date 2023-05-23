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
import net.treset.minecraftlauncher.ui.generic.ComponentChangerElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.ui.manager.InstanceDetailsElement;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class InstanceSelectorElement extends UiElement {
    @FXML private SplitPane rootPane;
    @FXML private VBox instanceContainer;
    @FXML private Button playButton;
    @FXML private Label instanceDetailsTitle;
    @FXML private Button folderButton;
    @FXML private InstanceDetailsElement instanceDetailsController;
    @FXML private Button componentFolderButton;
    @FXML private Label componentTitleLabel;
    @FXML private ComponentChangerElement componentChangerController;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> instances = new ArrayList<>();
    private InstanceData currentInstance;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        instanceDetailsController.init(this::onComponentSelected);
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
        instanceDetailsController.clearSelection();
        componentChangerController.setVisible(false);
        componentTitleLabel.setDisable(true);
        componentTitleLabel.setText(LauncherApplication.stringLocalizer.get("selector.instance.label.component.title"));
        componentFolderButton.setDisable(true);
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
            folderButton.setDisable(false);
            instanceDetailsController.populate(instanceData);
            instanceDetailsController.setVisible(true);
        } else {
            playButton.setDisable(true);
            currentInstance = null;
            instanceDetailsTitle.setText(LauncherApplication.stringLocalizer.get("instances.label.details.title"));
            instanceDetailsTitle.setDisable(true);
            folderButton.setDisable(true);
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

    private void onComponentSelected(boolean selected, InstanceDetailsElement.SelectedType type) {
        componentChangerController.setVisible(false);
        componentTitleLabel.setDisable(true);
        componentTitleLabel.setText(LauncherApplication.stringLocalizer.get("selector.instance.label.component.title"));
        componentFolderButton.setDisable(true);
        if(selected && type != null && type != InstanceDetailsElement.SelectedType.VERSION) {
            List<LauncherManifest> manifests;
            LauncherManifest currentManifest;
            String label;
            switch(type) {
                case SAVES -> {
                    manifests = files.getSavesComponents();
                    currentManifest = currentInstance.getSavesComponent();
                    label = LauncherApplication.stringLocalizer.get("selector.instance.change.saves");
                }
                case RESOURCEPACKS -> {
                    manifests = files.getResourcepackComponents();
                    currentManifest = currentInstance.getResourcepacksComponent();
                    label = LauncherApplication.stringLocalizer.get("selector.instance.change.resourcepacks");
                }
                case OPTIONS -> {
                    manifests = files.getOptionsComponents();
                    currentManifest = currentInstance.getOptionsComponent();
                    label = LauncherApplication.stringLocalizer.get("selector.instance.change.options");
                }
                case MODS -> {
                    manifests = files.getModsComponents().stream().map(Pair::getKey).toList();
                    currentManifest = currentInstance.getModsComponent().getKey();
                    label = LauncherApplication.stringLocalizer.get("selector.instance.change.mods");
                }
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }
            componentChangerController.init(manifests, currentManifest, this::onComponentChanged, this::allowChange);
            componentTitleLabel.setText(label);
            componentTitleLabel.setDisable(false);
            componentFolderButton.setDisable(false);
            componentChangerController.setVisible(true);
        }
    }

    private boolean allowChange() {
        return true;
    }

    private void onComponentChanged(LauncherManifest manifest) {
        switch(instanceDetailsController.getCurrentSelected()) {
            case SAVES -> currentInstance.getInstance().getValue().setSavesComponent(manifest.getId());
            case RESOURCEPACKS -> currentInstance.getInstance().getValue().setResourcepacksComponent(manifest.getId());
            case OPTIONS -> currentInstance.getInstance().getValue().setOptionsComponent(manifest.getId());
            case MODS -> currentInstance.getInstance().getValue().setModsComponent(manifest.getId());
        }
        currentInstance.getInstance().getValue().writeToFile(currentInstance.getInstance().getKey().getDirectory() + currentInstance.getInstance().getKey().getDetails());
        files.reloadAll();
        currentInstance = InstanceData.of(currentInstance.getInstance(), files);
        if(currentInstance != null) {
            instanceDetailsController.populate(currentInstance);
        }
    }

    @FXML
    private void onComponentFolderButtonClicked() {
        switch (instanceDetailsController.getCurrentSelected()) {
            case SAVES -> openFolder(currentInstance.getSavesComponent().getDirectory());
            case RESOURCEPACKS -> openFolder(currentInstance.getResourcepacksComponent().getDirectory());
            case OPTIONS -> openFolder(currentInstance.getOptionsComponent().getDirectory());
            case MODS -> openFolder(currentInstance.getModsComponent().getKey().getDirectory());
        }
    }

    @FXML
    private void onFolderButtonClicked() {
        openFolder(currentInstance.getInstance().getKey().getDirectory());
    }

    private void openFolder(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
