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
import net.treset.minecraftlauncher.ui.create.OptionsCreatorElement;
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

public class OptionsSelectorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(OptionsSelectorElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private VBox optionsContainer;
    @FXML private HBox createSelector;
    @FXML private Button folderButton;
    @FXML private Button deleteButton;
    @FXML private Label optionsDetailsTitle;
    @FXML private VBox creatorContainer;
    @FXML private OptionsCreatorElement optionsCreatorController;
    @FXML private Button createButton;
    @FXML private PopupElement popupController;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> options = new ArrayList<>();
    private LauncherManifest currentOptions;
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
        optionsCreatorController.enableUse(false);
        optionsCreatorController.init(this, lockSetter, lockGetter, severeExceptionHandler);
        optionsCreatorController.setPrerequisites(files.getOptionsComponents(), files.getLauncherDetails().getTypeConversion(), files.getOptionsManifest());
    }

    private void reloadComponents() {
        try {
            files.reloadAll();
        } catch (FileLoadException e) {
            handleSevereException(e);
        }
        options = new ArrayList<>();
        for(LauncherManifest save: files.getOptionsComponents()) {
            try {
                options.add(SelectorEntryElement.from(save));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        optionsContainer.getChildren().clear();
        folderButton.setDisable(true);
        optionsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
        optionsDetailsTitle.setDisable(true);
        deleteButton.setDisable(true);
        createSelected = false;
        createSelector.getStyleClass().remove("selected");
        creatorContainer.setVisible(false);
        for(Pair<SelectorEntryElement, AnchorPane> options : options) {
            optionsContainer.getChildren().add(options.getValue());
            options.getKey().setSelectionManifestAcceptor(this::allowSelection);
            options.getKey().setSelectionManifestListener(List.of(this::onSelected));
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
        optionsCreatorController.beforeShow(stage);
        for(Pair<SelectorEntryElement, AnchorPane> save: options) {
            save.getKey().beforeShow(stage);
        }
    }

    @Override
    public void afterShow(Stage stage) {
        optionsCreatorController.afterShow(stage);
        for(Pair<SelectorEntryElement, AnchorPane> save: options) {
            save.getKey().afterShow(stage);
        }
    }

    @FXML
    private void onFolderButtonClicked() {
        if(currentOptions == null) {
            LOGGER.warn("No saves selected");
        }
        File folder = new File(currentOptions.getDirectory());
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
                optionsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
                optionsDetailsTitle.setDisable(true);
                deleteButton.setDisable(true);
                creatorContainer.setVisible(false);
            } else {
                createSelector.getStyleClass().add("selected");
                for(Pair<SelectorEntryElement, AnchorPane> save : options) {
                    save.getKey().select(false, true, false);
                }
                folderButton.setDisable(true);
                optionsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.create"));
                optionsDetailsTitle.setDisable(false);
                deleteButton.setDisable(true);
                optionsCreatorController.beforeShow(null);
                creatorContainer.setVisible(true);
                optionsCreatorController.afterShow(null);
            }
            createSelected = !createSelected;
        }
    }

    @FXML
    private void onCreateButtonClicked() {
        if(optionsCreatorController.checkCreateReady()) {
            try {
                optionsCreatorController.getCreator().getId();
            } catch (ComponentCreationException e) {
                displayError(e);
            }
            reloadComponents();
            for(Pair<SelectorEntryElement, AnchorPane> option: options) {
                option.getKey().beforeShow(null);
            }
        } else {
            optionsCreatorController.showError(true);
        }
    }

    @FXML
    private void onDeleteButtonClicked() {
        for(Pair<LauncherManifest, LauncherInstanceDetails> i : files.getInstanceComponents()) {
            if(i.getValue().getOptionsComponent().equals(currentOptions.getId())) {
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
        if(currentOptions != null) {
            if(!files.getOptionsManifest().getComponents().remove(currentOptions.getId())) {
                LOGGER.warn("Unable to remove options from manifest");
                return;
            }
            try {
                files.getOptionsManifest().writeToFile(files.getOptionsManifest().getDirectory() + LauncherApplication.config.MANIFEST_FILE_NAME);
            } catch (IOException e) {
                displayError(e);
                return;
            }
            optionsCreatorController.setPrerequisites(files.getOptionsComponents(), files.getLauncherDetails().getTypeConversion(), files.getOptionsManifest());
            try {
                FileUtil.deleteDir(new File(currentOptions.getDirectory()));
            } catch (IOException e) {
                displayError(e);
                return;
            }
            LOGGER.debug("Options deleted");
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
            for(Pair<SelectorEntryElement, AnchorPane> option : options) {
                option.getKey().select(false, true, false);
            }
            currentOptions = manifest;
            createSelected = false;
            createSelector.getStyleClass().remove("selected");
            creatorContainer.setVisible(false);
            folderButton.setDisable(false);
            deleteButton.setDisable(false);
            optionsDetailsTitle.setText(manifest.getName());
            optionsDetailsTitle.setDisable(false);
        } else {
            currentOptions = null;
            folderButton.setDisable(true);
            deleteButton.setDisable(true);
            optionsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
            optionsDetailsTitle.setDisable(true);
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
