package net.treset.minecraftlauncher.ui.nav;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.MainController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.ImageUtil;

import java.util.function.Consumer;

public class NavbarElement extends UiElement {
    @FXML private ImageView profileImage;

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
        componentActivator.accept(MainController.Component.SETTINGS);
    }

    @Override
    public void beforeShow(Stage stage) {}

    @Override
    public void afterShow(Stage stage) {
        new Thread(this::setProfileImage).start();
    }

    private void setProfileImage() {
        if(LauncherApplication.userAuth.isLoggedIn()) {
            try {
                Image profileImage = ImageUtil.rescale(LauncherApplication.userAuth.getUserIcon(), 4);
                Platform.runLater(() -> this.profileImage.setImage(profileImage));

            } catch (FileDownloadException e) {
                LauncherApplication.displayError(e);
            }
        }
    }

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
