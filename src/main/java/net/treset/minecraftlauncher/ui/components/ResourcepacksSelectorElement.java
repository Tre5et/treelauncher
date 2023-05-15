package net.treset.minecraftlauncher.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.create.ResourcepacksCreatorElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ResourcepacksSelectorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(SavesSelectorElement.class);

    @FXML
    private SplitPane rootPane;
    @FXML private VBox resourcepacksContainer;
    @FXML private HBox createSelector;
    @FXML private Button folderButton;
    @FXML private Label resourcepacksDetailsTitle;
    @FXML private VBox creatorContainer;
    @FXML private ResourcepacksCreatorElement resourcepacksCreatorController;
    @FXML private Button createButton;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> resourcepacks = new ArrayList<>();
    private LauncherManifest currentResourcepacks;
    private boolean createSelected;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        files = new LauncherFiles();
        resourcepacksCreatorController.enableUse(false);
        resourcepacksCreatorController.init(this, lockSetter, lockGetter);
        resourcepacksCreatorController.setPrerequisites(files.getResourcepackComponents(), files.getLauncherDetails().getTypeConversion(), files.getResourcepackManifest());
    }

    private void reloadComponents() {
        files.reloadAll();
        resourcepacks = new ArrayList<>();
        for(LauncherManifest resourcepack: files.getResourcepackComponents()) {
            try {
                resourcepacks.add(SelectorEntryElement.from(resourcepack));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        resourcepacksContainer.getChildren().clear();
        folderButton.setDisable(true);
        resourcepacksDetailsTitle.setDisable(true);
        createSelected = false;
        createSelector.getStyleClass().remove("selected");
        creatorContainer.setVisible(false);
        for(Pair<SelectorEntryElement, AnchorPane> resourcepack : resourcepacks) {
            resourcepacksContainer.getChildren().add(resourcepack.getValue());
            resourcepack.getKey().setSelectionManifestAcceptor(this::allowSelection);
            resourcepack.getKey().setSelectionManifestListener(List.of(this::onSelected));
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
        resourcepacksCreatorController.beforeShow(stage);
        for(Pair<SelectorEntryElement, AnchorPane> resourcepack: resourcepacks) {
            resourcepack.getKey().beforeShow(stage);
        }
    }

    @Override
    public void afterShow(Stage stage) {
        resourcepacksCreatorController.afterShow(stage);
        for(Pair<SelectorEntryElement, AnchorPane> resourcepack: resourcepacks) {
            resourcepack.getKey().afterShow(stage);
        }
    }

    @FXML
    private void onFolderButtonClicked() {
        if(currentResourcepacks == null) {
            LOGGER.warn("No resourcepacks selected");
        }
        File folder = new File(currentResourcepacks.getDirectory());
        try {
            Desktop.getDesktop().open(folder);
        } catch (IOException e) {
            LOGGER.warn("Unable to open folder", e);
        }
    }

    @FXML
    private void onCreateSelectorClicked() {
        if(!getLock()) {
            if(createSelected) {
                createSelector.getStyleClass().remove("selected");
                folderButton.setDisable(true);
                resourcepacksDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
                resourcepacksDetailsTitle.setDisable(true);
                creatorContainer.setVisible(false);
            } else {
                createSelector.getStyleClass().add("selected");
                for(Pair<SelectorEntryElement, AnchorPane> resourcepack : resourcepacks) {
                    resourcepack.getKey().select(false, true, false);
                }
                folderButton.setDisable(true);
                resourcepacksDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.create"));
                resourcepacksDetailsTitle.setDisable(false);
                resourcepacksCreatorController.beforeShow(null);
                creatorContainer.setVisible(true);
                resourcepacksCreatorController.afterShow(null);
            }
            createSelected = !createSelected;
        }
    }

    @FXML
    private void onCreateButtonClicked() {
        if(resourcepacksCreatorController.checkCreateReady()) {
            resourcepacksCreatorController.getCreator().getId();
            reloadComponents();
            for(Pair<SelectorEntryElement, AnchorPane> resourcepack: resourcepacks) {
                resourcepack.getKey().beforeShow(null);
            }
        } else {
            resourcepacksCreatorController.showError(true);
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    private boolean allowSelection(LauncherManifest manifest, boolean selected) {
        return !getLock();
    }

    private void onSelected(LauncherManifest manifest, boolean selected) {
        if(selected) {
            for(Pair<SelectorEntryElement, AnchorPane> resourcepack : resourcepacks) {
                resourcepack.getKey().select(false, true, false);
            }
            currentResourcepacks = manifest;
            createSelected = false;
            createSelector.getStyleClass().remove("selected");
            creatorContainer.setVisible(false);
            folderButton.setDisable(false);
            resourcepacksDetailsTitle.setText(manifest.getName());
            resourcepacksDetailsTitle.setDisable(false);
        } else {
            currentResourcepacks = null;
            folderButton.setDisable(true);
            resourcepacksDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
            resourcepacksDetailsTitle.setDisable(true);
        }
    }
}
