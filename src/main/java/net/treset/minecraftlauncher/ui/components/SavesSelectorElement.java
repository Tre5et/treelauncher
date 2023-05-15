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
import net.treset.minecraftlauncher.ui.create.SavesCreatorElement;
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

public class SavesSelectorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(SavesSelectorElement.class);

    @FXML private SplitPane rootPane;
    @FXML private VBox savesContainer;
    @FXML private HBox createSelector;
    @FXML private Button folderButton;
    @FXML private Label savesDetailsTitle;
    @FXML private VBox creatorContainer;
    @FXML private SavesCreatorElement savesCreatorController;
    @FXML private Button createButton;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> saves = new ArrayList<>();
    private LauncherManifest currentSaves;
    private boolean createSelected;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        files = new LauncherFiles();
        savesCreatorController.enableUse(false);
        savesCreatorController.init(this, lockSetter, lockGetter);
        savesCreatorController.setPrerequisites(files.getSavesComponents(), files.getLauncherDetails().getTypeConversion(), files.getSavesManifest(), files.getGameDetailsManifest());
    }

    private void reloadComponents() {
        files.reloadAll();
        saves = new ArrayList<>();
        for(LauncherManifest save: files.getSavesComponents()) {
            try {
                saves.add(SelectorEntryElement.from(save));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        savesContainer.getChildren().clear();
        folderButton.setDisable(true);
        savesDetailsTitle.setDisable(true);
        createSelected = false;
        createSelector.getStyleClass().remove("selected");
        creatorContainer.setVisible(false);
        for(Pair<SelectorEntryElement, AnchorPane> save : saves) {
            savesContainer.getChildren().add(save.getValue());
            save.getKey().setSelectionManifestAcceptor(this::allowSelection);
            save.getKey().setSelectionManifestListener(List.of(this::onSelected));
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
        savesCreatorController.beforeShow(stage);
        for(Pair<SelectorEntryElement, AnchorPane> save: saves) {
            save.getKey().beforeShow(stage);
        }
    }

    @Override
    public void afterShow(Stage stage) {
        savesCreatorController.afterShow(stage);
        for(Pair<SelectorEntryElement, AnchorPane> save: saves) {
            save.getKey().afterShow(stage);
        }
    }

    @FXML
    private void onFolderButtonClicked() {
        if(currentSaves == null) {
            LOGGER.warn("No saves selected");
        }
        File folder = new File(currentSaves.getDirectory());
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
                savesDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
                savesDetailsTitle.setDisable(true);
                creatorContainer.setVisible(false);
            } else {
                createSelector.getStyleClass().add("selected");
                for(Pair<SelectorEntryElement, AnchorPane> save : saves) {
                    save.getKey().select(false, true, false);
                }
                folderButton.setDisable(true);
                savesDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.create"));
                savesDetailsTitle.setDisable(false);
                savesCreatorController.beforeShow(null);
                creatorContainer.setVisible(true);
                savesCreatorController.afterShow(null);
            }
            createSelected = !createSelected;
        }
    }

    @FXML
    private void onCreateButtonClicked() {
        if(savesCreatorController.checkCreateReady()) {
            savesCreatorController.getCreator().getId();
            reloadComponents();
            for(Pair<SelectorEntryElement, AnchorPane> save: saves) {
                save.getKey().beforeShow(null);
            }
        } else {
            savesCreatorController.showError(true);
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
            for(Pair<SelectorEntryElement, AnchorPane> save : saves) {
                save.getKey().select(false, true, false);
            }
            currentSaves = manifest;
            createSelected = false;
            createSelector.getStyleClass().remove("selected");
            creatorContainer.setVisible(false);
            folderButton.setDisable(false);
            savesDetailsTitle.setText(manifest.getName());
            savesDetailsTitle.setDisable(false);
        } else {
            currentSaves = null;
            folderButton.setDisable(true);
            savesDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
            savesDetailsTitle.setDisable(true);
        }
    }
}
