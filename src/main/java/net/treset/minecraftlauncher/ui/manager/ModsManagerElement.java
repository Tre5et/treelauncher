package net.treset.minecraftlauncher.ui.manager;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.minecraftlauncher.ui.base.UiElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModsManagerElement extends UiElement {
    @FXML private AnchorPane rootPane;
    @FXML private VBox currentModsBox;
    @FXML private VBox currentModsContainer;

    private LauncherModsDetails details;
    private List<Pair<ModListElement, AnchorPane>> elements;

    public void setLauncherMods(LauncherModsDetails details) {
        this.details = details;
    }


    @Override
    public void beforeShow(Stage stage){
        if(details == null || details.getMods() == null)
            return;
        elements = new ArrayList<>();
        for(LauncherMod m : details.getMods()) {
            try {
                elements.add(ModListElement.from(m));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        currentModsContainer.getChildren().clear();
        for(Pair<ModListElement, AnchorPane> element : elements) {
            element.getKey().beforeShow(stage);
            currentModsContainer.getChildren().add(element.getValue());
        }
    }

    @Override
    public void afterShow(Stage stage) {
        for(Pair<ModListElement, AnchorPane> element : elements) {
            element.getKey().afterShow(stage);
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
