package net.treset.minecraftlauncher.ui;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.ui.base.GenericUiController;
import net.treset.minecraftlauncher.ui.components.ModsSelectorElement;
import net.treset.minecraftlauncher.ui.components.OptionsSelectorElement;
import net.treset.minecraftlauncher.ui.components.ResourcepacksSelectorElement;
import net.treset.minecraftlauncher.ui.create.InstanceCreatorElement;
import net.treset.minecraftlauncher.ui.instance.InstanceSelectorElement;
import net.treset.minecraftlauncher.ui.nav.NavbarElement;
import net.treset.minecraftlauncher.ui.components.SavesSelectorElement;
import net.treset.minecraftlauncher.ui.title.TitlebarElement;

import java.io.IOException;

public class MainController extends GenericUiController {
    @FXML private TitlebarElement titlebarController;
    @FXML private InstanceSelectorElement instanceSelectorController;
    @FXML private InstanceCreatorElement instanceCreatorController;
    @FXML private SavesSelectorElement savesSelectorController;
    @FXML private ResourcepacksSelectorElement resourcepacksSelectorController;
    @FXML private OptionsSelectorElement optionsSelectorController;
    @FXML private ModsSelectorElement modsSelectorController;
    @FXML private NavbarElement navbarController;

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
        }
    }

    private void setAllInvisible() {
        instanceSelectorController.setVisible(false);
        instanceCreatorController.setVisible(false);
        savesSelectorController.setVisible(false);
        resourcepacksSelectorController.setVisible(false);
        optionsSelectorController.setVisible(false);
        modsSelectorController.setVisible(false);
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
        MODS_SELECTOR;
    }
}
