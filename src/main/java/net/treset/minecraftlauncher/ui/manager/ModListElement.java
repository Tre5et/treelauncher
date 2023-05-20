package net.treset.minecraftlauncher.ui.manager;

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
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModDownload;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.UiLoader;

import java.io.IOException;

public class ModListElement extends UiElement {
    @FXML private AnchorPane rootPane;
    @FXML private Label title;
    @FXML private Label description;
    @FXML private Button installButton;
    @FXML private ComboBox<String> versionSelector;
    @FXML private ImageView modrinthLogo;
    @FXML private ImageView curseforgeLogo;

    private LauncherMod mod;


    @Override
    public void beforeShow(Stage stage) {
        if(mod != null) {
            title.setText(mod.getName());
            description.setText(mod.getDescription());
            versionSelector.getItems().clear();
            versionSelector.getItems().add(mod.getVersion());
            versionSelector.getSelectionModel().select(0);
            modrinthLogo.getStyleClass().remove("current");
            modrinthLogo.getStyleClass().remove("available");
            curseforgeLogo.getStyleClass().remove("current");
            curseforgeLogo.getStyleClass().remove("available");
            for(LauncherModDownload d : mod.getDownloads()) {
                if("modrinth".equals(d.getProvider())) {
                    if("modrinth".equals(mod.getCurrentProvider())) {
                        modrinthLogo.getStyleClass().add("current");
                    } else {
                        modrinthLogo.getStyleClass().add("available");
                    }
                } else if("curseforge".equals(d.getProvider())) {
                    if("curseforge".equals(mod.getCurrentProvider())) {
                        curseforgeLogo.getStyleClass().add("current");
                    } else {
                        curseforgeLogo.getStyleClass().add("available");
                    }
                }
            }
        }
    }

    @Override
    public void afterShow(Stage stage) {
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    public void setInstallAvailable(boolean available) {
        installButton.setVisible(available);
    }

    public LauncherMod getMod() {
        return mod;
    }

    public void setMod(LauncherMod mod) {
        this.mod = mod;
    }

    public static Pair<ModListElement, AnchorPane> from(LauncherMod mod) throws IOException {
        Pair<ModListElement, AnchorPane> result = newInstance();
        result.getKey().setMod(mod);
        return result;
    }
    public static Pair<ModListElement, AnchorPane> newInstance() throws IOException {
        FXMLLoader loader = UiLoader.getFXMLLoader("manager/ModListElement");
        AnchorPane element = UiLoader.loadFXML(loader);
        ModListElement listElementController = loader.getController();
        return new Pair<>(listElementController, element);
    }

}
