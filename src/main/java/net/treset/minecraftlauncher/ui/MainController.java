package net.treset.minecraftlauncher.ui;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.ui.base.GenericUiController;
import net.treset.minecraftlauncher.ui.create.InstanceCreatorElement;
import net.treset.minecraftlauncher.ui.instance.InstanceSelectorElement;
import net.treset.minecraftlauncher.ui.nav.NavbarElement;
import net.treset.minecraftlauncher.ui.title.TitlebarElement;

import java.io.IOException;

public class MainController extends GenericUiController {
    @FXML private TitlebarElement titlebarController;
    @FXML private InstanceSelectorElement instanceSelectorController;
    @FXML private InstanceCreatorElement instanceCreatorController;
    @FXML private NavbarElement navbarController;

    boolean locked = false;

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        titlebarController.init(this::setLocked, this::getLocked);
        navbarController.init(this::setLocked, this::getLocked);
        navbarController.setComponentActivator(this::activate);
        instanceSelectorController.init(this::setLocked, this::getLocked);
        instanceCreatorController.init(this::setLocked, this::getLocked);
        activate(Component.INSTANCE_SELECTOR);
    }

    public static MainController showOnStage(Stage stage) throws IOException {
        return showOnStage(stage, "MainScreen", "launcher.name");
    }

    public void activate(Component component) {
        if(getLocked()) return;
        switch(component) {
            case INSTANCE_SELECTOR:
                instanceSelectorController.setVisible(true);
                instanceCreatorController.setVisible(false);
                break;
            case INSTANCE_CREATOR:
                instanceSelectorController.setVisible(false);
                instanceCreatorController.setVisible(true);
                break;
        }
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
        INSTANCE_CREATOR;
    }
}
