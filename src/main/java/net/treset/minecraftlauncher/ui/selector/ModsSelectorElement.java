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
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.create.ModsCreatorElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.ui.manager.ModsManagerElement;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import net.treset.minecraftlauncher.util.exception.GameResourceException;
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

public class ModsSelectorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsSelectorElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private VBox modsContainer;
    @FXML private HBox createSelector;
    @FXML private Button folderButton;
    @FXML private Label modsDetailsTitle;
    @FXML private Button deleteButton;
    @FXML private VBox creatorContainer;
    @FXML private ModsCreatorElement modsCreatorController;
    @FXML private Button createButton;
    @FXML private ModsManagerElement modsManagerController;
    @FXML private PopupElement popupController;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> mods = new ArrayList<>();
    private Pair<LauncherManifest, LauncherModsDetails> currentMods;
    private boolean createSelected;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter, Consumer<Exception> severeExceptionHandler) {
        super.init(parent, lockSetter, lockGetter, severeExceptionHandler);
        try {
            files = new LauncherFiles();
            files.reloadAll();
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
        modsCreatorController.enableUse(false);
        modsCreatorController.init(this, lockSetter, lockGetter, severeExceptionHandler);
        modsCreatorController.setPrerequisites(files.getModsComponents(), files.getLauncherDetails().getTypeConversion(), files.getModsManifest(), files.getGameDetailsManifest());
        modsCreatorController.enableVersionSelect(true);
        modsCreatorController.setModsType("fabric");
        modsManagerController.setVisible(false);
    }

    private void reloadComponents() {
        try {
            files.reloadAll();
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
        mods = new ArrayList<>();
        for(Pair<LauncherManifest, LauncherModsDetails> mod: files.getModsComponents()) {
            try {
                mods.add(SelectorEntryElement.from(mod.getKey()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        modsContainer.getChildren().clear();
        folderButton.setDisable(true);
        modsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
        modsDetailsTitle.setDisable(true);
        deleteButton.setDisable(true);
        createSelected = false;
        createSelector.getStyleClass().remove("selected");
        creatorContainer.setVisible(false);
        for(Pair<SelectorEntryElement, AnchorPane> mod : mods) {
            modsContainer.getChildren().add(mod.getValue());
            mod.getKey().setSelectionManifestAcceptor(this::allowSelection);
            mod.getKey().setSelectionManifestListener(List.of(this::onSelected));
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
        modsCreatorController.beforeShow(stage);
        modsManagerController.setVisible(false);
        for(Pair<SelectorEntryElement, AnchorPane> m: mods) {
            m.getKey().beforeShow(stage);
        }
    }

    @Override
    public void afterShow(Stage stage) {
        modsCreatorController.afterShow(stage);
        for(Pair<SelectorEntryElement, AnchorPane> m: mods) {
            m.getKey().afterShow(stage);
        }
    }

    @FXML
    private void onFolderButtonClicked() {
        if(currentMods == null) {
            LOGGER.warn("No mods selected");
        }
        File folder = new File(currentMods.getKey().getDirectory());
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
                modsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
                modsDetailsTitle.setDisable(true);
                deleteButton.setDisable(true);
                creatorContainer.setVisible(false);
            } else {
                createSelector.getStyleClass().add("selected");
                for(Pair<SelectorEntryElement, AnchorPane> mods : mods) {
                    mods.getKey().select(false, true, false);
                }
                folderButton.setDisable(true);
                modsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.create"));
                modsDetailsTitle.setDisable(false);
                deleteButton.setDisable(true);
                modsManagerController.setVisible(false);
                modsCreatorController.beforeShow(null);
                creatorContainer.setVisible(true);
                modsCreatorController.afterShow(null);
            }
            createSelected = !createSelected;
        }
    }

    @FXML
    private void onCreateButtonClicked() {
        if(modsCreatorController.checkCreateReady()) {
            try {
                modsCreatorController.getCreator().getId();
            } catch (ComponentCreationException e) {
                displayError(e);
            }
            reloadComponents();
            for(Pair<SelectorEntryElement, AnchorPane> mod: mods) {
                mod.getKey().beforeShow(null);
            }
        } else {
            modsCreatorController.showError(true);
        }
    }

    @FXML
    private void onDeleteButtonClicked() {
        for(Pair<LauncherManifest, LauncherInstanceDetails> i : files.getInstanceComponents()) {
            if(currentMods.getKey().getId().equals(i.getValue().getModsComponent())) {
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
        if(currentMods != null) {
            if(!files.getModsManifest().getComponents().remove(currentMods.getKey().getId())) {
                displayError(new GameResourceException("Unable to remove mods from manifest"));
                return;
            }
            try {
                files.getModsManifest().writeToFile(files.getModsManifest().getDirectory() + files.getGameDetailsManifest().getComponents().get(0));
            } catch (IOException e) {
                displayError(e);
                return;
            }
            modsCreatorController.setPrerequisites(files.getModsComponents(), files.getLauncherDetails().getTypeConversion(), files.getModsManifest(), files.getGameDetailsManifest());
            try {
                FileUtil.deleteDir(new File(currentMods.getKey().getDirectory()));
            } catch (IOException e) {
                displayError(e);
                return;
            }
            LOGGER.debug("Mods deleted");
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
            for(Pair<SelectorEntryElement, AnchorPane> mod : mods) {
                mod.getKey().select(false, true, false);
            }
            currentMods = null;
            for(Pair<LauncherManifest, LauncherModsDetails> m : files.getModsComponents()) {
                if(m.getKey().equals(manifest)) {
                    currentMods = m;
                    break;
                }
            }
            if(currentMods == null) {
                return;
            }
            createSelected = false;
            createSelector.getStyleClass().remove("selected");
            creatorContainer.setVisible(false);
            folderButton.setDisable(false);
            deleteButton.setDisable(false);
            modsDetailsTitle.setText(manifest.getName());
            modsDetailsTitle.setDisable(false);
            modsManagerController.setLauncherMods(currentMods);
            modsManagerController.setVisible(false);
            modsManagerController.setVisible(true);
        } else {
            currentMods = null;
            folderButton.setDisable(true);
            deleteButton.setDisable(true);
            modsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
            modsDetailsTitle.setDisable(true);
            modsManagerController.setVisible(false);
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
