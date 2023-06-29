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
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Label languageRestartHint;
    @FXML private TextField pathField;
    @FXML private CheckBox removeCheckBox;
    @FXML private Label usernameLabel;
    @FXML private Label uuidLabel;
    @FXML private ImageView skinView;
    @FXML private PopupElement popupController;

    private Runnable logoutCallback;


    public void init(Runnable logoutCallback) {
        this.logoutCallback = logoutCallback;

        for(StringLocalizer.Language language : StringLocalizer.getAvailableLanguages()) {
            languageComboBox.getItems().add((language.equals(StringLocalizer.getSystemLanguage())) ? language.getName() + LauncherApplication.stringLocalizer.get("language.default") : language.getName());
        }
        StringLocalizer.Language language = LauncherApplication.stringLocalizer.getLanguage();
        languageComboBox.getSelectionModel().select((language.equals(StringLocalizer.getSystemLanguage())) ? language.getName() + LauncherApplication.stringLocalizer.get("language.default") : language.getName());

        languageComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::onLanguageComboBoxChanged));

        usernameLabel.setText(LauncherApplication.userAuth.getMinecraftUser().name());
        uuidLabel.setText(LauncherApplication.userAuth.getMinecraftUser().uuid());

        new Thread(this::loadSkin).start();
    }

    private void loadSkin() {
        if(!LauncherApplication.userAuth.isLoggedIn()) {
            return;
        }
        try {
            Image profileImage = ImageUtil.rescale(LauncherApplication.userAuth.getUserIcon(), 6);
            Platform.runLater(() -> skinView.setImage(profileImage));
        } catch (FileDownloadException e) {
            LauncherApplication.displayError(e);
        }
    }

    @FXML
    private void onLogoutButtonClicked() {
        logoutCallback.run();
    }

    @FXML
    private void onLanguageComboBoxChanged() {
        String input = languageComboBox.getSelectionModel().getSelectedItem();
        if(input != null) {
            StringLocalizer.Language language;
            try {
                language = languageFromString(input);
            } catch (IllegalArgumentException e) {
                LauncherApplication.displaySevereError(e);
                return;
            }
            LauncherApplication.stringLocalizer.setLanguage(language);
            languageRestartHint.setVisible(true);
            try {
                GlobalConfigLoader.updateLanguage(language);
            } catch (IOException e) {
                LauncherApplication.displayError(e);
            }
        }
    }

    @FXML
    private void onFileSelectorClicked() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(pathField.getText()));
        File chosen = chooser.showDialog(LauncherApplication.primaryStage);
        if(chosen != null && chosen.isDirectory()) {
            pathField.setText(chosen.getAbsolutePath());
        }
    }

    @FXML
    private void onPathApplyClicked() {
        File dir = new File(pathField.getText());
        if(!dir.isDirectory()) {
            popupController.setType(PopupElement.PopupType.ERROR);
            popupController.setContent("settings.path.invalid.title", "");
            popupController.clearButtons();
            popupController.addButtons(
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.POSITIVE,
                            "settings.path.close",
                            "close",
                            id -> popupController.setVisible(false)
                    )
            );
            popupController.setVisible(true);
            return;
        }

        popupController.setType(PopupElement.PopupType.NONE);
        popupController.setContent("settings.path.changing.title", "");
        popupController.clearButtons();
        popupController.setVisible(true);

        new Thread(() -> {
            try {
                GlobalConfigLoader.updatePath(dir, removeCheckBox.isSelected());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                popupController.setVisible(false);
                return;
            }

            Platform.runLater(() -> {
                popupController.setType(PopupElement.PopupType.SUCCESS);
                popupController.setContent("settings.path.success.title", "");
                popupController.clearButtons();
                popupController.addButtons(
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
                popupController.setVisible(false);
                popupController.setVisible(true);
            });
        }).start();

    }

    @FXML
    private void onSourceButtonClicked() throws URISyntaxException, IOException {
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
        pathField.setText(LauncherApplication.config.BASE_DIR);
    }

    @Override
    public void afterShow(Stage stage) {

    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
