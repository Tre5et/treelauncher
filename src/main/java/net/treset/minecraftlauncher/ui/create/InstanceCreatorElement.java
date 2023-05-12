package net.treset.minecraftlauncher.ui.create;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiElement;

public class InstanceCreatorElement extends UiElement {
    @FXML private GridPane rootPane;
    @FXML private SavesCreatorElement savesCreatorController;
    @FXML private OptionsCreatorElement optionsCreatorController;
    @FXML private Button createButton;

    private LauncherFiles launcherFiles;

    @FXML
    private void onCreateButtonClicked() {
        if(savesCreatorController.checkCreateReady()) {
            savesCreatorController.create();
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        launcherFiles = new LauncherFiles();
        launcherFiles.reloadAll();
        savesCreatorController.setPrerequisites(launcherFiles.getSavesComponents(), launcherFiles.getLauncherDetails().getTypeConversion(), launcherFiles.getSavesManifest(), launcherFiles.getGameDetailsManifest());
        savesCreatorController.beforeShow(stage);
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
