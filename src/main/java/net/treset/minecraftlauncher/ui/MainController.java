package net.treset.minecraftlauncher.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.GenericUiController;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.login.LoginController;
import net.treset.minecraftlauncher.ui.selector.ModsSelectorElement;
import net.treset.minecraftlauncher.ui.selector.OptionsSelectorElement;
import net.treset.minecraftlauncher.ui.selector.ResourcepacksSelectorElement;
import net.treset.minecraftlauncher.ui.create.InstanceCreatorElement;
import net.treset.minecraftlauncher.ui.selector.InstanceSelectorElement;
import net.treset.minecraftlauncher.ui.nav.NavbarElement;
import net.treset.minecraftlauncher.ui.selector.SavesSelectorElement;
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
        titlebarController.init(this, this::setLocked, this::getLocked, this::displaySevereError);
        titlebarController.beforeShow(stage);
        navbarController.init(this, this::setLocked, this::getLocked, this::displaySevereError);
        navbarController.setComponentActivator(this::activate);
        navbarController.beforeShow(stage);
        instanceSelectorController.init(this, this::setLocked, this::getLocked, this::displaySevereError);
        instanceCreatorController.init(this, this::setLocked, this::getLocked, this::displaySevereError);
        savesSelectorController.init(this, this::setLocked, this::getLocked, this::displaySevereError);
        resourcepacksSelectorController.init(this, this::setLocked, this::getLocked, this::displaySevereError);
        optionsSelectorController.init(this, this::setLocked, this::getLocked, this::displaySevereError);
        modsSelectorController.init(this, this::setLocked, this::getLocked, this::displaySevereError);
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

    public void activate(Component component) {
        if(getLocked()) return;
        switch(component) {
            case INSTANCE_SELECTOR:
                setAllInvisible();
                instanceSelectorController.setVisible(true);
                break;
            case INSTANCE_CREATOR:
                setAllInvisible();
                instanceCreatorController.setVisible(true);
                break;
            case SAVES_SELECTOR:
                setAllInvisible();
                savesSelectorController.setVisible(true);
                break;
            case RESOURCEPACKS_SELECTOR:
                setAllInvisible();
                resourcepacksSelectorController.setVisible(true);
                break;
            case OPTIONS_SELECTOR:
                setAllInvisible();
                optionsSelectorController.setVisible(true);
                break;
            case MODS_SELECTOR:
                setAllInvisible();
                modsSelectorController.setVisible(true);
                break;
            case SETTINGS:
                setAllInvisible();
                settingsController.setVisible(true);
                break;
        }
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

    private void displaySevereError(Exception e) {
        LOGGER.error("AN ERROR OCCURRED", e);
        popupController.setType(PopupElement.PopupType.ERROR);
        popupController.setTitle("error.severe.title");
        popupController.setMessage("error.severe.message", e.getMessage());
        popupController.clearButtons();
        popupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.NEGATIVE,
                        "error.severe.close",
                        "confirm",
                        id -> Platform.exit()
                )
        );
        popupController.setVisible(true);
    }
}
