package net.treset.minecraftlauncher.ui.selector;

import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ManifestSelectorElement extends SelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(ManifestSelectorElement.class);

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
        if(selected) {
            deselectAll();
            currentManifest = manifest;
            createSelected = false;
            createSelectable.getStyleClass().remove("selected");
            createContainer.setVisible(false);
            actionBar.setDisable(false);
            actionBar.setLabel(manifest.getName());
        } else {
            currentManifest = null;
            actionBar.setDisable(true);
            actionBar.clearLabel();
        }
    }

    @Override
    protected void onFolderClicked() {
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
}
