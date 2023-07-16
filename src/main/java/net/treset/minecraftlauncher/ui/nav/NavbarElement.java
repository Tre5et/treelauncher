package net.treset.minecraftlauncher.ui.nav;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.MainController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.ImageUtil;
import net.treset.minecraftlauncher.ui.generic.IconButton;

import java.util.function.Function;

public class NavbarElement extends UiElement {
    @FXML private IconButton homeButton;
    @FXML private Button addButton;
    @FXML private Button savesButton;
    @FXML private Button resourcepacksButton;
    @FXML private Button optionsButton;
    @FXML private Button modsButton;
    @FXML private Button profileButton;
    @FXML private ImageView profileImage;

    private boolean locked = false;
    private Stage stage;
    private Function<MainController.Component, Boolean> componentActivator;

    @FXML
    private void onHomeButtonClicked() {
        if(componentActivator.apply(MainController.Component.INSTANCE_SELECTOR)) {
            deselectAll();
            homeButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onAddButtonClicked() {
        if(componentActivator.apply(MainController.Component.INSTANCE_CREATOR)) {
            deselectAll();
            addButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onSavesButtonClicked() {
        if(componentActivator.apply(MainController.Component.SAVES_SELECTOR)) {
            deselectAll();
            savesButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onResourcepacksButtonClicked() {
        if(componentActivator.apply(MainController.Component.RESOURCEPACKS_SELECTOR)) {
            deselectAll();
            resourcepacksButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onOptionsButtonClicked() {
        if(componentActivator.apply(MainController.Component.OPTIONS_SELECTOR)) {
            deselectAll();
            optionsButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onModsButtonClicked() {
        if(componentActivator.apply(MainController.Component.MODS_SELECTOR)) {
            deselectAll();
            modsButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onProfileButtonClicked() {
        if(componentActivator.apply(MainController.Component.SETTINGS)) {
            deselectAll();
            profileButton.getStyleClass().add("selected");
        }
    }

    @Override
    public void beforeShow(Stage stage) {}

    @Override
    public void afterShow(Stage stage) {
        new Thread(this::setProfileImage).start();
    }

    private void deselectAll() {
        homeButton.getStyleClass().remove("selected");
        addButton.getStyleClass().remove("selected");
        savesButton.getStyleClass().remove("selected");
        resourcepacksButton.getStyleClass().remove("selected");
        optionsButton.getStyleClass().remove("selected");
        modsButton.getStyleClass().remove("selected");
        profileButton.getStyleClass().remove("selected");
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

    public Function<MainController.Component, Boolean> getComponentActivator() {
        return componentActivator;
    }

    public void setComponentActivator(Function<MainController.Component, Boolean> componentActivator) {
        this.componentActivator = componentActivator;
    }

    @Override
    public void triggerHomeAction() {
        onHomeButtonClicked();
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
