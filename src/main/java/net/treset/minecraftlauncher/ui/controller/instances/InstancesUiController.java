package net.treset.minecraftlauncher.ui.controller.instances;

import javafx.stage.Stage;
import net.treset.minecraftlauncher.ui.controller.GenericUiController;

import java.io.IOException;

public class InstancesUiController extends GenericUiController {

    public static InstancesUiController showOnStage(Stage stage) throws IOException {
        return showOnStage(stage, "InstancesUi", "launcher.name");
    }

}
