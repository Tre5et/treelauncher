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
import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.mods.ModData;
import net.treset.mc_version_loader.mods.ModVersionData;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;

public class ModsSearchElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(ModsSearchElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private Button searchButton;
    @FXML private TextField searchField;
    @FXML private Label loadingLabel;
    @FXML private VBox resultsContainer;

    private String gameVersion;
    private String loaderType;
    private TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback;
    private Runnable backCallback;
    private List<LauncherMod> currentMods;

    public void init(String gameVersion, String loaderType, TriConsumer<ModVersionData, LauncherMod, ModListElement> installCallback, Runnable backCallback, List<LauncherMod> currentMods) {
        this.gameVersion = gameVersion;
        this.loaderType = loaderType;
        this.installCallback = installCallback;
        this.backCallback = backCallback;
        this.currentMods = currentMods;
    }

    @Override
    public void beforeShow(Stage stage) {
        searchField.setText(null);
        resultsContainer.getChildren().clear();
    }

    @Override
    public void afterShow(Stage stage) {
    }

    @FXML
    private void onSearchButtonClicked() {
        if(searchField.getText() == null || searchField.getText().isEmpty())
            return;
        resultsContainer.getChildren().clear();
        loadingLabel.setVisible(true);
        new Thread(this::populateResults).start();
    }

    @FXML
    private void onBackButtonClicked() {
        backCallback.run();
    }

    @FXML
    private void onSearchKeyPress(KeyEvent event) {
        if(event.getCode().getName().equals("Enter")) {
            onSearchButtonClicked();
        }
    }

    private void populateResults() {
        List<ModData> results;
        try {
            results = VersionLoader.searchCombinedMods(searchField.getText(), gameVersion, loaderType, 20, 0);
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
            loadingLabel.setVisible(false);
            resultsContainer.getChildren().addAll(panes);
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
