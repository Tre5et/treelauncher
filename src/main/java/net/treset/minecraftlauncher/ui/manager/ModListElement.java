package net.treset.minecraftlauncher.ui.manager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModDownload;
import net.treset.mc_version_loader.mods.ModData;
import net.treset.mc_version_loader.mods.ModProvider;
import net.treset.mc_version_loader.mods.ModVersionData;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.UiLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ModListElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModListElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private ImageView logoImage;
    @FXML private Label title;
    @FXML private Label description;
    @FXML private ImageView downloadingImage;
    @FXML private Button installButton;
    @FXML private ComboBox<String> versionSelector;
    @FXML private Button disableButton;
    @FXML private FontIcon disableIcon;
    @FXML private Button deleteButton;
    @FXML private ImageView modrinthLogo;
    @FXML private ImageView curseforgeLogo;

    private boolean disabled = false;
    private LauncherMod mod;
    private ModData modData;
    private String gameVersion;
    private TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback;
    private BiFunction<Boolean, LauncherMod, Boolean> disableCallback;
    private BiConsumer<LauncherMod, ModListElement> deleteCallback;


    @Override
    public void beforeShow(Stage stage) {
        modrinthLogo.getStyleClass().remove("current");
        modrinthLogo.getStyleClass().remove("available");
        curseforgeLogo.getStyleClass().remove("current");
        curseforgeLogo.getStyleClass().remove("available");
        installButton.setDisable(true);
        downloadingImage.setVisible(false);
        versionSelector.getItems().clear();
        versionSelector.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onVersionSelected);
        });
        if(mod != null) {
            title.setText(mod.getName());
            description.setText(mod.getDescription());
            versionSelector.getItems().add(mod.getVersion());
            versionSelector.getSelectionModel().select(0);
            disabled = !mod.isEnabled();
            updateDisableStatus();
            for(LauncherModDownload d : mod.getDownloads()) {
                if("modrinth".equals(d.getProvider())) {
                    if("modrinth".equals(mod.getCurrentProvider())) {
                        modrinthLogo.getStyleClass().add("current");
                    } else {
                        modrinthLogo.getStyleClass().add("available");
                    }
                } else if("curseforge".equals(d.getProvider())) {
                    if("curseforge".equals(mod.getCurrentProvider())) {
                        curseforgeLogo.getStyleClass().add("current");
                    } else {
                        curseforgeLogo.getStyleClass().add("available");
                    }
                }
            }
        } else if(modData != null) {
            title.setText(modData.getName());
            description.setText(modData.getDescription());
            for(ModProvider p : modData.getModProviders()) {
                if(p == ModProvider.MODRINTH) {
                    modrinthLogo.getStyleClass().add("available");
                } else if(p == ModProvider.CURSEFORGE) {
                    curseforgeLogo.getStyleClass().add("available");
                }
            }
        }
    }

    @Override
    public void afterShow(Stage stage) {
        new Thread(this::populateVersionChoice).start();
        if(logoImage.getImage() == null) {
            new Thread(this::loadImage).start();
        }
    }

    private void populateVersionChoice() {
        if(modData == null && mod != null) {
            try {
                modData = mod.getModData();
            } catch (FileDownloadException e) {
                LOGGER.error("Failed to get mod data", e);
            }
        }
        if (modData != null) {
            List<ModVersionData> versionData = null;
            try {
                versionData = modData.getVersions(gameVersion, "fabric");
            } catch (FileDownloadException e) {
                LOGGER.error("Failed to get mod versions", e);
            }
            List<String> selectorList = new ArrayList<>();
            for (ModVersionData v : versionData) {
                if(mod == null || !mod.getVersion().equals(v.getVersionNumber())) {
                    selectorList.add(v.getVersionNumber());
                }
            }
            Platform.runLater(() -> versionSelector.getItems().addAll(selectorList));
        }
    }

    private void loadImage() {
        if(mod != null) {
            Image logo = new Image(mod.getIconUrl());
            Platform.runLater(() -> logoImage.setImage(logo));
        } else if(modData != null) {
            Image logo = new Image(modData.getIconUrl());
            Platform.runLater(() -> logoImage.setImage(logo));
        }
    }

    private void onVersionSelected() {
        installButton.setDisable(mod != null && mod.getVersion().equals(versionSelector.getSelectionModel().getSelectedItem()));
    }

    @FXML
    private void onInstallButtonClicked() {
        installCallback.accept(getSelectedVersion(), mod, this);
    }

    @FXML
    private void onDisableButtonClicked() {
        if(disableCallback.apply(!disabled, mod)) {
            disabled = !disabled;
            updateDisableStatus();
        }
    }

    @FXML
    private void onDeleteButtonClicked() {
        deleteCallback.accept(mod, this);
    }

    private void updateDisableStatus() {
        if(disabled) {
            installButton.setDisable(true);
            versionSelector.setDisable(true);
            disableIcon.getStyleClass().remove("select-enabled");
            disableIcon.getStyleClass().add("select-disabled");
            rootPane.setOpacity(0.5);
        } else {
            installButton.setDisable(mod != null && mod.getVersion().equals(versionSelector.getSelectionModel().getSelectedItem()));
            versionSelector.setDisable(false);
            disableIcon.getStyleClass().remove("select-disabled");
            disableIcon.getStyleClass().add("select-enabled");
            rootPane.setOpacity(1);
        }
    }

    private ModVersionData getSelectedVersion() {
        String selected = versionSelector.getSelectionModel().getSelectedItem();
        if(selected != null && modData != null) {
            try {
                for(ModVersionData v : modData.getVersions(gameVersion, "fabric")) {
                    if(selected.equals(v.getVersionNumber())) {
                        return v;
                    }
                }
            } catch (FileDownloadException e) {
                LOGGER.error("Failed to get mod versions", e);
            }
        }
        return null;
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void setInstallAvailable(boolean available) {
        installButton.setVisible(available);
    }

    public void setLocalOperationsAvailable(boolean available) {
        disableButton.setVisible(available);
        deleteButton.setVisible(available);
    }

    public LauncherMod getMod() {
        return mod;
    }

    public void setMod(LauncherMod mod) {
        this.mod = mod;
    }

    public ModData getModData() {
        return modData;
    }

    public void setModData(ModData modData) {
        this.modData = modData;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public TriConsumer<ModVersionData, LauncherMod, ModListElement> getInstallCallback() {
        return installCallback;
    }

    public void setInstallCallback(TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback) {
        this.installCallback = installCallback;
    }

    public BiFunction<Boolean, LauncherMod, Boolean> getDisableCallback() {
        return disableCallback;
    }

    public void setDisableCallback(BiFunction<Boolean, LauncherMod, Boolean> disableCallback) {
        this.disableCallback = disableCallback;
    }

    public BiConsumer<LauncherMod, ModListElement> getDeleteCallback() {
        return deleteCallback;
    }

    public void setDeleteCallback(BiConsumer<LauncherMod, ModListElement> deleteCallback) {
        this.deleteCallback = deleteCallback;
    }

    public void setDownloading(boolean downloading) {
        downloadingImage.setVisible(downloading);
        installButton.setDisable(downloading);
    }

    public static Pair<ModListElement, AnchorPane> from(LauncherMod mod, String gameVersion, TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback, BiFunction<Boolean, LauncherMod, Boolean> disableCallback, BiConsumer<LauncherMod, ModListElement> deleteCallback) throws IOException {
        Pair<ModListElement, AnchorPane> result = newInstance();
        result.getKey().setMod(mod);
        result.getKey().setGameVersion(gameVersion);
        result.getKey().setInstallCallback(installCallback);
        result.getKey().setDisableCallback(disableCallback);
        result.getKey().setDeleteCallback(deleteCallback);
        return result;
    }

    public static Pair<ModListElement, AnchorPane> from(ModData mod, String gameVersion, TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback) throws IOException {
        Pair<ModListElement, AnchorPane> result = newInstance();
        result.getKey().setModData(mod);
        result.getKey().setGameVersion(gameVersion);
        result.getKey().setInstallCallback(installCallback);
        result.getKey().setLocalOperationsAvailable(false);
        return result;
    }

    public static Pair<ModListElement, AnchorPane> newInstance() throws IOException {
        FXMLLoader loader = UiLoader.getFXMLLoader("manager/ModListElement");
        AnchorPane element = UiLoader.loadFXML(loader);
        ModListElement listElementController = loader.getController();
        return new Pair<>(listElementController, element);
    }

}
