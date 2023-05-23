package net.treset.minecraftlauncher;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.auth.UserAuth;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.ui.login.LoginController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;


public class LauncherApplication extends Application {
    private static final Logger LOGGER = LogManager.getLogger(LauncherApplication.class);

    public static final UserAuth userAuth = new UserAuth();
    public static final StringLocalizer stringLocalizer = new StringLocalizer();
    public static Config config;

    public static void main(String[] args) {
        if(System.getProperty("launcher.path") == null || System.getProperty("launcher.path").isEmpty()) {
            LOGGER.error("No launcher path provided!");
            System.exit(-1);
            return;
        }
        config = new Config(System.getProperty("launcher.path"), System.getProperty("launcher.path") != null);

        setupLogger();

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

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.setProperty("prism.lcdtext", "false");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/icon.png"))));

        LoginController.showOnStage(primaryStage);
    }
}