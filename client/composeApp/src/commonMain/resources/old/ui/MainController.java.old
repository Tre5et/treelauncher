package net.treset.minecraftlauncher.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.treelauncher.backend.news.News;
import net.treset.minecraftlauncher.news.NewsService;
import net.treset.minecraftlauncher.sync.AllSynchronizer;
import net.treset.minecraftlauncher.ui.base.GenericUiController;
import net.treset.minecraftlauncher.ui.create.InstanceCreatorElement;
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;
import net.treset.minecraftlauncher.ui.login.LoginController;
import net.treset.minecraftlauncher.ui.nav.NavbarElement;
import net.treset.minecraftlauncher.ui.selector.*;
import net.treset.minecraftlauncher.ui.settings.SettingsElement;
import net.treset.minecraftlauncher.util.exception.FileLoadException;
import net.treset.minecraftlauncher.util.ui.FileSyncExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class MainController extends GenericUiController {
    private static final Logger LOGGER = LogManager.getLogger(MainController.class);

    @FXML private InstanceSelectorElement icInstancesController;
    @FXML private InstanceCreatorElement icCreatorController;
    @FXML private SavesSelectorElement icSavesController;
    @FXML private ResourcepacksSelectorElement icResourcepacksController;
    @FXML private OptionsSelectorElement icOptionsController;
    @FXML private ModsSelectorElement icModsController;
    @FXML private SettingsElement icSettingsController;
    @FXML private NavbarElement icNavbarController;

    boolean locked = false;

    private News news;

    @Override
    public void beforeShow(Stage stage) {
        super.beforeShow(stage);
        LauncherApplication.setPopupConsumer(this::showPopup);
        LauncherApplication.setCloseCallback(this::onClose);

        icNavbarController.init(this, this::setLocked, this::getLocked);
        icNavbarController.setComponentActivator(this::activate);
        icNavbarController.beforeShow(stage);
        icInstancesController.init(this, this::setLocked, this::getLocked);
        icCreatorController.init(this, this::setLocked, this::getLocked);
        icSavesController.init(this, this::setLocked, this::getLocked);
        icResourcepacksController.init(this, this::setLocked, this::getLocked);
        icOptionsController.init(this, this::setLocked, this::getLocked);
        icModsController.init(this, this::setLocked, this::getLocked);
        icSettingsController.init(this::onLogout);
        icNavbarController.triggerHomeAction();
    }

    @Override
    public void afterShow(Stage stage) {
        super.afterShow(stage);
        icNavbarController.afterShow(stage);

        new Thread(() -> this.showImportantNews(false)).start();

        if(LauncherApplication.settings.hasSyncData()) {
            new Thread(() -> {
                LauncherFiles files;
                try {
                    files = new LauncherFiles();
                    files.reloadAll();
                } catch (FileLoadException e) {
                    LauncherApplication.displaySevereError(e);
                    return;
                }
                new FileSyncExecutor(
                        new AllSynchronizer(files, (s) -> {
                        })
                ).download(() -> LauncherApplication.setPopup(null));
            }).start();
        }
    }

    @Override
    public void triggerHomeAction() {
        activate(Component.INSTANCE_SELECTOR);
    }

    @FXML
    private void onNews() {
        new Thread(this::showNews).start();
    }

    public static MainController showOnStage(Stage stage) throws IOException {
        return showOnStage(stage, "MainScreen", "launcher.name");
    }

    public boolean activate(Component component) {
        if(getLocked()) return false;
        switch (component) {
            case INSTANCE_SELECTOR -> {
                setAllInvisible();
                icInstancesController.setVisible(true);
            }
            case INSTANCE_CREATOR -> {
                setAllInvisible();
                icCreatorController.setVisible(true);
            }
            case SAVES_SELECTOR -> {
                setAllInvisible();
                icSavesController.setVisible(true);
            }
            case RESOURCEPACKS_SELECTOR -> {
                setAllInvisible();
                icResourcepacksController.setVisible(true);
            }
            case OPTIONS_SELECTOR -> {
                setAllInvisible();
                icOptionsController.setVisible(true);
            }
            case MODS_SELECTOR -> {
                setAllInvisible();
                icModsController.setVisible(true);
            }
            case SETTINGS -> {
                setAllInvisible();
                icSettingsController.setVisible(true);
            }
        }
        return true;
    }

    private void setAllInvisible() {
        icInstancesController.setVisible(false);
        icCreatorController.setVisible(false);
        icSavesController.setVisible(false);
        icResourcepacksController.setVisible(false);
        icOptionsController.setVisible(false);
        icModsController.setVisible(false);
        icSettingsController.setVisible(false);
    }

    public boolean getLocked() {
        return locked;
    }

    public boolean setLocked(boolean locked) {
        this.locked = locked;
        return true;
    }

    public enum Component {
        INSTANCE_SELECTOR,
        INSTANCE_CREATOR,
        SAVES_SELECTOR,
        RESOURCEPACKS_SELECTOR,
        OPTIONS_SELECTOR,
        MODS_SELECTOR,
        SETTINGS
    }

    private void onLogout() {
        LauncherApplication.userAuth.logout();
        try {
            LoginController.showOnStage(getStage());
        } catch (IOException e) {
            LOGGER.error("Failed to open login screen", e);
            getStage().close();
        }
    }

    private boolean autoSynced = false;
    private boolean onClose() {
        if(getLocked()) {
            autoSynced = false;
            return false;
        }
        if(!autoSynced && LauncherApplication.settings.hasSyncData()) {
            LauncherFiles files;
            try {
                files = new LauncherFiles();
                files.reloadAll();
            } catch (FileLoadException e) {
                LauncherApplication.displaySevereError(e);
                return false;
            }
            new FileSyncExecutor(
                    new AllSynchronizer(files, (s) -> {})
            ).upload(() -> {
                autoSynced = true;
                Platform.runLater(() -> LauncherApplication.primaryStage.close());
            });
            return false;
        }
        return true;
    }

    private void showNews() {
        try {
            if (news == null) {
                news = new NewsService().news();
            }
            List<News.NewsElement> importantNews = news.getImportant();
            if(importantNews == null) {
                importantNews = List.of();
            }
            List<News.NewsElement> otherNews = news.getOther();
            if(otherNews == null) {
                otherNews = List.of();
            }

            if(!importantNews.isEmpty() || !otherNews.isEmpty()) {
                StringBuilder sb = new StringBuilder("<hr/>");
                for(News.NewsElement element : importantNews) {
                    sb.append("<h1>").append(element.getTitle()).append("</h1>");
                    sb.append(element.getContent() == null ? "" : element.getContent());
                    sb.append("<hr/>");
                }
                sb.append("<hr/>");
                for(News.NewsElement element : otherNews) {
                    sb.append("<h2>").append(element.getTitle()).append("</h2>");
                    sb.append(element.getContent() == null ? "" : element.getContent());
                    sb.append("<hr/>");
                }

                LauncherApplication.setPopup(
                        new PopupElement(
                                PopupElement.PopupType.NONE,
                                LauncherApplication.stringLocalizer.get("news.title"),
                                sb.toString(),
                                null,
                                List.of(
                                        new PopupElement.PopupButton(
                                                PopupElement.ButtonType.POSITIVE,
                                                LauncherApplication.stringLocalizer.get("news.close"),
                                                (a) -> {
                                                    LauncherApplication.settings.setAcknowledgedNews(news.getImportant().stream().map(News.NewsElement::getId).toList());
                                                    LauncherApplication.setPopup(null);
                                                }
                                        )
                                ),
                                true
                        )
                );
            }
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
    }

    private void showImportantNews(boolean ignoreAcknowledged) {
        try {
            if(news == null) {
                news = new NewsService().news();
            }
            if(news.getImportant() == null) {
                return;
            }
            List<News.NewsElement> importantNews = news.getImportant();
            if(!ignoreAcknowledged) {
                importantNews = importantNews.stream().filter((e) -> LauncherApplication.settings.getAcknowledgedNews() == null || !LauncherApplication.settings.getAcknowledgedNews().contains(e.getId())).toList();
            }
            if(!importantNews.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("<hr/>");
                for(News.NewsElement element : importantNews) {
                    sb.append("<h1>").append(element.getTitle()).append("</h1>");
                    sb.append(element.getContent() == null ? "" : element.getContent());
                    sb.append("<hr/>");
                }

                LauncherApplication.setPopup(
                        new PopupElement(
                                PopupElement.PopupType.NONE,
                                LauncherApplication.stringLocalizer.get("news.important.title"),
                                sb.toString(),
                                null,
                                List.of(
                                        new PopupElement.PopupButton(
                                                PopupElement.ButtonType.POSITIVE,
                                                LauncherApplication.stringLocalizer.get("news.close"),
                                                (a) -> {
                                                    LauncherApplication.settings.setAcknowledgedNews(news.getImportant().stream().map(News.NewsElement::getId).toList());
                                                    LauncherApplication.setPopup(null);
                                                }
                                        )
                                ),
                                true
                        )
                );
            }
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
    }
}
