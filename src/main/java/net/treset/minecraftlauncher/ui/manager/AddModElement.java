package net.treset.minecraftlauncher.ui.manager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.files.Sources;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.mc_version_loader.launcher.LauncherModDownload;
import net.treset.mc_version_loader.mods.curseforge.CurseforgeMod;
import net.treset.mc_version_loader.mods.modrinth.ModrinthMod;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.util.BiConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddModElement extends UiElement {
    @FXML private AnchorPane rootPane;
    @FXML private TextField modName;
    @FXML private TextField pathField;
    @FXML private Label fileError;
    @FXML private TextField versionField;
    @FXML private Label versionError;
    @FXML private TextField modrinthId;
    @FXML private Label modrinthError;
    @FXML private TextField curseforgeId;
    @FXML private Label curseforgeError;

    private BiConsumer<LauncherMod, File> addCallback;

    public void init(BiConsumer<LauncherMod, File> addCallback) {
        this.addCallback = addCallback;
    }

    @FXML
    private void onFileSelectorClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Mod files (*.jar)", "*.jar"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File chosen = fileChooser.showOpenDialog(LauncherApplication.primaryStage);
        if(chosen != null && chosen.isFile()) {
            pathField.setText(chosen.getAbsolutePath());
        }
    }

    @FXML
    private void onCancelButtonClicked() {
        setVisible(false);
    }

    @FXML
    private void onAddButtonClicked() {
        fileError.setVisible(false);
        pathField.getStyleClass().remove("error");
        if(!isComplete()) {
            displayError();
            return;
        }

        File modFile = new File(pathField.getText());
        String fileName = modFile.getName();
        String name = modName.getText().isEmpty() ? fileName.substring(0, fileName.length() - 4) : modName.getText();
        LauncherMod mod = new LauncherMod(
                null,
                null,
                true,
                getIconUrl(),
                name,
                fileName,
                versionField.getText(),
                getDownloads()
        );
        addCallback.accept(mod, modFile);
        setVisible(false);
    }

    private boolean modrinthValid() {
        if(!modrinthId.getText().isEmpty()) {
            try {
                String result = Sources.getFileFromHttpGet(String.format(Sources.getModrinthProjectUrl(), modrinthId.getText()), Sources.getModrinthHeaders(), List.of());
                return result != null && !result.isEmpty();
            } catch (FileDownloadException e) {
                return false;
            }
        }
        return true;
    }

    private boolean curseforgeValid() {
        if(!curseforgeId.getText().isEmpty()) {
            if(!curseforgeId.getText().matches("[0-9]+")) return false;
            try {
                String result = Sources.getFileFromHttpGet(String.format(Sources.getCurseforgeProjectUrl(), Integer.parseInt(curseforgeId.getText())), Sources.getCurseforgeHeaders(), List.of());
                return result != null && !result.isEmpty();
            } catch (FileDownloadException e) {
                return false;
            }
        }
        return true;
    }

    private String getIconUrl() {
        if(!modrinthId.getText().isEmpty()) {
            try {
                String result = Sources.getFileFromHttpGet(String.format(Sources.getModrinthProjectUrl(), modrinthId.getText()), Sources.getModrinthHeaders(), List.of());
                return ModrinthMod.fromJson(result).getIconUrl();
            } catch (FileDownloadException ignored) {}
        }
        if(!curseforgeId.getText().isEmpty()) {
            try {
                String result = Sources.getFileFromHttpGet(String.format(Sources.getCurseforgeProjectUrl(), Integer.parseInt(curseforgeId.getText())), Sources.getCurseforgeHeaders(), List.of());
                return CurseforgeMod.fromJson(result).getIconUrl();
            } catch (FileDownloadException ignored) {}
        }
        return null;
    }

    private List<LauncherModDownload> getDownloads() {
        ArrayList<LauncherModDownload> downloads = new ArrayList<>();
        if(!modrinthId.getText().isEmpty()) {
            downloads.add(new LauncherModDownload(
                    "modrinth",
                    modrinthId.getText()
            ));
        }
        if(!curseforgeId.getText().isEmpty()) {
            downloads.add(new LauncherModDownload(
                    "curseforge",
                    curseforgeId.getText()
            ));
        }
        return downloads;
    }

    public void displayError() {
        versionField.getStyleClass().remove("error");
        versionError.setVisible(false);
        pathField.getStyleClass().remove("error");
        fileError.setVisible(false);
        modrinthId.getStyleClass().remove("error");
        modrinthError.setVisible(false);
        curseforgeId.getStyleClass().remove("error");
        curseforgeError.setVisible(false);
        if (versionField.getText().isEmpty()) {
            versionField.getStyleClass().add("error");
            versionError.setVisible(true);
        }
        if (pathField.getText().isEmpty() || !pathField.getText().endsWith(".jar") || !new File(pathField.getText()).isFile()) {
            pathField.getStyleClass().add("error");
            fileError.setVisible(true);
        }
        if(!modrinthValid()) {
            modrinthId.getStyleClass().add("error");
            modrinthError.setVisible(true);
        }
        if(!curseforgeValid()) {
            curseforgeId.getStyleClass().add("error");
            curseforgeError.setVisible(true);
        }
    }

    public boolean isComplete() {
        return !versionField.getText().isEmpty() && !pathField.getText().isEmpty() && pathField.getText().endsWith(".jar") && new File(pathField.getText()).isFile() && modrinthValid() && curseforgeValid();
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }

    @Override
    public void beforeShow(Stage stage) {
        modName.clear();
        pathField.clear();
        versionField.clear();
        modrinthId.clear();
        curseforgeId.clear();
        pathField.getStyleClass().remove("error");
        fileError.setVisible(false);
        versionField.getStyleClass().remove("error");
        versionError.setVisible(false);
        modrinthId.getStyleClass().remove("error");
        modrinthError.setVisible(false);
        curseforgeId.getStyleClass().remove("error");
        curseforgeError.setVisible(false);
    }

    @Override
    public void afterShow(Stage stage) {}
}
