package net.treset.treelauncher.backend.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hycrafthd.minecraft_authenticator.login.AuthenticationException
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile
import net.hycrafthd.minecraft_authenticator.login.Authenticator
import net.hycrafthd.minecraft_authenticator.login.User
import net.treset.mc_version_loader.exception.FileDownloadException
import net.treset.mc_version_loader.json.SerializationException
import net.treset.mc_version_loader.mojang.MinecraftProfile
import net.treset.mc_version_loader.mojang.MinecraftProfileTextures
import net.treset.mc_version_loader.mojang.MojangData
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.Images
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import java.util.function.Consumer
import java.util.regex.Pattern
import javax.imageio.ImageIO

class UserAuth {
    private val LOGGER = KotlinLogging.logger {}
    var isLoggedIn = false
        private set
    var isAuthenticating = false
        private set
    var minecraftUser: User? = null
        private set

    fun authenticate(remember: Boolean, doneCallback: Consumer<Boolean?>) {
        val loginUrl = Authenticator.microsoftLogin().toString()
        if (remember && !appConfig().AUTH_FILE.getParentFile().isDirectory() && !appConfig().AUTH_FILE.getParentFile().mkdirs()) {
            LOGGER.error("Unable to create auth file directory")
            doneCallback.accept(false)
            return
        }
        //TODO: Show web view
        Platform.runLater {
            val stage = Stage()
            stage.setTitle(loginUrl)
            stage.setOnCloseRequest { e: WindowEvent? -> onWebCloseRequested(stage, doneCallback) }
            val webView = WebView()
            webView.getEngine().load(loginUrl)
            webView.getEngine().locationProperty().addListener { obeservable, oldValue, newValue ->
                onWebLocationChanged(
                    stage,
                    remember,
                    newValue,
                    doneCallback
                )
            }
            val webScene = Scene(webView)
            stage.setScene(webScene)
            stage.show()
        }
    }

    fun authenticateFromFile(doneCallback: Consumer<Boolean?>) {
        if (!appConfig().AUTH_FILE.isFile()) {
            return
        }
        val authFileStream: InputStream
        try {
            authFileStream = FileInputStream(appConfig().AUTH_FILE)
        } catch (e: FileNotFoundException) {
            LOGGER.error(e) { "${"Unable to open create input stream for auth file"}" }
            doneCallback.accept(false)
            return
        }
        val authenticationFile: AuthenticationFile
        try {
            authenticationFile = AuthenticationFile.readCompressed(authFileStream)
            authFileStream.close()
        } catch (e: IOException) {
            LOGGER.error(e) { "Unable to read auth file" }
            doneCallback.accept(false)
            return
        }
        completeAuthentication(Authenticator.of(authenticationFile).shouldAuthenticate().build(), true, doneCallback)
    }

    fun hasFile(): Boolean {
        return appConfig().AUTH_FILE.isFile()
    }

    fun logout() {
        isLoggedIn = false
        minecraftUser = null
        if (hasFile() && !appConfig().AUTH_FILE.delete()) {
            LOGGER.error("Unable to delete auth file")
        }
    }

    private fun completeAuthentication(
        minecraftAuth: Authenticator,
        saveResults: Boolean,
        doneCallback: Consumer<Boolean?>
    ) {
        try {
            minecraftAuth.run()
        } catch (e: Exception) {
            val resultFile: AuthenticationFile? = minecraftAuth.getResultFile()
            if (resultFile != null && saveResults && !writeToFile(resultFile)) {
                LOGGER.error { "Unable to write auth file" }
            }
            LOGGER.error(e) { "Unable to login" }
            doneCallback.accept(false)
            return
        }
        val resultFile: AuthenticationFile = minecraftAuth.getResultFile()
        if (saveResults && !writeToFile(resultFile)) {
            LOGGER.error { "Unable to write auth file" }
            doneCallback.accept(false)
            return
        }
        val optionalUser = minecraftAuth.getUser()
        if (optionalUser.isEmpty) {
            LOGGER.error { "User not present after login" }
            doneCallback.accept(false)
            return
        }
        minecraftUser = optionalUser.get()
        isLoggedIn = true
        isAuthenticating = false
        doneCallback.accept(true)
    }

    private fun writeToFile(file: AuthenticationFile): Boolean {
        val outputStream: FileOutputStream
        try {
            outputStream = FileOutputStream(appConfig().AUTH_FILE)
        } catch (e: FileNotFoundException) {
            LOGGER.error(e) { "Unable to create output stream for auth file" }
            return false
        }
        try {
            file.writeCompressed(outputStream)
            outputStream.close()
        } catch (e: IOException) {
            LOGGER.error(e) { "Unable to write auth file" }
            return false
        }
        return true
    }

    fun onWebCloseRequested(stage: Stage, doneCallback: Consumer<Boolean?>) {
        stage.close()
        LOGGER.warn { "Login window closed" }
        doneCallback.accept(false)
    }

    fun onWebLocationChanged(stage: Stage, saveResults: Boolean, newUrl: String, doneCallback: Consumer<Boolean?>) {
        stage.setTitle(newUrl)
        if (newUrl.startsWith("https://login.live.com/oauth20_desktop.srf?code=")) {
            val pattern = Pattern.compile("code=(.*)&")
            val matcher = pattern.matcher(newUrl)
            if (matcher.find()) {
                val match = matcher.group(1)
                stage.close()
                completeAuthentication(
                    Authenticator.ofMicrosoft(match).shouldAuthenticate().build(),
                    saveResults,
                    doneCallback
                )
            }
        }
    }

    private var userIcon: BufferedImage? = null

    @Throws(FileDownloadException::class)
    fun getUserIcon(): BufferedImage {
        userIcon?.let {
            return it
        }?: loadUserIcon().let {
                userIcon = it
                return it
            }
    }

    @Throws(FileDownloadException::class)
    fun loadUserIcon(): BufferedImage {
        val profile: MinecraftProfile = MojangData.getMinecraftProfile(minecraftUser!!.uuid())
        if (profile.properties == null || profile.properties.isEmpty()) {
            throw FileDownloadException("No properties found for user " + minecraftUser!!.name())
        }
        var textures: MinecraftProfileTextures? = null
        for (property in profile.properties) {
            textures = try {
                property.textures
            } catch (e: SerializationException) {
                throw FileDownloadException("Failed to deserialize textures for user " + minecraftUser!!.name(), e)
            }
            if (textures != null) {
                break
            }
        }
        if (textures == null) {
            throw FileDownloadException("No textures found for user " + minecraftUser!!.name())
        }
        val skinMap: BufferedImage
        try {
            skinMap = ImageIO.read(URL(textures.textures.skin.url))
        } catch (e: IOException) {
            throw FileDownloadException("Failed to download skin for user " + minecraftUser!!.name(), e)
        }

        val top = Images.crop(skinMap, headTopUVWH[0], headTopUVWH[1], headTopUVWH[2], headTopUVWH[3])
        val base = Images.crop(skinMap, headBaseUVWH[0], headBaseUVWH[1], headBaseUVWH[2], headBaseUVWH[3])
        return Images.overlay(base, top)
    }

    companion object {
        private val headBaseUVWH = intArrayOf(8, 8, 8, 8)
        private val headTopUVWH = intArrayOf(40, 8, 8, 8)
    }
}
