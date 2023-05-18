package net.treset.minecraftlauncher.ui.nav;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.ui.MainController;
import net.treset.minecraftlauncher.ui.base.UiElement;

import java.util.function.Consumer;

public class NavbarElement extends UiElement {
    private boolean locked = false;
    private Stage stage;
    private Consumer<MainController.Component> componentActivator;

    @FXML
    private void onHomeButtonClicked() {
        componentActivator.accept(MainController.Component.INSTANCE_SELECTOR);
    }

    @FXML
    private void onAddButtonClicked() {
        componentActivator.accept(MainController.Component.INSTANCE_CREATOR);
    }

    @FXML
    private void onSavesButtonClicked() {
        componentActivator.accept(MainController.Component.SAVES_SELECTOR);
    }

    @FXML
    private void onResourcepacksButtonClicked() {
        componentActivator.accept(MainController.Component.RESOURCEPACKS_SELECTOR);
    }

    @FXML
    private void onOptionsButtonClicked() {
        componentActivator.accept(MainController.Component.OPTIONS_SELECTOR);
    }

    @FXML
    private void onModsButtonClicked() {
        componentActivator.accept(MainController.Component.MODS_SELECTOR);
    }

    @FXML
    private void onProfileButtonClicked() {
    }

    @Override
    public void beforeShow(Stage stage) {}

    @Override
    public void afterShow(Stage stage) {}

    public Consumer<MainController.Component> getComponentActivator() {
        return componentActivator;
    }

    public void setComponentActivator(Consumer<MainController.Component> componentActivator) {
        this.componentActivator = componentActivator;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setRootVisible(boolean visible) {}
}
