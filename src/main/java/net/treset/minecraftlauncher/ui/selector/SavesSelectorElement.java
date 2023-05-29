package net.treset.minecraftlauncher.ui.selector;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.create.SavesCreatorElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SavesSelectorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(SavesSelectorElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private VBox savesContainer;
    @FXML private HBox createSelector;
    @FXML private Button folderButton;
    @FXML private Label savesDetailsTitle;
    @FXML private Button deleteButton;
    @FXML private VBox creatorContainer;
    @FXML private SavesCreatorElement savesCreatorController;
    @FXML private Button createButton;
    @FXML private PopupElement popupController;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> saves = new ArrayList<>();
    private LauncherManifest currentSaves;
    private boolean createSelected;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter, Consumer<Exception> severeExceptionHandler) {
        super.init(parent, lockSetter, lockGetter, severeExceptionHandler);
        try {
            files = new LauncherFiles();
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
        savesCreatorController.enableUse(false);
        savesCreatorController.init(this, lockSetter, lockGetter, severeExceptionHandler);
        savesCreatorController.setPrerequisites(files.getSavesComponents(), files.getLauncherDetails().getTypeConversion(), files.getSavesManifest(), files.getGameDetailsManifest());
    }

    private void reloadComponents() {
        try {
            files.reloadAll();
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
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
        savesDetailsTitle.setText("components.label.details.title");
        deleteButton.setDisable(true);
        savesDetailsTitle.setDisable(true);
        popupController.setVisible(false);
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
                deleteButton.setDisable(true);
                savesDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
                savesDetailsTitle.setDisable(true);
                creatorContainer.setVisible(false);
            } else {
                createSelector.getStyleClass().add("selected");
                for(Pair<SelectorEntryElement, AnchorPane> save : saves) {
                    save.getKey().select(false, true, false);
                }
                folderButton.setDisable(true);
                deleteButton.setDisable(true);
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
            try {
                savesCreatorController.getCreator().getId();
            } catch (ComponentCreationException e) {
                displayError(e);
            }
            reloadComponents();
            for(Pair<SelectorEntryElement, AnchorPane> save: saves) {
                save.getKey().beforeShow(null);
            }
        } else {
            savesCreatorController.showError(true);
        }
    }

    @FXML
    private void onDeleteButtonClicked() {
        for(Pair<LauncherManifest, LauncherInstanceDetails> i : files.getInstanceComponents()) {
            if(i.getValue().getSavesComponent().equals(currentSaves.getId())) {
                popupController.setType(PopupElement.PopupType.ERROR);
                popupController.setTitle("selector.component.delete.unable.title");
                popupController.setMessage("selector.component.delete.unable.message", i.getKey().getName());
                popupController.clearButtons();
                popupController.addButtons(
                        new PopupElement.PopupButton(
                                PopupElement.ButtonType.POSITIVE,
                                "selector.component.delete.unable.close",
                                "close",
                                id -> popupController.setVisible(false)
                        )
                );
                popupController.setVisible(true);
                return;
            }
        }

        popupController.setType(PopupElement.PopupType.WARNING);
        popupController.setContent("selector.component.delete.title", "selector.component.delete.message");
        popupController.clearButtons();
        popupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.NEGATIVE,
                        "selector.component.delete.cancel",
                        "cancel",
                        this::onDeleteCancel
                ),
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.component.delete.confirm",
                        "confirm",
                        this::onDeleteConfirm
                )
        );
        popupController.setVisible(true);
    }

    private void onDeleteCancel(String id) {
        popupController.setVisible(false);
    }

    private void onDeleteConfirm(String id) {
        popupController.setVisible(false);
        if(currentSaves != null) {
            if(!files.getSavesManifest().getComponents().remove(currentSaves.getId())) {
                LOGGER.warn("Unable to remove save from manifest");
                return;
            }
            try {
                files.getSavesManifest().writeToFile(files.getSavesManifest().getDirectory() + files.getGameDetailsManifest().getComponents().get(1));
            } catch (IOException e) {
                displayError(e);
                return;
            }
            savesCreatorController.setPrerequisites(files.getSavesComponents(), files.getLauncherDetails().getTypeConversion(), files.getSavesManifest(), files.getGameDetailsManifest());
            try {
                FileUtil.deleteDir(new File(currentSaves.getDirectory()));
            } catch (IOException e) {
                displayError(e);
            }
            LOGGER.debug("Save deleted");
            setVisible(false);
            setVisible(true);
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
            deleteButton.setDisable(false);
            savesDetailsTitle.setText(manifest.getName());
            savesDetailsTitle.setDisable(false);
        } else {
            currentSaves = null;
            folderButton.setDisable(true);
            deleteButton.setDisable(true);
            savesDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
            savesDetailsTitle.setDisable(true);
        }
    }

    private void displayError(Exception e) {
        LOGGER.error("An error occurred", e);
        popupController.setType(PopupElement.PopupType.ERROR);
        popupController.setTitle("error.title");
        popupController.setMessage("error.message", e.getMessage());
        popupController.setControlsDisabled(false);
        popupController.clearButtons();
        popupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "error.close",
                        "close",
                        id -> popupController.setVisible(false)
                )
        );
    }
}
