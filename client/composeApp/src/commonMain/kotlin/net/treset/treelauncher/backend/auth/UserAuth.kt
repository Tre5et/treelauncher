package net.treset.treelauncher.backend.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import net.treset.mcdl.auth.AuthDL
import net.treset.mcdl.auth.AuthenticationStep
import net.treset.mcdl.auth.InteractiveData
import net.treset.mcdl.auth.data.UserData
import net.treset.mcdl.auth.token.DefaultTokenPolicy
import net.treset.mcdl.auth.token.FileTokenPolicy
import net.treset.mcdl.exception.FileDownloadException
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.util.Images
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import javax.imageio.ImageIO

class UserAuth {
    var isLoggedIn = false
        private set
    var isAuthenticating = false
        private set
    var minecraftUser: UserData? = null
        private set

    private var thread: Thread? = null

    fun authenticate(remember: Boolean, onInteractive: (InteractiveData) -> Unit, onStatus: (AuthenticationStep) -> Unit, onDone: (Exception?) -> Unit) {
        AuthDL.setClientId(appConfig().msClientId)

        AuthDL.setTokenPolicy(
            if(remember) FileTokenPolicy(appConfig().tokenFile) else DefaultTokenPolicy()
        )

        thread = Thread {
            try {
                isAuthenticating = true
                val data = AuthDL.authenticate(onInteractive, onStatus)
                minecraftUser = data
                isLoggedIn = true
                isAuthenticating = false
                onDone(null)
            } catch (e: Exception) {
                isLoggedIn = false
                minecraftUser = null
                isAuthenticating = false
                onDone(e)
            }
        }
        thread?.start()
    }

    fun cancelAuthentication() {
        thread?.interrupt()
        thread = null
    }

    fun hasFile(): Boolean {
        return appConfig().tokenFile.isFile()
    }

    fun logout() {
        isLoggedIn = false
        minecraftUser = null
        if (hasFile() && !appConfig().tokenFile.delete()) {
            LOGGER.error { "Unable to delete auth file" }
        }
    }

    private var userIcon: BufferedImage? = null

    @Throws(FileDownloadException::class)
    fun getUserIcon(): BufferedImage? {
        userIcon?.let {
            return it
        }?: loadUserIcon().let {
                userIcon = it
                return it
            }
    }

    @Throws(FileDownloadException::class)
    fun loadUserIcon(): BufferedImage? {
        if (minecraftUser == null || minecraftUser!!.skins == null || minecraftUser!!.skins!!.isEmpty()) {
            return null
        }
        val skinMap: BufferedImage
        try {
            skinMap = ImageIO.read(URL(minecraftUser!!.skins[0].url))
        } catch (e: IOException) {
            throw FileDownloadException("Failed to download skin for user " + minecraftUser!!.username, e)
        }

        val top = Images.crop(skinMap, headTopUVWH[0], headTopUVWH[1], headTopUVWH[2], headTopUVWH[3])
        val base = Images.crop(skinMap, headBaseUVWH[0], headBaseUVWH[1], headBaseUVWH[2], headBaseUVWH[3])
        return Images.overlay(base, top)
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        private val headBaseUVWH = intArrayOf(8, 8, 8, 8)
        private val headTopUVWH = intArrayOf(40, 8, 8, 8)
    }
}

private var userAuth = UserAuth()
fun userAuth() = userAuth
