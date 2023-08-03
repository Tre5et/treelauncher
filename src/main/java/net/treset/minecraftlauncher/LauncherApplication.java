package net.treset.minecraftlauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.auth.UserAuth;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.config.GlobalConfigLoader;
import net.treset.minecraftlauncher.config.Settings;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.ui.login.LoginController;
import net.treset.minecraftlauncher.update.LauncherUpdater;
import net.treset.minecraftlauncher.util.FileInitializer;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class LauncherApplication extends Application {
    private static final Logger LOGGER = LogManager.getLogger(LauncherApplication.class);

    public static final UserAuth userAuth = new UserAuth();
    public static StringLocalizer stringLocalizer;
    public static Config config;
    public static Settings settings;
    public static LauncherUpdater launcherUpdater;
    public static Stage primaryStage;

    public static void main(String[] args) {
        setupLogger();

        try{
            config = GlobalConfigLoader.loadConfig();
        } catch (IllegalStateException | IOException e) {
            LOGGER.error("Failed to load config!", e);
            System.exit(-1);
            return;
        }

        try {
            if(!new File(config.BASE_DIR).exists() || !GlobalConfigLoader.manifestExists(new File(config.BASE_DIR))) {
                new FileInitializer(new File(config.BASE_DIR)).create();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to initialize directory structure!", e);
            System.exit(-1);
            return;
        }

        try {
            loadSettings();
        } catch (IOException e) {
            LOGGER.error("Failed to load settings!", e);
            System.exit(-1);
            return;
        }

        stringLocalizer = new StringLocalizer(config.LANGUAGE);

        launch(args);
    }

    private static void setupLogger() {
        // TODO: file not working in build, why?

        /* ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.DEBUG);
        builder.setConfigurationName("DefaultLogger");

        // File Logger
        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %highlight{%-5level} %logger{1.2*} - %msg%n");
        ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "1MB"));

        AppenderComponentBuilder appenderBuilder = builder.newAppender("FileAppender", "RollingFile")
                .addAttribute("fileName", config.LOG_PATH + "latest.log")
                .addAttribute("filePattern",  config.LOG_PATH + "log-%d{MM-dd-yy-HH-mm-ss}.log.gz")
                .add(layoutBuilder).addComponent(triggeringPolicy);

        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);

        //Console Logger
        AppenderComponentBuilder consoleAppenderBuilder = builder.newAppender("ConsoleAppender", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        consoleAppenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %highlight{%-5level} %logger{1.2*} - %msg%n"));

        builder.add(appenderBuilder);
        builder.add(consoleAppenderBuilder);
        rootLogger.add(builder.newAppenderRef("FileAppender"));
        rootLogger.add(builder.newAppenderRef("ConsoleAppender"));
        builder.add(rootLogger);

        Configurator.reconfigure(builder.build());*/
    }

    private static void loadSettings() throws IOException {
        File settingsFile = new File(FormatUtil.absoluteFilePath(config.BASE_DIR, config.SETTINGS_FILE_NAME));
        if(!settingsFile.exists()) {
            settings = new Settings(settingsFile);
            settings.save();
        } else {
            settings = Settings.load(settingsFile);
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        LauncherApplication.primaryStage = primaryStage;

        System.setProperty("prism.lcdtext", "false");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/icon.png"))));

        LoginController.showOnStage(primaryStage);
    }

    public static void displayError(Exception e) {
        LOGGER.warn("An error occurred", e);
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, LauncherApplication.stringLocalizer.getFormatted("error.message", e)).show());
    }

    public static void displaySevereError(Exception e) {
        LOGGER.error("A SEVERE ERROR OCCURRED", e);
        Platform.runLater(() -> {
            new Alert(Alert.AlertType.ERROR, LauncherApplication.stringLocalizer.getFormatted("error.severe.message", e)).showAndWait();
            System.exit(-1);
        });
    }

    @Override
    public void stop() throws Exception {
        try {
            settings.save();
        } catch (IOException e) {
            displayError(e);
        }
        if(new File("update.json").exists()) {
            try {
                startUpdater();
            } catch (IOException e) {
                displayError(e);
            }
        }
        super.stop();
    }

    private void startUpdater() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(FormatUtil.absoluteFilePath(System.getProperty("java.home"), "bin", "java"), "-jar", "app/updater.jar");
        pb.start();
    }
}