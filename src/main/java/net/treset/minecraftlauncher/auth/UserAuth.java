package net.treset.minecraftlauncher.auth;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.mojang.MinecraftProfile;
import net.treset.mc_version_loader.mojang.MinecraftProfileProperty;
import net.treset.mc_version_loader.mojang.MinecraftProfileTextures;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.ImageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UserAuth {
    private final Logger LOGGER = LogManager.getLogger(UserAuth.class);

    private boolean loggedIn = false;
    private boolean authenticating = false;
    private User minecraftUser;

    public void authenticate(boolean remember, Consumer<Boolean> doneCallback) {
        String loginUrl = Authenticator.microsoftLogin().toString();
        if (remember && !LauncherApplication.config.AUTH_FILE.getParentFile().isDirectory() && !LauncherApplication.config.AUTH_FILE.getParentFile().mkdirs()) {
            LOGGER.error("Unable to create auth file directory");
            doneCallback.accept(false);
            return;
        }

        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle(loginUrl);
            stage.setOnCloseRequest((WindowEvent e) -> onWebCloseRequested(stage, doneCallback));

            WebView webView = new WebView();
            webView.getEngine().load(loginUrl);

            webView.getEngine().locationProperty().addListener(((obeservable, oldValue, newValue) -> this.onWebLocationChanged(stage, remember, newValue, doneCallback)));

            Scene webScene = new Scene(webView);
            stage.setScene(webScene);

            stage.show();
        });
    }

    public void authenticateFromFile(Consumer<Boolean> doneCallback) {
        if(!LauncherApplication.config.AUTH_FILE.isFile()) {
            return;
        }

        InputStream authFileStream;
        try {
            authFileStream = new FileInputStream(LauncherApplication.config.AUTH_FILE);
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to open create input stream for auth file", e);
            doneCallback.accept(false);
            return;
        }

        AuthenticationFile authenticationFile;
        try {
            authenticationFile = AuthenticationFile.readCompressed(authFileStream);
            authFileStream.close();
        } catch (IOException e) {
            LOGGER.error("Unable to read auth file", e);
            doneCallback.accept(false);
            return;
        }

        completeAuthentication(Authenticator.of(authenticationFile).shouldAuthenticate().build(), true, doneCallback);
    }

    public boolean hasFile() {
        return LauncherApplication.config.AUTH_FILE.isFile();
    }

    public void logout() {
        this.loggedIn = false;
        this.minecraftUser = null;
        if(hasFile() && !LauncherApplication.config.AUTH_FILE.delete()) {
            LOGGER.error("Unable to delete auth file");
        }
    }


    private void completeAuthentication(Authenticator minecraftAuth, boolean saveResults, Consumer<Boolean> doneCallback) {
        try {
            minecraftAuth.run();
        } catch (AuthenticationException | NullPointerException e) {
            final AuthenticationFile resultFile = minecraftAuth.getResultFile();
            if(resultFile != null && saveResults && !writeToFile(resultFile)) {
                    LOGGER.error("Unable to write auth file");
            }
            LOGGER.error("Unable to login", e);
            doneCallback.accept(false);
            return;
        }

        final AuthenticationFile resultFile = minecraftAuth.getResultFile();
        if(saveResults && !writeToFile(resultFile)) {
            LOGGER.error("Unable to write auth file");
            doneCallback.accept(false);
            return;
        }

        final Optional<User> optionalUser = minecraftAuth.getUser();
        if(optionalUser.isEmpty()) {
            LOGGER.error("User not present after login");
            doneCallback.accept(false);
            return;
        }

        this.minecraftUser = optionalUser.get();

        this.loggedIn = true;
        this.authenticating = false;
        doneCallback.accept(true);
    }

    private boolean writeToFile(AuthenticationFile file) {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(LauncherApplication.config.AUTH_FILE);
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to create output stream for auth file", e);
            return false;
        }
        try {
            file.writeCompressed(outputStream);
            outputStream.close();
        } catch (IOException e) {
            LOGGER.error("Unable to write auth file", e);
            return false;
        }
        return true;
    }

    public User getMinecraftUser() {
        return minecraftUser;
    }

    public void onWebCloseRequested(Stage stage, Consumer<Boolean> doneCallback) {
        stage.close();
        LOGGER.warn("Login window closed");
        doneCallback.accept(false);
    }

    public void onWebLocationChanged(Stage stage, boolean saveResults, String newUrl, Consumer<Boolean> doneCallback) {
        stage.setTitle(newUrl);
        if(newUrl.startsWith("https://login.live.com/oauth20_desktop.srf?code=")) {
            Pattern pattern = Pattern.compile("code=(.*)&");
            Matcher matcher = pattern.matcher(newUrl);
            if(matcher.find()) {
                String match = matcher.group(1);
                stage.close();
                completeAuthentication(Authenticator.ofMicrosoft(match).shouldAuthenticate().build(), saveResults, doneCallback);
            }
        }
    }

    public boolean isAuthenticating() {
        return authenticating;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    private Image userIcon;

    public Image getUserIcon() throws FileDownloadException {
        if(userIcon == null) {
            userIcon = loadUserIcon();
        }
        return userIcon;
    }

    private static final int[] headBaseUVWH = new int[]{8, 8, 8, 8};
    private static final int[] headTopUVWH = new int[]{40, 8, 8, 8};

    public Image loadUserIcon() throws FileDownloadException {
        MinecraftProfile profile = VersionLoader.getMinecraftProfile(minecraftUser.uuid());
        if(profile.getProperties() == null || profile.getProperties().isEmpty()) {
            throw new FileDownloadException("No properties found for user " + minecraftUser.name());
        }
        MinecraftProfileTextures textures = profile.getProperties().stream().map(MinecraftProfileProperty::getTextures).filter(Objects::nonNull).findFirst().orElse(null);
        if(textures == null) {
            throw new FileDownloadException("No textures found for user " + minecraftUser.name());
        }
        Image texture;
        try {
            BufferedImage img = ImageIO.read(new URL(textures.getTextures().getSKIN().getUrl()));
            texture = ImageUtil.getImage(img);
        } catch (IOException e) {
            throw new FileDownloadException("Failed to download skin for user " + minecraftUser.name(), e);
        }

        Image top = new WritableImage(texture.getPixelReader(), headTopUVWH[0], headTopUVWH[1], headTopUVWH[2], headTopUVWH[3]);
        Image base = new WritableImage(texture.getPixelReader(), headBaseUVWH[0], headBaseUVWH[1], headBaseUVWH[2], headBaseUVWH[3]);

        return ImageUtil.combine(base, top);
    }
}
