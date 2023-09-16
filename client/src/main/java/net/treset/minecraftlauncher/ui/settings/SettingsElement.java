package net.treset.minecraftlauncher.ui.settings;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
import net.treset.minecraftlauncher.sync.SyncService;
import net.treset.minecraftlauncher.ui.MainController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.update.LauncherUpdater;
import net.treset.minecraftlauncher.util.ImageUtil;
import net.treset.minecraftlauncher.util.UiUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SettingsElement extends UiElement {
    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<StringLocalizer.Language> cbLanguage;
    @FXML private Label lbLanguage;
    @FXML private TextField tfPath;
    @FXML private CheckBox cbRemove;
    @FXML private TextField tfSyncUrl;
    @FXML private TextField tfSyncPort;
    @FXML private TextField tfSyncKey;
    @FXML private Label lbUsername;
    @FXML private Label lbUuid;
    @FXML private ImageView ivSkin;
    @FXML private Label lbUpdate;

    private Runnable logoutCallback;


    public void init(Runnable logoutCallback) {
        this.logoutCallback = logoutCallback;

        cbLanguage.getItems().addAll(StringLocalizer.getAvailableLanguages());
        cbLanguage.getSelectionModel().select(LauncherApplication.stringLocalizer.getLanguage());

        cbLanguage.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::onLanguageComboBoxChanged));

        tfPath.setText(LauncherApplication.config.BASE_DIR);

        tfSyncUrl.setText(LauncherApplication.settings.getSyncUrl());
        tfSyncPort.setText(LauncherApplication.settings.getSyncPort());
        tfSyncKey.setText(LauncherApplication.settings.getSyncKey());

        tfSyncUrl.textProperty().addListener((observable, oldValue, newValue) -> LauncherApplication.settings.setSyncUrl(newValue.isBlank() ? null : newValue));
        tfSyncPort.textProperty().addListener((observable, oldValue, newValue) -> LauncherApplication.settings.setSyncPort(newValue.isBlank() ? null : newValue));
        tfSyncKey.textProperty().addListener((observable, oldValue, newValue) -> LauncherApplication.settings.setSyncKey(newValue.isBlank() ? null : newValue));

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
    private void onSyncTest() {
        SyncService service = new SyncService();
        try {
            service.testConnection();
        } catch (IOException e) {
            LauncherApplication.setPopup(new PopupElement(
                    PopupElement.PopupType.ERROR,
                    "settings.popup.sync.failure",
                    e.getMessage(),
                    List.of(
                            new PopupElement.PopupButton(
                                    PopupElement.ButtonType.POSITIVE,
                                    "settings.popup.sync.close",
                                    event -> LauncherApplication.setPopup(null)
                            )
                    )
            ));
            return;
        }

        LauncherApplication.setPopup(new PopupElement(
                PopupElement.PopupType.SUCCESS,
                "settings.popup.sync.success",
                null,
                List.of(
                        new PopupElement.PopupButton(
                                PopupElement.ButtonType.POSITIVE,
                                "settings.popup.sync.close",
                                event -> LauncherApplication.setPopup(null)
                        )
                )
        ));
    }

    @FXML
    private void onLogout() {
        logoutCallback.run();
    }

    @FXML
    private void onLanguageComboBoxChanged() {
        LauncherApplication.stringLocalizer.setLanguage(cbLanguage.getSelectionModel().getSelectedItem());
        lbLanguage.setVisible(true);
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
            LauncherApplication.setPopup(
                    new PopupElement(
                            PopupElement.PopupType.ERROR,
                            "settings.path.invalid.title",
                            null,
                            List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        LauncherApplication.stringLocalizer.get("settings.path.close"),
                                        event -> LauncherApplication.setPopup(null)
                                )
                            )
                    )
            );
            return;
        }

        LauncherApplication.setPopup(
                new PopupElement(
                        "settings.path.changing.title",
                        null
                )
        );

        new Thread(() -> {
            try {
                GlobalConfigLoader.updatePath(dir, cbRemove.isSelected());
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                return;
            }

            LauncherApplication.setPopup(
                    new PopupElement(
                            PopupElement.PopupType.SUCCESS,
                            "settings.path.success.title",
                            null,
                            List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        LauncherApplication.stringLocalizer.get("settings.path.close"),
                                        event -> {
                                            try {
                                                LauncherApplication.setPopup(null);
                                                MainController.showOnStage(LauncherApplication.primaryStage);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                )
                            )
                    )
            );
        }).start();
    }

    @FXML
    private void onSource() {
        try {
            UiUtil.openBrowser(LauncherApplication.stringLocalizer.get("url.source"));
        } catch(Exception e) {
            LauncherApplication.displayError(e);
        }
    }

    @FXML
    private void onUpdate() {
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.NONE,
                        "settings.update.checking.title",
                        null,
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.NEGATIVE,
                                        "settings.update.cancel",
                                        event -> this.cancelUpdate()
                                )
                        )
                )
        );
        canceled = false;

        new Thread(() -> {
            if(LauncherApplication.launcherUpdater == null) {
                try {
                    LauncherApplication.launcherUpdater = new LauncherUpdater();
                } catch (FileDownloadException e) {
                    LauncherApplication.displayError(e);
                    LauncherApplication.setPopup(null);
                    return;
                }
            }

            if(canceled) {
                return;
            }

            if(LauncherApplication.launcherUpdater.getUpdateVersion() == null) {
                Platform.runLater(this::showUpdateLatest);
            } else {
                Platform.runLater(this::showUpdateAvailable);
            }
        }).start();
    }

    private void showUpdateLatest() {
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.SUCCESS,
                        "settings.update.latest.title",
                        LauncherApplication.stringLocalizer.getFormatted("settings.update.latest.message", LauncherApplication.stringLocalizer.get("launcher.version")),
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "settings.update.close",
                                        event -> LauncherApplication.setPopup(null)
                                )
                        )
                )
        );
    }

    private void showUpdateAvailable() {
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.NONE,
                        "settings.update.available.title",
                        LauncherApplication.stringLocalizer.getFormatted("settings.update.available.message", LauncherApplication.stringLocalizer.get("launcher.version"), LauncherApplication.launcherUpdater.getUpdateVersion()),
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.NEGATIVE,
                                        "settings.update.cancel",
                                        event -> LauncherApplication.setPopup(null)
                                ),
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "settings.update.download",
                                        event -> downloadUpdate()
                                )
                        )
                )
        );
    }

    private void showUpdateSuccess() {
        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.SUCCESS,
                        "settings.update.success.title",
                        "settings.update.success.message",
                        List.of(
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "settings.update.close",
                                        event -> LauncherApplication.setPopup(null)
                                ),
                                new PopupElement.PopupButton(
                                        PopupElement.ButtonType.POSITIVE,
                                        "settings.update.restart",
                                        event -> {
                                            LauncherApplication.setRestartAfterUpdate(true);
                                            Platform.exit();
                                        }
                                )
                        )
                )
        );
    }

    private void downloadUpdate() {
        PopupElement popup = new PopupElement(
                "settings.update.downloading.title",
                null
        );
        LauncherApplication.setPopup(popup);

        int total = LauncherApplication.launcherUpdater.getFileCount();

        new Thread(() -> {
            try {
                LauncherApplication.launcherUpdater.downloadFiles(
                        (amount, file) -> Platform.runLater(() -> popup.setMessage(LauncherApplication.stringLocalizer.getFormatted("settings.update.downloading.message", file, amount, total)))
                );
            } catch (FileDownloadException e) {
                LauncherApplication.displayError(e);
                LauncherApplication.setPopup(null);
                return;
            }

            try {
                LauncherApplication.launcherUpdater.writeFile();
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                LauncherApplication.setPopup(null);
                return;
            }

            Platform.runLater(this::showUpdateSuccess);
        }).start();
    }

    private boolean canceled = false;
    private void cancelUpdate() {
        LauncherApplication.setPopup(null);
        canceled = true;
    }

    private void checkUpdate() {
        if(LauncherApplication.launcherUpdater == null) {
            try {
                LauncherApplication.launcherUpdater = new LauncherUpdater();
            } catch (FileDownloadException e) {
                LauncherApplication.displayError(e);
                return;
            }
        }
        if(LauncherApplication.launcherUpdater.getUpdateVersion() != null) {
            Platform.runLater(() -> lbUpdate.setVisible(true));
        }
    }

    @Override
    public void beforeShow(Stage stage) {
        new Thread(this::checkUpdate).start();
    }

    @Override
    public void afterShow(Stage stage) {

    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
