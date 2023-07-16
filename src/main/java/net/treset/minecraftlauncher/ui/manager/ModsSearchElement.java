package net.treset.minecraftlauncher.ui.manager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.mods.ModData;
import net.treset.mc_version_loader.mods.ModUtil;
import net.treset.mc_version_loader.mods.ModVersionData;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModsSearchElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsSearchElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private Button btSearch;
    @FXML private TextField tfSearch;
    @FXML private Label lbLoading;
    @FXML private VBox vbResults;
    @FXML private Button btAdd;
    @FXML private AddModElement icAddModController;

    private String gameVersion;
    private String loaderType;
    private TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback;
    private Runnable backCallback;
    private List<LauncherMod> currentMods;

    public void init(String gameVersion, String loaderType, TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback, Runnable backCallback, List<LauncherMod> currentMods, BiConsumer<LauncherMod, File> addCallback) {
        this.gameVersion = gameVersion;
        this.loaderType = loaderType;
        this.installCallback = installCallback;
        this.backCallback = backCallback;
        this.currentMods = currentMods;
        icAddModController.init(addCallback);
    }

    @Override
    public void beforeShow(Stage stage) {
        icAddModController.setVisible(false);
        tfSearch.setText(null);
        vbResults.getChildren().clear();
    }

    @Override
    public void afterShow(Stage stage) {
    }

    @FXML
    private void onSearch() {
        if(tfSearch.getText() == null || tfSearch.getText().isEmpty())
            return;
        vbResults.getChildren().clear();
        lbLoading.setVisible(true);
        new Thread(this::populateResults).start();
    }

    @FXML
    private void onAdd() {
        icAddModController.setVisible(true);
    }

    @FXML
    private void onBack() {
        backCallback.run();
    }

    @FXML
    private void onSearchKeyPress(KeyEvent event) {
        if(event.getCode().getName().equals("Enter")) {
            onSearch();
        }
    }

    private void populateResults() {
        List<ModData> results;
        try {
            results = ModUtil.searchCombinedMods(tfSearch.getText(), gameVersion, loaderType, 20, 0);
        } catch (FileDownloadException e) {
            LOGGER.error("Failed to search for mods", e);
            return;
        }
        ArrayList<Pair<ModListElement, AnchorPane>> elements = new ArrayList<>();
        for(ModData m : results) {
            try {
                Pair<ModListElement, AnchorPane> currentElement = ModListElement.from(m, gameVersion, installCallback);
                currentElement.getKey().setMod(getLocalMod(m));
                elements.add(currentElement);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        List<AnchorPane> panes = elements.stream().map(Pair::getValue).toList();
        for(Pair<ModListElement, AnchorPane> element : elements) {
            element.getKey().beforeShow(null);
        }
        Platform.runLater(() -> {
            lbLoading.setVisible(false);
            vbResults.getChildren().addAll(panes);
        });
        for(Pair<ModListElement, AnchorPane> element : elements) {
            element.getKey().afterShow(null);
        }
    }

    private LauncherMod getLocalMod(ModData modData) {
        for(LauncherMod m : currentMods) {
            if(m.getName().equals(modData.getName())) {
                return m;
            }
        }
        return null;
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public List<LauncherMod> getCurrentMods() {
        return currentMods;
    }

    public void setCurrentMods(List<LauncherMod> currentMods) {
        this.currentMods = currentMods;
    }
}
