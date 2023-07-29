package net.treset.minecraftlauncher.ui.manager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.mods.ModData;
import net.treset.mc_version_loader.mods.ModUtil;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.lists.ChangeEvent;
import net.treset.minecraftlauncher.ui.generic.lists.ModContentElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private ChangeEvent<LauncherMod, ModContentElement> managerChangeCallback;
    private final ModSearchElementChangeCallback changeCallback = new ModSearchElementChangeCallback();
    private Pair<LauncherManifest, LauncherModsDetails> details;
    private Runnable backCallback;

    public void init(String gameVersion, String loaderType, Runnable backCallback, ChangeEvent<LauncherMod, ModContentElement> managerChangeCallback, Pair<LauncherManifest, LauncherModsDetails> details) {
        this.gameVersion = gameVersion;
        this.loaderType = loaderType;
        this.backCallback = backCallback;
        this.managerChangeCallback = managerChangeCallback;
        this.details = details;
        icAddModController.init(details, changeCallback);
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
        List<ModContentElement> elements = results.parallelStream()
                .map(m -> new ModContentElement(m, details.getValue().getModsVersion(), changeCallback, details))
                .sorted((e1, e2) -> Integer.compare(e2.getModData().getDownloadsCount(), e1.getModData().getDownloadsCount()))
                .toList();
        elements.forEach(e -> e.setLauncherMod(getLocalMod(e.getModData())));

        Platform.runLater(() -> {
            lbLoading.setVisible(false);
            vbResults.getChildren().addAll(elements);
        });
    }

    private LauncherMod getLocalMod(ModData modData) {
        for(LauncherMod m : details.getValue().getMods()) {
            if(m.getName().equals(modData.getName())) {
                return m;
            }
        }
        return null;
    }

    private class ModSearchElementChangeCallback implements ChangeEvent<LauncherMod, ModContentElement> {

        @Override
        public void update() {
            managerChangeCallback.update();
        }

        @Override
        public void add(LauncherMod value) {
            ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
            mods.add(value);
            details.getValue().setMods(mods);
            for(Node element : vbResults.getChildren()) {
                if(element instanceof ModContentElement modElement && modElement.getModData().getName().equals(value.getName())) {
                    modElement.setLauncherMod(value);
                    break;
                }
            }
            managerChangeCallback.update();
        }

        @Override
        public void remove(ModContentElement element) {
            throw new UnsupportedOperationException("Remove not permitted in search");
        }

        @Override
        public void change(LauncherMod oldValue, LauncherMod newValue) {
            ArrayList<LauncherMod> mods = new ArrayList<>(details.getValue().getMods());
            mods.remove(oldValue);
            mods.add(newValue);
            details.getValue().setMods(mods);
            managerChangeCallback.update();
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
