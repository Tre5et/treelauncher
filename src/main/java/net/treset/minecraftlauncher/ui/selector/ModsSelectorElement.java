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
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.create.ModsCreatorElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.ui.manager.ModsManagerElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModsSelectorElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsSelectorElement.class);

    @FXML
    private SplitPane rootPane;
    @FXML private VBox modsContainer;
    @FXML private HBox createSelector;
    @FXML private Button folderButton;
    @FXML private Label modsDetailsTitle;
    @FXML private VBox creatorContainer;
    @FXML private ModsCreatorElement modsCreatorController;
    @FXML private Button createButton;
    @FXML private ModsManagerElement modsManagerController;

    private LauncherFiles files;
    private List<Pair<SelectorEntryElement, AnchorPane>> mods = new ArrayList<>();
    private Pair<LauncherManifest, LauncherModsDetails> currentMods;
    private boolean createSelected;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        files = new LauncherFiles();
        modsCreatorController.enableUse(false);
        modsCreatorController.init(this, lockSetter, lockGetter);
        modsCreatorController.setPrerequisites(files.getModsComponents(), files.getLauncherDetails().getTypeConversion(), files.getModsManifest(), files.getGameDetailsManifest());
        modsCreatorController.enableVersionSelect(true);
        modsCreatorController.setModsType("fabric");
        modsManagerController.setVisible(false);
    }

    private void reloadComponents() {
        files.reloadAll();
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
        modsDetailsTitle.setDisable(true);
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
        for(Pair<SelectorEntryElement, AnchorPane> save: mods) {
            save.getKey().beforeShow(stage);
        }
    }

    @Override
    public void afterShow(Stage stage) {
        modsCreatorController.afterShow(stage);
        for(Pair<SelectorEntryElement, AnchorPane> save: mods) {
            save.getKey().afterShow(stage);
        }
    }

    @FXML
    private void onFolderButtonClicked() {
        if(currentMods == null) {
            LOGGER.warn("No saves selected");
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
                creatorContainer.setVisible(false);
            } else {
                createSelector.getStyleClass().add("selected");
                for(Pair<SelectorEntryElement, AnchorPane> mods : mods) {
                    mods.getKey().select(false, true, false);
                }
                folderButton.setDisable(true);
                modsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.create"));
                modsDetailsTitle.setDisable(false);
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
            modsCreatorController.getCreator().getId();
            reloadComponents();
            for(Pair<SelectorEntryElement, AnchorPane> mod: mods) {
                mod.getKey().beforeShow(null);
            }
        } else {
            modsCreatorController.showError(true);
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
            modsDetailsTitle.setText(manifest.getName());
            modsDetailsTitle.setDisable(false);
            modsManagerController.setLauncherMods(currentMods.getValue());
            modsManagerController.setVisible(false);
            modsManagerController.setVisible(true);
        } else {
            currentMods = null;
            folderButton.setDisable(true);
            modsDetailsTitle.setText(LauncherApplication.stringLocalizer.get("components.label.details.title"));
            modsDetailsTitle.setDisable(true);
            modsManagerController.setVisible(false);
        }
    }
}
