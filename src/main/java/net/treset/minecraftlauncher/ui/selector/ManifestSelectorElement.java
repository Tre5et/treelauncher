package net.treset.minecraftlauncher.ui.selector;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.generic.lists.FolderContentContainer;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ManifestSelectorElement extends SelectorElement<SelectorEntryElement> {
    private static final Logger LOGGER = LogManager.getLogger(ManifestSelectorElement.class);

    @FXML
    protected FolderContentContainer ccDetails;

    protected LauncherManifest currentManifest = null;

    @Override
    protected String getCurrentUsedBy() {
        for(Pair<LauncherManifest, LauncherInstanceDetails> i : files.getInstanceComponents()) {
            if (currentManifest.getId().equals(getManifestId(i.getValue()))) {
                return i.getKey().getName();
            }
        }
        return null;
    }

    protected abstract String getManifestId(LauncherInstanceDetails instanceDetails);

    @Override
    protected List<Pair<SelectorEntryElement, AnchorPane>> getElements() {
        ArrayList<Pair<SelectorEntryElement, AnchorPane>> elements = new ArrayList<>();
        for(LauncherManifest save: getComponents()) {
            try {
                elements.add(SelectorEntryElement.from(save));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return elements.stream().peek(e -> {
            e.getKey().setSelectionManifestAcceptor(this::allowSelection);
            e.getKey().setSelectionManifestListener(List.of(this::onSelected));
        }).toList();
    }

    protected abstract List<LauncherManifest> getComponents();

    protected boolean allowSelection(LauncherManifest manifest, boolean selected) {
        return !getLock();
    }

    protected void onSelected(LauncherManifest manifest, boolean selected) {
        if(ccDetails != null) {
            ccDetails.setVisible(selected);
        }
        if(selected) {
            deselectAll();
            currentManifest = manifest;
            createSelected = false;
            csCreate.getStyleClass().remove("selected");
            vbCreate.setVisible(false);
            abMain.setDisable(false);
            abMain.setLabel(manifest.getName());
            if(ccDetails != null) {
                ccDetails.setFolder(new File(manifest.getDirectory()));
            }
        } else {
            currentManifest = null;
            abMain.setDisable(true);
            abMain.clearLabel();
        }
    }

    @Override
    protected void onSelectCreate() {
        super.onSelectCreate();
        if(ccDetails != null) {
            ccDetails.setVisible(false);
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        if(ccDetails != null) {
            ccDetails.setVisible(false);
        }
    }

    @Override
    protected void onFolder() {
        if(currentManifest == null) {
            LOGGER.warn("No element selected");
        }
        File folder = new File(currentManifest.getDirectory());
        try {
            Desktop.getDesktop().open(folder);
        } catch (IOException e) {
            LOGGER.warn("Unable to open folder", e);
        }
    }

    @Override
    protected boolean editValid(String newName) {
        return newName != null && !newName.isBlank() && !newName.equals(currentManifest.getName());
    }

    @Override
    protected void editCurrent(String newName) {
        if(currentManifest == null) {
            LOGGER.warn("No element selected");
            return;
        }
        currentManifest.setName(newName);
        try {
            currentManifest.writeToFile(FormatUtil.absoluteFilePath(currentManifest.getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        setVisible(false);
        setVisible(true);
    }
}
