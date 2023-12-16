package net.treset.minecraftlauncher.ui.selector;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.create.ModsCreatorElement;
import net.treset.minecraftlauncher.ui.manager.ModsManagerElement;
import net.treset.minecraftlauncher.util.exception.GameResourceException;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModsSelectorElement extends ManifestSelectorElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsSelectorElement.class);
    @FXML
    private ModsManagerElement icManagerController;

    private Pair<LauncherManifest, LauncherModsDetails> currentMods;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        ((ModsCreatorElement) crCreator).setModsType("fabric");
        icManagerController.init(this, lockSetter, lockGetter);
        icManagerController.setVisible(false);
    }

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        icManagerController.setVisible(false);
    }

    @Override
    protected void reloadComponents() {
        super.reloadComponents();
        ((ModsCreatorElement) crCreator).init(files.getModsComponents(), files.getLauncherDetails().getTypeConversion(), files.getModsManifest(), files.getGameDetailsManifest());
    }

    @Override
    protected void onSelectCreate() {
        super.onSelectCreate();
        if (!getLock()) {
            icManagerController.setVisible(false);
        }
    }

    @Override
    protected void deleteCurrent() {
        if (currentMods != null) {
            if (!files.getModsManifest().getComponents().remove(currentMods.getKey().getId())) {
                LauncherApplication.displaySevereError(new GameResourceException("Unable to remove mods from manifest"));
                return;
            }
            try {
                LauncherFile.of(files.getModsManifest().getDirectory(), files.getGameDetailsManifest().getComponents().get(0)).write(files.getModsManifest());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            try {
                LauncherFile.of(currentMods.getKey().getDirectory()).remove();
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }
            LOGGER.debug("Mods deleted");
            setVisible(false);
            setVisible(true);
        }
    }

    @Override
    protected String getManifestId(LauncherInstanceDetails instanceDetails) {
        return instanceDetails.getModsComponent();
    }

    @Override
    protected List<LauncherManifest> getComponents() {
        return files.getModsComponents().stream().map(Pair::getKey).toList();
    }

    @Override
    protected void onSelected(ManifestContentProvider contentProvider, boolean selected) {
        super.onSelected(contentProvider, selected);
        if (selected) {
            currentMods = null;
            for (Pair<LauncherManifest, LauncherModsDetails> m : files.getModsComponents()) {
                if (m.getKey().equals(contentProvider.getManifest())) {
                    currentMods = m;
                    break;
                }
            }
            if (currentMods == null) {
                return;
            }
            icManagerController.setLauncherMods(currentMods);
            icManagerController.setVisible(false);
            icManagerController.setVisible(true);
        } else {
            currentMods = null;
            icManagerController.setVisible(false);
        }
    }

    @Override
    protected LauncherManifest getBaseManifest() {
        return files.getModsManifest();
    }
}
