package net.treset.minecraftlauncher.ui.manager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.minecraft.MinecraftUtil;
import net.treset.mc_version_loader.minecraft.MinecraftVersion;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.lists.ChangeEvent;
import net.treset.minecraftlauncher.ui.generic.lists.ModContentElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ModsManagerElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsManagerElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private VBox vbCurrentMods;
    @FXML private CheckBox chUpdate;
    @FXML private CheckBox chEnable;
    @FXML private CheckBox chDisable;
    @FXML private VBox currentModsContainer;
    @FXML private ComboBox<String> cbVersion;
    @FXML private CheckBox chSnapshots;
    @FXML private Button btChange;
    @FXML private ModsSearchElement icModSearchController;
    @FXML private PopupElement icPopupController;

    private final ModManagerChangeCallback changeCallback = new ModManagerChangeCallback();
    private Pair<LauncherManifest, LauncherModsDetails> details;
    private List<ModContentElement> elements;

    public void setLauncherMods(Pair<LauncherManifest, LauncherModsDetails> details) {
        this.details = details;
    }

    @FXML
    private void onAdd() {
        vbCurrentMods.setVisible(false);
        icModSearchController.setVisible(true);
    }

    @FXML
    private void onUpdate(){
        for(ModContentElement e : elements) {
            e.update(chUpdate.isSelected(), chEnable.isSelected(), chDisable.isSelected());
        }
    }

    @FXML
    private void onCheckUpdate() {
        chEnable.setDisable(!chUpdate.isSelected());
        if(!chUpdate.isSelected()) {
            chEnable.setSelected(false);
        }
    }

    @FXML
    private void onCheckSnapshots() {
        populateVersionChoice();
    }

    @FXML
    private void onChange() {
        if(cbVersion.getSelectionModel().getSelectedItem() != null && !Objects.equals(cbVersion.getSelectionModel().getSelectedItem(), details.getValue().getModsVersion())) {
            icPopupController.setType(PopupElement.PopupType.WARNING);
            icPopupController.setContent("mods.manager.popup.change.title", "mods.manager.popup.change.message");
            icPopupController.clearControls();
            icPopupController.addButtons(
                    new PopupElement.PopupButton(PopupElement.ButtonType.NEGATIVE,
                            "mods.manager.popup.change.cancel", "cancelButton",
                            this::onVersionChangeCanceled),
                    new PopupElement.PopupButton(PopupElement.ButtonType.POSITIVE,
                            "mods.manager.popup.change.accept", "acceptButton",
                            this::onVersionChangeAccepted)
            );
            icPopupController.setVisible(true);
        }
    }

    private void onVersionChangeAccepted(String id) {
        icPopupController.setVisible(false);
        details.getValue().setModsVersion(cbVersion.getSelectionModel().getSelectedItem());
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        icModSearchController.init(details.getValue().getModsVersion(), details.getValue().getModsType(), this::onSearchBackClicked, changeCallback, details);
        onVersionSelected();
        reloadMods();
    }

    private void onVersionChangeCanceled(String id) {
        icPopupController.setVisible(false);
        cbVersion.getSelectionModel().select(details.getValue().getModsVersion());
        onVersionSelected();
    }

    private void onVersionSelected() {
        btChange.setDisable(details.getValue().getModsVersion().equals(cbVersion.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void beforeShow(Stage stage){
        if(details == null || details.getValue().getMods() == null)
            return;
        icModSearchController.init(details.getValue().getModsVersion(), details.getValue().getModsType(), this::onSearchBackClicked, changeCallback, details);
        chSnapshots.setSelected(false);
        cbVersion.getItems().clear();
        cbVersion.setDisable(true);
        cbVersion.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::onVersionSelected));
        icPopupController.setVisible(false);
        vbCurrentMods.setVisible(true);
        icModSearchController.setVisible(false);
    }

    private void reloadMods() {
        currentModsContainer.getChildren().clear();
        new Thread(() -> {
            long time = System.currentTimeMillis();
            elements = new ArrayList<>(details.getValue().getMods().parallelStream()
                    .map(m -> new ModContentElement(m, details.getValue().getModsVersion(), changeCallback, details, true))
                    .sorted(Comparator.comparing(e -> e.getLauncherMod().getName()))
                    .toList());
            Platform.runLater(() -> currentModsContainer.getChildren().addAll(elements));
            LOGGER.info("Loaded {} mods in {}ms", elements.size(), System.currentTimeMillis() - time);
        }).start();

    }

    private void populateVersionChoice() {
        cbVersion.getItems().clear();
        cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.loading"));
        cbVersion.setDisable(true);
        cbVersion.getItems().add(details.getValue().getModsVersion());
        cbVersion.getSelectionModel().select(0);
        new Thread(() -> {
            try {
                List <String> names = (chSnapshots.isSelected() ? MinecraftUtil.getVersions() : MinecraftUtil.getReleases()).stream()
                        .map(MinecraftVersion::getId)
                        .filter(s -> !s.equals(details.getValue().getModsVersion()))
                        .toList();
                Platform.runLater(() -> {
                    cbVersion.getItems().addAll(names);
                    cbVersion.setPromptText(LauncherApplication.stringLocalizer.get("creator.version.prompt.version"));
                    cbVersion.setDisable(false);
                });
            } catch (FileDownloadException e) {
               LauncherApplication.displayError(e);
            }
        }).start();
    }

    @Override
    public void afterShow(Stage stage) {
        reloadMods();
        populateVersionChoice();
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void onSearchBackClicked() {
        vbCurrentMods.setVisible(true);
        icModSearchController.setVisible(false);
        reloadMods();
    }

    private void writeModList() {
        try {
            details.getValue().writeToFile(details.getKey().getDirectory() + details.getKey().getDetails());
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
    }

    private class ModManagerChangeCallback implements ChangeEvent<LauncherMod, ModContentElement> {

        @Override
        public void update() {
            writeModList();
        }

        @Override
        public void add(LauncherMod value) {
            int index = elements.size();
            for(int i = 0; i < elements.size(); i++) {
                if(elements.get(i).getLauncherMod().getName().compareToIgnoreCase(value.getName()) > 0) {
                    index = i;
                    break;
                }
            }
            int finalIndex = index;
            elements.add(index, new ModContentElement(value, details.getValue().getModsVersion(), this, details, true));
            Platform.runLater(() -> currentModsContainer.getChildren().add(finalIndex, elements.get(finalIndex)));
            ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
            mods.add(value);
            details.getValue().setMods(mods);
            writeModList();

        }

        @Override
        public void remove(ModContentElement element) {
            currentModsContainer.getChildren().remove(element);
            elements.remove(element);
            ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
            mods.remove(element.getLauncherMod());
            details.getValue().setMods(mods);
            writeModList();
        }

        @Override
        public void change(LauncherMod oldValue, LauncherMod newValue) {
            ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
            mods.remove(oldValue);
            mods.add(newValue);
            details.getValue().setMods(mods);
            writeModList();
        }
    }
}
