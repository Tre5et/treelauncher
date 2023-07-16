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
    @FXML private IconButton btHome;
    @FXML private Button btAdd;
    @FXML private Button btSaves;
    @FXML private Button btResourcepacks;
    @FXML private Button btOptions;
    @FXML private Button btMods;
    @FXML private Button btProfile;
    @FXML private ImageView ivProfile;

    private boolean locked = false;
    private Stage stage;
    private Function<MainController.Component, Boolean> componentActivator;

    @FXML
    private void onHome() {
        if(componentActivator.apply(MainController.Component.INSTANCE_SELECTOR)) {
            deselectAll();
            btHome.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onAdd() {
        if(componentActivator.apply(MainController.Component.INSTANCE_CREATOR)) {
            deselectAll();
            btAdd.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onSaves() {
        if(componentActivator.apply(MainController.Component.SAVES_SELECTOR)) {
            deselectAll();
            btSaves.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onResourcepacks() {
        if(componentActivator.apply(MainController.Component.RESOURCEPACKS_SELECTOR)) {
            deselectAll();
            btResourcepacks.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onOptions() {
        if(componentActivator.apply(MainController.Component.OPTIONS_SELECTOR)) {
            deselectAll();
            btOptions.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onMods() {
        if(componentActivator.apply(MainController.Component.MODS_SELECTOR)) {
            deselectAll();
            btMods.getStyleClass().add("selected");
        }
    }

    @FXML
    private void onProfile() {
        if(componentActivator.apply(MainController.Component.SETTINGS)) {
            deselectAll();
            btProfile.getStyleClass().add("selected");
        }
    }

    @Override
    public void beforeShow(Stage stage) {}

    @Override
    public void afterShow(Stage stage) {
        new Thread(this::setProfileImage).start();
    }

    private void deselectAll() {
        btHome.getStyleClass().remove("selected");
        btAdd.getStyleClass().remove("selected");
        btSaves.getStyleClass().remove("selected");
        btResourcepacks.getStyleClass().remove("selected");
        btOptions.getStyleClass().remove("selected");
        btMods.getStyleClass().remove("selected");
        btProfile.getStyleClass().remove("selected");
    }

    private void setProfileImage() {
        if(LauncherApplication.userAuth.isLoggedIn()) {
            try {
                Image profileImage = ImageUtil.rescale(LauncherApplication.userAuth.getUserIcon(), 4);
                Platform.runLater(() -> this.ivProfile.setImage(profileImage));

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
        onHome();
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
