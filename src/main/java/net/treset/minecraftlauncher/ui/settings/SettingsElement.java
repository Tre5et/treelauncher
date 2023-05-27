package net.treset.minecraftlauncher.ui.settings;

import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.config.GlobalConfigLoader;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.ui.base.UiElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SettingsElement extends UiElement {
    private static final Logger LOGGER = LogManager.getLogger(SettingsElement.class);

    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Label languageRestartHint;
    @FXML private Label usernameLabel;
    @FXML private Label uuidLabel;
    @FXML private Button logoutButton;

    private Runnable logoutCallback;


    public void init(Runnable logoutCallback) {
        this.logoutCallback = logoutCallback;

        for(StringLocalizer.Language language : StringLocalizer.getAvailableLanguages()) {
            languageComboBox.getItems().add((language.equals(StringLocalizer.getSystemLanguage())) ? language.getName() + LauncherApplication.stringLocalizer.get("language.default") : language.getName());
        }
        StringLocalizer.Language language = LauncherApplication.stringLocalizer.getLanguage();
        languageComboBox.getSelectionModel().select((language.equals(StringLocalizer.getSystemLanguage())) ? language.getName() + LauncherApplication.stringLocalizer.get("language.default") : language.getName());

        languageComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::onLanguageComboBoxChanged);
        });

        usernameLabel.setText(LauncherApplication.userAuth.getMinecraftUser().name());
        uuidLabel.setText(LauncherApplication.userAuth.getMinecraftUser().uuid());
    }

    @FXML
    private void onLogoutButtonClicked() {
        logoutCallback.run();
    }

    @FXML
    private void onLanguageComboBoxChanged() {
        String input = languageComboBox.getSelectionModel().getSelectedItem();
        if(input != null) {
            StringLocalizer.Language language = languageFromString(input);
            if(language != null) {
                LauncherApplication.stringLocalizer.setLanguage(language);
                languageRestartHint.setVisible(true);
                GlobalConfigLoader.updateLanguage(language);
            }
        }
    }

    @FXML
    private void onSourceButtonClicked() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI(LauncherApplication.stringLocalizer.get("url.source")));
    }

    private static StringLocalizer.Language languageFromString(String language) {
        for(StringLocalizer.Language l : StringLocalizer.getAvailableLanguages()) {
            if(l.getName().equals(language) || language.equals(l.getName() + LauncherApplication.stringLocalizer.get("language.default"))) {
                return l;
            }
        }
        LOGGER.warn("Could not find language: " + language);
        return null;
    }

    @Override
    public void beforeShow(Stage stage) {
    }

    @Override
    public void afterShow(Stage stage) {

    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
