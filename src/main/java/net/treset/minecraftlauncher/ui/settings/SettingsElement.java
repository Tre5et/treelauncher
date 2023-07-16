package net.treset.minecraftlauncher.ui.settings;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.config.GlobalConfigLoader;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.ui.MainController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.util.ImageUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SettingsElement extends UiElement {
    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<String> cbLanguage;
    @FXML private Label lbLanguage;
    @FXML private TextField tfPath;
    @FXML private CheckBox cbRemove;
    @FXML private Label lbUsername;
    @FXML private Label lbUuid;
    @FXML private ImageView ivSkin;
    @FXML private PopupElement icPopupController;

    private Runnable logoutCallback;


    public void init(Runnable logoutCallback) {
        this.logoutCallback = logoutCallback;

        for(StringLocalizer.Language language : StringLocalizer.getAvailableLanguages()) {
            cbLanguage.getItems().add((language.equals(StringLocalizer.getSystemLanguage())) ? language.getName() + LauncherApplication.stringLocalizer.get("language.default") : language.getName());
        }
        StringLocalizer.Language language = LauncherApplication.stringLocalizer.getLanguage();
        cbLanguage.getSelectionModel().select((language.equals(StringLocalizer.getSystemLanguage())) ? language.getName() + LauncherApplication.stringLocalizer.get("language.default") : language.getName());

        cbLanguage.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::onLanguageComboBoxChanged));

        lbUsername.setText(LauncherApplication.userAuth.getMinecraftUser().name());
        lbUuid.setText(LauncherApplication.userAuth.getMinecraftUser().uuid());

        new Thread(this::loadSkin).start();
    }

    private void loadSkin() {
        if(!LauncherApplication.userAuth.isLoggedIn()) {
            return;
        }
        try {
            Image profileImage = ImageUtil.rescale(LauncherApplication.userAuth.getUserIcon(), 6);
            Platform.runLater(() -> ivSkin.setImage(profileImage));
        } catch (FileDownloadException e) {
            LauncherApplication.displayError(e);
        }
    }

    @FXML
    private void onLogout() {
        logoutCallback.run();
    }

    @FXML
    private void onLanguageComboBoxChanged() {
        String input = cbLanguage.getSelectionModel().getSelectedItem();
        if(input != null) {
            StringLocalizer.Language language;
            try {
                language = languageFromString(input);
            } catch (IllegalArgumentException e) {
                LauncherApplication.displaySevereError(e);
                return;
            }
            LauncherApplication.stringLocalizer.setLanguage(language);
            lbLanguage.setVisible(true);
            try {
                GlobalConfigLoader.updateLanguage(language);
            } catch (IOException e) {
                LauncherApplication.displayError(e);
            }
        }
    }

    @FXML
    private void onFileSelector() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(tfPath.getText()));
        File chosen = chooser.showDialog(LauncherApplication.primaryStage);
        if(chosen != null && chosen.isDirectory()) {
            tfPath.setText(chosen.getAbsolutePath());
        }
    }

    @FXML
    private void onPathApply() {
        File dir = new File(tfPath.getText());
        if(!dir.isDirectory()) {
            icPopupController.setType(PopupElement.PopupType.ERROR);
            icPopupController.setContent("settings.path.invalid.title", "");
            icPopupController.clearControls();
            icPopupController.addButtons(
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.POSITIVE,
                            "settings.path.close",
                            "close",
                            id -> icPopupController.setVisible(false)
                    )
            );
            icPopupController.setVisible(true);
            return;
        }

        icPopupController.setType(PopupElement.PopupType.NONE);
        icPopupController.setContent("settings.path.changing.title", "");
        icPopupController.clearControls();
        icPopupController.setVisible(true);

        new Thread(() -> {
            try {
                GlobalConfigLoader.updatePath(dir, cbRemove.isSelected());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                icPopupController.setVisible(false);
                return;
            }

            Platform.runLater(() -> {
                icPopupController.setType(PopupElement.PopupType.SUCCESS);
                icPopupController.setContent("settings.path.success.title", "");
                icPopupController.clearControls();
                icPopupController.addButtons(
                        new PopupElement.PopupButton(
                                PopupElement.ButtonType.POSITIVE,
                                "settings.path.close",
                                "close",
                                id -> {
                                    try {
                                        MainController.showOnStage(LauncherApplication.primaryStage);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                );
                icPopupController.setVisible(false);
                icPopupController.setVisible(true);
            });
        }).start();

    }

    @FXML
    private void onSource() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI(LauncherApplication.stringLocalizer.get("url.source")));
    }

    private static StringLocalizer.Language languageFromString(String language) throws IllegalArgumentException {
        for(StringLocalizer.Language l : StringLocalizer.getAvailableLanguages()) {
            if(l.getName().equals(language) || language.equals(l.getName() + LauncherApplication.stringLocalizer.get("language.default"))) {
                return l;
            }
        }
        throw new IllegalArgumentException("Could not find language: " + language);
    }

    @Override
    public void beforeShow(Stage stage) {
        tfPath.setText(LauncherApplication.config.BASE_DIR);
    }

    @Override
    public void afterShow(Stage stage) {

    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
