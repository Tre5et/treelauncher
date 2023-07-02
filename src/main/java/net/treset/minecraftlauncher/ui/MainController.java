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

    @FXML private TitlebarElement titlebarController;
    @FXML private InstanceSelectorElement instanceSelectorController;
    @FXML private InstanceCreatorElement instanceCreatorController;
    @FXML private SavesSelectorElement savesSelectorController;
    @FXML private ResourcepacksSelectorElement resourcepacksSelectorController;
    @FXML private OptionsSelectorElement optionsSelectorController;
    @FXML private ModsSelectorElement modsSelectorController;
    @FXML private SettingsElement settingsController;
    @FXML private NavbarElement navbarController;
    @FXML private PopupElement popupController;

    boolean locked = false;

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        titlebarController.init(this, this::setLocked, this::getLocked);
        titlebarController.beforeShow(stage);
        navbarController.init(this, this::setLocked, this::getLocked);
        navbarController.setComponentActivator(this::activate);
        navbarController.beforeShow(stage);
        instanceSelectorController.init(this, this::setLocked, this::getLocked);
        instanceCreatorController.init(this, this::setLocked, this::getLocked);
        savesSelectorController.init(this, this::setLocked, this::getLocked);
        resourcepacksSelectorController.init(this, this::setLocked, this::getLocked);
        optionsSelectorController.init(this, this::setLocked, this::getLocked);
        modsSelectorController.init(this, this::setLocked, this::getLocked);
        settingsController.init(this::onLogout);
        activate(Component.INSTANCE_SELECTOR);
    }

    @Override
    public void afterShow(Stage stage) {
        super.afterShow(stage);
        titlebarController.afterShow(stage);
        navbarController.afterShow(stage);
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
                instanceSelectorController.setVisible(true);
            }
            case INSTANCE_CREATOR -> {
                setAllInvisible();
                instanceCreatorController.setVisible(true);
            }
            case SAVES_SELECTOR -> {
                setAllInvisible();
                savesSelectorController.setVisible(true);
            }
            case RESOURCEPACKS_SELECTOR -> {
                setAllInvisible();
                resourcepacksSelectorController.setVisible(true);
            }
            case OPTIONS_SELECTOR -> {
                setAllInvisible();
                optionsSelectorController.setVisible(true);
            }
            case MODS_SELECTOR -> {
                setAllInvisible();
                modsSelectorController.setVisible(true);
            }
            case SETTINGS -> {
                setAllInvisible();
                settingsController.setVisible(true);
            }
        }
        return true;
    }

    private void setAllInvisible() {
        instanceSelectorController.setVisible(false);
        instanceCreatorController.setVisible(false);
        savesSelectorController.setVisible(false);
        resourcepacksSelectorController.setVisible(false);
        optionsSelectorController.setVisible(false);
        modsSelectorController.setVisible(false);
        settingsController.setVisible(false);
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
            e.printStackTrace();
            getStage().close();
        }
    }
}
