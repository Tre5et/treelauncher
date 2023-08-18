package net.treset.minecraftlauncher.ui;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.GenericUiController;
import net.treset.minecraftlauncher.ui.create.InstanceCreatorElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.login.LoginController;
import net.treset.minecraftlauncher.ui.nav.NavbarElement;
import net.treset.minecraftlauncher.ui.selector.*;
import net.treset.minecraftlauncher.ui.settings.SettingsElement;
import net.treset.minecraftlauncher.ui.title.TitlebarElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class MainController extends GenericUiController {
    private static final Logger LOGGER = LogManager.getLogger(MainController.class);

    @FXML private TitlebarElement icTitlebarController;
    @FXML private InstanceSelectorElement icInstancesController;
    @FXML private InstanceCreatorElement icCreatorController;
    @FXML private SavesSelectorElement icSavesController;
    @FXML private ResourcepacksSelectorElement icResourcepacksController;
    @FXML private OptionsSelectorElement icOptionsController;
    @FXML private ModsSelectorElement icModsController;
    @FXML private SettingsElement icSettingsController;
    @FXML private NavbarElement icNavbarController;
    @FXML private PopupElement icPopupController;

    boolean locked = false;

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        LauncherApplication.setPopupConsumer(this::showPopup);
        LauncherApplication.setCloseCallback(() -> !getLocked());

        icTitlebarController.init(this, this::setLocked, this::getLocked);
        icTitlebarController.beforeShow(stage);
        icNavbarController.init(this, this::setLocked, this::getLocked);
        icNavbarController.setComponentActivator(this::activate);
        icNavbarController.beforeShow(stage);
        icInstancesController.init(this, this::setLocked, this::getLocked);
        icCreatorController.init(this, this::setLocked, this::getLocked);
        icSavesController.init(this, this::setLocked, this::getLocked);
        icResourcepacksController.init(this, this::setLocked, this::getLocked);
        icOptionsController.init(this, this::setLocked, this::getLocked);
        icModsController.init(this, this::setLocked, this::getLocked);
        icSettingsController.init(this::onLogout);
        icNavbarController.triggerHomeAction();
    }

    @Override
    public void afterShow(Stage stage) {
        super.afterShow(stage);
        icTitlebarController.afterShow(stage);
        icNavbarController.afterShow(stage);
    }

    @Override
    public void triggerHomeAction() {
        activate(Component.INSTANCE_SELECTOR);
    }

    public static MainController showOnStage(Stage stage) throws IOException {
        return showOnStage(stage, "MainScreen", "launcher.name");
    }

    public boolean activate(Component component) {
        if(getLocked()) return false;
        switch (component) {
            case INSTANCE_SELECTOR -> {
                setAllInvisible();
                icInstancesController.setVisible(true);
            }
            case INSTANCE_CREATOR -> {
                setAllInvisible();
                icCreatorController.setVisible(true);
            }
            case SAVES_SELECTOR -> {
                setAllInvisible();
                icSavesController.setVisible(true);
            }
            case RESOURCEPACKS_SELECTOR -> {
                setAllInvisible();
                icResourcepacksController.setVisible(true);
            }
            case OPTIONS_SELECTOR -> {
                setAllInvisible();
                icOptionsController.setVisible(true);
            }
            case MODS_SELECTOR -> {
                setAllInvisible();
                icModsController.setVisible(true);
            }
            case SETTINGS -> {
                setAllInvisible();
                icSettingsController.setVisible(true);
            }
        }
        return true;
    }

    private void setAllInvisible() {
        icInstancesController.setVisible(false);
        icCreatorController.setVisible(false);
        icSavesController.setVisible(false);
        icResourcepacksController.setVisible(false);
        icOptionsController.setVisible(false);
        icModsController.setVisible(false);
        icSettingsController.setVisible(false);
    }

    public boolean getLocked() {
        return locked;
    }

    public boolean setLocked(boolean locked) {
        this.locked = locked;
        return true;
    }

    public enum Component {
        INSTANCE_SELECTOR,
        INSTANCE_CREATOR,
        SAVES_SELECTOR,
        RESOURCEPACKS_SELECTOR,
        OPTIONS_SELECTOR,
        MODS_SELECTOR,
        SETTINGS
    }

    private void onLogout() {
        LauncherApplication.userAuth.logout();
        try {
            LoginController.showOnStage(getStage());
        } catch (IOException e) {
            LOGGER.error("Failed to open login screen", e);
            getStage().close();
        }
    }
}
