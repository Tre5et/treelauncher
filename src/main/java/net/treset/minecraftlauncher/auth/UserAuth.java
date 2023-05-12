package net.treset.minecraftlauncher.auth;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UserAuth {
    private final Logger LOGGER = LogManager.getLogger(UserAuth.class);

    private boolean loggedIn = false;
    private boolean authenticating = false;
    private User minecraftUser;

    public void authenticate(File authFile, boolean ignoreFile , Consumer<Boolean> doneCallback) {
        authenticating = true;

        if(authFile.isFile() && !ignoreFile) {
            InputStream authFileStream;
            try {
                authFileStream = new FileInputStream(authFile);
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

            completeAuthentication(authFile, Authenticator.of(authenticationFile).shouldAuthenticate().build(), doneCallback);
        }
        else {
            String loginUrl = Authenticator.microsoftLogin().toString();
            if(!authFile.getParentFile().mkdirs()) {
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

                webView.getEngine().locationProperty().addListener(((obeservable, oldValue, newValue) -> this.onWebLocationChanged(stage, authFile, newValue, doneCallback)));

                Scene webScene = new Scene(webView);
                stage.setScene(webScene);

                stage.show();
            });
        }


    }

    private void completeAuthentication(File authFile, Authenticator minecraftAuth, Consumer<Boolean> doneCallback) {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(authFile);
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to create output stream for auth file", e);
            doneCallback.accept(false);
            return;
        }

        try {
            minecraftAuth.run();
        } catch (AuthenticationException | NullPointerException e) {
            final AuthenticationFile resultFile = minecraftAuth.getResultFile();
            if(resultFile != null) {
                try {
                    resultFile.writeCompressed(outputStream);
                    outputStream.close();
                } catch (IOException e1) {
                    LOGGER.error("Unable to write auth file", e1);
                    doneCallback.accept(false);
                    return;
                }
            }
            LOGGER.error("Unable to login", e);
            doneCallback.accept(false);
            return;
        }

        final AuthenticationFile resultFile = minecraftAuth.getResultFile();
        try {
            resultFile.writeCompressed(outputStream);
            outputStream.close();
        } catch (IOException e) {
            LOGGER.error("Unable to write auth file", e);
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

    public User getMinecraftUser() {
        return minecraftUser;
    }

    public void onWebCloseRequested(Stage stage, Consumer<Boolean> doneCallback) {
        stage.close();
        LOGGER.warn("Login window closed");
        doneCallback.accept(false);
    }

    public void onWebLocationChanged(Stage stage, File authFile, String newUrl, Consumer<Boolean> doneCallback) {
        stage.setTitle(newUrl);
        if(newUrl.startsWith("https://login.live.com/oauth20_desktop.srf?code=")) {
            Pattern pattern = Pattern.compile("code=(.*)&");
            Matcher matcher = pattern.matcher(newUrl);
            if(matcher.find()) {
                String match = matcher.group(1);
                stage.close();
                completeAuthentication(authFile, Authenticator.ofMicrosoft(match).shouldAuthenticate().build(), doneCallback);
            }
        }
    }

    public boolean isAuthenticating() {
        return authenticating;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
