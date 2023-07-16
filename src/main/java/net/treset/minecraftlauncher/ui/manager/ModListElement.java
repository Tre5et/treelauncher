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
import net.treset.minecraftlauncher.ui.generic.IconButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ModListElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModListElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private ImageView ivLogo;
    @FXML private Label lbTitle;
    @FXML private Label lbDescription;
    @FXML private ImageView ivDownloading;
    @FXML private Button btInstall;
    @FXML private ComboBox<String> cbVersion;
    @FXML private IconButton btDisable;
    @FXML private Button btDelete;
    @FXML private ImageView ivModrinth;
    @FXML private ImageView ivCurseforge;

    private boolean disabled = false;
    private LauncherMod mod;
    private ModData modData;
    private String gameVersion;
    private TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback;
    private BiFunction<Boolean, LauncherMod, Boolean> disableCallback;
    private BiConsumer<LauncherMod, ModListElement> deleteCallback;
    boolean versionAvailable = false;


    @Override
    public void beforeShow(Stage stage) {
        ivModrinth.getStyleClass().remove("current");
        ivModrinth.getStyleClass().remove("available");
        ivCurseforge.getStyleClass().remove("current");
        ivCurseforge.getStyleClass().remove("available");
        btInstall.setDisable(true);
        ivDownloading.setVisible(false);
        cbVersion.getItems().clear();
        cbVersion.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::onVersionSelected));
        if(mod != null) {
            lbTitle.setText(mod.getName());
            lbDescription.setText(mod.getDescription());
            cbVersion.getItems().add(mod.getVersion());
            cbVersion.getSelectionModel().select(0);
            disabled = !mod.isEnabled();
            updateDisableStatus();
            for(LauncherModDownload d : mod.getDownloads()) {
                if("modrinth".equals(d.getProvider())) {
                    if("modrinth".equals(mod.getCurrentProvider())) {
                        ivModrinth.getStyleClass().add("current");
                    } else {
                        ivModrinth.getStyleClass().add("available");
                    }
                } else if("curseforge".equals(d.getProvider())) {
                    if("curseforge".equals(mod.getCurrentProvider())) {
                        ivCurseforge.getStyleClass().add("current");
                    } else {
                        ivCurseforge.getStyleClass().add("available");
                    }
                }
            }
        } else if(modData != null) {
            lbTitle.setText(modData.getName());
            lbDescription.setText(modData.getDescription());
            for(ModProvider p : modData.getModProviders()) {
                if(p == ModProvider.MODRINTH) {
                    ivModrinth.getStyleClass().add("available");
                } else if(p == ModProvider.CURSEFORGE) {
                    ivCurseforge.getStyleClass().add("available");
                }
            }
        }
    }

    @Override
    public void afterShow(Stage stage) {
        new Thread(this::populateVersionChoice).start();
        if(ivLogo.getImage() == null) {
            new Thread(this::loadImage).start();
        }
    }

    private void populateVersionChoice() {
        if(modData == null && mod != null && mod.getDownloads() != null && !mod.getDownloads().isEmpty()) {
            try {
                modData = mod.getModData();
            } catch (FileDownloadException e) {
                LOGGER.error("Failed to get mod data", e);
            }
        }
        if (modData != null) {
            List<ModVersionData> versionData;
            try {
                versionData = modData.getVersions(gameVersion, "fabric");
            } catch (FileDownloadException e) {
                LOGGER.error("Failed to get mod versions", e);
                return;
            }
            List<String> selectorList = new ArrayList<>();
            int currentIndex = -1;
            for (ModVersionData v : versionData) {
                if(mod != null && mod.getVersion().equals(v.getVersionNumber())) {
                    versionAvailable = true;
                    currentIndex = selectorList.size();
                }
                selectorList.add(v.getVersionNumber());
            }
            if(currentIndex == -1 && mod != null) {
                currentIndex = selectorList.size();
                selectorList.add(mod.getVersion());
            }
            int finalCurrentIndex = currentIndex;
            Platform.runLater(() -> {
                cbVersion.getItems().clear();
                cbVersion.getItems().addAll(selectorList);
                if(finalCurrentIndex != -1) {
                    cbVersion.getSelectionModel().select(finalCurrentIndex);
                }
            });
        }
    }

    private void loadImage() {
        if(mod != null && mod.getIconUrl() != null && !mod.getIconUrl().isBlank()) {
            Image logo = new Image(mod.getIconUrl());
            Platform.runLater(() -> ivLogo.setImage(logo));
        } else if(modData != null && modData.getIconUrl() != null && !modData.getIconUrl().isBlank()) {
            Image logo = new Image(modData.getIconUrl());
            Platform.runLater(() -> ivLogo.setImage(logo));
        }
    }

    private void onVersionSelected() {
        btInstall.setDisable(mod != null && mod.getVersion().equals(cbVersion.getSelectionModel().getSelectedItem()));
    }

    @FXML
    private void onInstall() {
        installCallback.accept(getSelectedVersion(), mod, this);
    }

    @FXML
    private void onDisable() {
        if(disableCallback.apply(!disabled, mod)) {
            disabled = !disabled;
            updateDisableStatus();
        }
    }

    @FXML
    private void onDelete() {
        deleteCallback.accept(mod, this);
    }

    private void updateDisableStatus() {
        if(disabled) {
            btInstall.setDisable(true);
            cbVersion.setDisable(true);
            btDisable.getStyleClass().add("disabled");
            rootPane.setOpacity(0.5);
        } else {
            btInstall.setDisable(mod != null && mod.getVersion().equals(cbVersion.getSelectionModel().getSelectedItem()));
            cbVersion.setDisable(false);
            btDisable.getStyleClass().remove("disabled");
            rootPane.setOpacity(1);
        }
    }

    private ModVersionData getSelectedVersion() {
        String selected = cbVersion.getSelectionModel().getSelectedItem();
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

    public void checkUpdate() {
        cbVersion.getSelectionModel().select(0);
    }

    public void confirmVersionChange(boolean enableUpdated) {
        if(mod == null || !mod.getVersion().equals(cbVersion.getSelectionModel().getSelectedItem())) {
            if(enableUpdated && disabled) {
                onDisable();
            }
            if(!disabled) {
                onInstall();
            }
        }
    }

    public void disableNoVersion() {
        if(!versionAvailable && !disabled) {
            onDisable();
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void setInstallAvailable(boolean available) {
        btInstall.setVisible(available);
    }

    public void setLocalOperationsAvailable(boolean available) {
        btDisable.setVisible(available);
        btDelete.setVisible(available);
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
        ivDownloading.setVisible(downloading);
        btInstall.setDisable(downloading);
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
