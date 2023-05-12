package net.treset.minecraftlauncher.ui.controller.instances;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.launching.GameLauncher;
import net.treset.minecraftlauncher.ui.controller.GenericUiController;
import net.treset.minecraftlauncher.ui.controller.ListElementController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstancesUiController extends GenericUiController {
    private LauncherFiles files;
    private List<Pair<ListElementController, GridPane>> instances = new ArrayList<>();
    private InstanceData currentInstance;
    private boolean locked;
    @FXML public VBox instanceContainer;
    @FXML public Button playButton;
    @FXML public Label instanceDetailsTitle;
    @FXML public InstanceDetailsController instanceDetailsController;

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        files = new LauncherFiles();
        files.reloadAll();
        for(Pair<LauncherManifest, LauncherInstanceDetails> instance : files.getInstanceComponents()) {
            try {
                instances.add(ListElementController.from(InstanceData.of(instance, files)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for(Pair<ListElementController, GridPane> instance : instances) {
            instanceContainer.getChildren().add(instance.getValue());
            instance.getKey().setSelectionAccepted(this::allowSelection);
            instance.getKey().setSelectionListeners(List.of(this::onSelected));
            instance.getKey().beforeShow();
        }
    }

    private void onSelected(InstanceData instanceData, boolean selected) {
        if(selected) {
            for(Pair<ListElementController, GridPane> instance : instances) {
                if(instance.getKey().getInstanceData() != instanceData) {
                    instance.getKey().select(false, true, false);
                }
            }
            playButton.setDisable(false);
            currentInstance = instanceData;
            instanceDetailsTitle.setText(instanceData.getInstance().getKey().getName());
            instanceDetailsController.populate(instanceData);
            instanceDetailsController.setVisible(true);
        } else {
            playButton.setDisable(true);
            currentInstance = null;
            instanceDetailsController.setVisible(false);
        }
    }

    private boolean allowSelection(InstanceData instanceData, boolean selected) {
        return !locked;
    }

    public static InstancesUiController showOnStage(Stage stage) throws IOException {
        return showOnStage(stage, "InstancesUi", "launcher.name");
    }

    public void onPlayButtonClicked() {
        if(currentInstance != null) {
            locked = true;
            playButton.setDisable(true);
            GameLauncher launcher = new GameLauncher(currentInstance, files, LauncherApplication.userAuth.getMinecraftUser(), List.of(this::onGameExit));
            launcher.launch(false);
        }
    }

    private void onGameExit(String s) {
        locked = false;
        Platform.runLater(() -> playButton.setDisable(false));
    }
}
