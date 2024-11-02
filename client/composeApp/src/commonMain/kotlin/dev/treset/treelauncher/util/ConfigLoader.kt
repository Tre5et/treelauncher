package dev.treset.treelauncher.util

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import dev.treset.treelauncher.backend.config.Config
import dev.treset.treelauncher.backend.config.GlobalConfig
import dev.treset.treelauncher.backend.config.setAppConfig
import dev.treset.treelauncher.backend.util.FileInitializer
import dev.treset.treelauncher.backend.util.file.LauncherFile
import dev.treset.treelauncher.generic.Button
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.icons
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.resources.painterResource
import treelauncher.composeapp.generated.resources.Res
import treelauncher.composeapp.generated.resources.icon
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

@Composable
fun ApplicationScope.ConfigLoader(
    content: @Composable () -> Unit
) {
    LaunchedEffect(Unit) {
        if (!validateSessionLock()) {
            throw IllegalStateException("Failed to lock session file")
        }
    }

    var loaded by remember { mutableStateOf(false) }
    var requiresPath by remember { mutableStateOf(false) }
    var initializing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!loaded) {
            LOGGER.info { "Loading config, file=$configFile..." }

            if (!configFile.isFile) {
                LOGGER.info { "No config found, attempting to patch old one" }
                try {
                    patchOldConfig()
                } catch (e: IOException) {
                    LOGGER.warn(e) { "Failed to patch old config" }
                }
                if (!configFile.isFile) {
                    LOGGER.info { "No config found, requesting new path" }
                    requiresPath = true
                    return@LaunchedEffect
                }
            }

            try {
                val config = GlobalConfig.fromJson(configFile.readString())

                if (config.path.isBlank()) {
                    LOGGER.warn { "Invalid config: path=${config.path}" }
                    requiresPath = true
                }

                val launcherConfig = Config(config)
                setAppConfig(launcherConfig)
                LOGGER.info { "Loaded Config" }
                loaded = true
            } catch (e: Exception) {
                LOGGER.warn(e) { "Failed to load config" }
                requiresPath = true
            }
        }
    }

    if(!loaded) {
        val windowState = remember { WindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(500.dp, 300.dp)
        ) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "TreeLauncher - Setup",
            state = windowState,
            icon = painterResource(Res.drawable.icon),
            visible = initializing || requiresPath
        ) {
            if (initializing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                ) {
                    Text(Strings.launcher.setup.initializing())
                }
            } else if (requiresPath) {
                var path by remember { mutableStateOf(defaultPath) }
                var changing by remember { mutableStateOf(false) }
                var showDirPicker by remember { mutableStateOf(false) }
                var error: Exception? by remember { mutableStateOf(null) }

                MaterialTheme(
                    colors = lightColors(primary = Color.Green)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                    ) {
                        Text(
                            Strings.launcher.setup.title(),
                            style = TextStyle(fontSize = 20.sp)
                        )

                        Text(Strings.launcher.setup.message())

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = path,
                                onValueChange = {
                                    path = it
                                    error = null
                                }
                            )

                            IconButton(
                                onClick = {
                                    showDirPicker = true
                                },
                                icon = icons().folder,
                                tooltip = Strings.launcher.setup.dirPicker()
                            )
                        }
                        error?.let {
                            Text(
                                Strings.launcher.setup.error(it),
                                color = Color.Red
                            )
                        }

                        Button(
                            onClick = {
                                changing = true
                                Thread {
                                    try {
                                        val dir = LauncherFile.of(path)
                                        GlobalConfig.validateDataPath(dir)
                                        val globalConfig = GlobalConfig(path)
                                        configFile.write(globalConfig)
                                        setAppConfig(Config(globalConfig))

                                        if (GlobalConfig.isLauncherDataPath(dir)) {
                                            LOGGER.info { "Selected directory is already a launcher directory" }
                                            loaded = true
                                        } else {
                                            LOGGER.info { "Selected directory is not a launcher directory, initializing" }
                                            initializing = true
                                            val initializer = FileInitializer(dir)
                                            initializer.create()
                                            initializing = false
                                        }

                                        loaded = true
                                    } catch (e: Exception) {
                                        LOGGER.warn(e) { "Failed to set path" }
                                        error = e
                                    }
                                    changing = false
                                }.start()
                            }
                        ) {
                            Text("Confirm")
                        }

                        DirectoryPicker(
                            show = showDirPicker,
                            initialDirectory = if (LauncherFile.of(path)
                                    .isDirectory()
                            ) path else defaultPath,
                            onFileSelected = {
                                it?.let { path = it }
                                showDirPicker = false
                            },
                        )
                    }
                }
            }
        }
    } else {
        content()
    }
}

val configPath = if(!System.getenv("treelauncher.configPath").isNullOrBlank()) {
    LauncherFile.of(System.getenv("treelauncher.configPath"))
} else {
    LauncherFile.of(System.getenv("LOCALAPPDATA"), "treelauncher-config")
}

val configFile = configPath.child("config.json")

val sessionFile = configPath.child("session.lock")

private var fileChannel: FileChannel? = null
private var lock: FileLock? = null

private fun validateSessionLock(): Boolean {
    LOGGER.info { "Validating session lock..." }
    if (!sessionFile.exists()) {
        LOGGER.debug { "Session file does not exist, creating..." }
        try {
            sessionFile.createFile()
        } catch (e: IOException) {
            LOGGER.error(e) { "Failed to create session file" }
            return false
        }
    }

    try {
        LOGGER.debug { "Locking session file..." }
        fileChannel = RandomAccessFile(sessionFile, "rw").channel
        lock = fileChannel!!.lock()
    } catch (e: Exception) {
        LOGGER.error(e) { "Failed to lock session file" }
        return false
    }

    LOGGER.info { "Session lock validated" }
    return true
}

fun unlockSession() {
    try {
        lock?.release()
        fileChannel?.close()
    } catch (e: IOException) {
        LOGGER.error(e) { "Failed to unlock session file" }
    }
}

@Throws(IOException::class)
private fun patchOldConfig() {
    LOGGER.debug { "Patching old config" }
    val file = File("app/treelauncher.conf")

    if(!file.isFile) {
        LOGGER.debug { "No old config found" }
        return
    }

    val oldConfig = file.readText()
    for(line in oldConfig.lines()) {
        if(line.startsWith("path=")) {
            LOGGER.debug { "Found old path: $line" }
            val path = line.substringAfter("path=").trim()
            val globalConfig = GlobalConfig(path)
            configFile.write(globalConfig)
            file.delete()
            return
        }
    }
    LOGGER.debug { "No path found in old config" }
}

private val defaultPath = "${System.getenv("LOCALAPPDATA")}/treelauncher-data"

private val LOGGER = KotlinLogging.logger {}