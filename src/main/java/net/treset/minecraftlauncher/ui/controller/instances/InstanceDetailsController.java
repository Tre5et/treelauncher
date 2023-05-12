package net.treset.minecraftlauncher.ui.controller.instances;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.treset.minecraftlauncher.data.InstanceData;

public class InstanceDetailsController {
    @FXML public VBox container;
    @FXML public Label versionName;
    @FXML public Label savesName;
    @FXML public Label resourcepacksName;
    @FXML public Label optionsName;
    @FXML public Label modsName;

    public void setVisible(boolean visible) {
        container.setVisible(visible);
    }

    public void populate(InstanceData instanceData) {
        versionName.setText(instanceData.getVersionComponents().get(0).getKey().getName());
        savesName.setText(instanceData.getSavesComponent().getName());
        resourcepacksName.setText(instanceData.getResourcepacksComponent().getName());
        optionsName.setText(instanceData.getOptionsComponent().getName());
        if(instanceData.getModsComponent() != null) {
            modsName.setVisible(true);
            modsName.setText(instanceData.getModsComponent().getKey().getName());
        } else {
            modsName.setVisible(false);
        }
    }
}
