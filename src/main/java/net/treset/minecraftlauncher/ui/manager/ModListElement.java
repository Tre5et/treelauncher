package net.treset.minecraftlauncher.ui.manager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModDownload;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.util.UiLoader;

import java.io.IOException;
import java.util.Objects;

public class ModListElement extends UiElement {
    @FXML private AnchorPane rootPane;
    @FXML private Label title;
    @FXML private Label description;
    @FXML private ComboBox<String> versionSelector;
    @FXML private ImageView modrinthLogo;
    @FXML private ImageView curseforgeLogo;

    private LauncherMod mod;


    @Override
    public void beforeShow(Stage stage) {
        if(mod != null) {
            title.setText(mod.getName());
            description.setText(mod.getDescription());
            for(LauncherModDownload d : mod.getDownloads()) {
                if("modrinth".equals(d.getProvider())) {
                    modrinthLogo.setImage(new Image("/img/modrinth.png"));
                } else if("curseforge".equals(d.getProvider())) {
                    curseforgeLogo.setImage(new Image("/img/curseforge.png"));
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
