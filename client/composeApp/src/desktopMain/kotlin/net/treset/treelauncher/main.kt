package net.treset.treelauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.treset.mcdl.util.OsUtil
import net.treset.treelauncher.backend.config.Window
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.*
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt


fun main() = application {
    var theme by remember { mutableStateOf(Theme.SYSTEM) }

    val app = remember {
        LauncherApp(
            ::exitApplication
        ) {
            theme = it
        }
    }

    val titleColors = if(theme.isDark()) JewelTheme.darkThemeDefinition() else JewelTheme.lightThemeDefinition()
    val styling = ComponentStyling.decoratedWindow(
        titleBarStyle = if(theme.isDark()) darkTitleBar() else lightTitleBar()
    )
    val colors = if(theme.isDark()) darkColors() else lightColors()

    val density = LocalDensity.current
    val position: WindowPosition = remember {
        appSettings().window?.let {
            WindowPosition.Absolute(it.x, it.y)
                .let { if(isValidPosition(it, density)) it else null }
        } ?: WindowPosition(Alignment.Center)
    }

    val size: DpSize = remember {
        appSettings().window?.let {
            DpSize(it.width, it.height)
                .let { if(isValidSize(it)) it else null }
        } ?: with(density) { DpSize(min(1600.dp, Toolkit.getDefaultToolkit().screenSize.width.toDp() - 100.dp), min(900.dp, Toolkit.getDefaultToolkit().screenSize.height.toDp() - 100.dp)) }
    }

    val placement = remember {
        appSettings().window?.let {
            if(it.isMaximized) WindowPlacement.Maximized else null
        } ?: WindowPlacement.Floating
    }

    val windowState = rememberWindowState(
        placement = placement,
        position = position,
        size = size
    )

    LaunchedEffect(Unit) {
        resetSize = {
            windowState.position = WindowPosition(Alignment.Center)
            windowState.size = with(density) { DpSize(min(1600.dp, Toolkit.getDefaultToolkit().screenSize.width.toDp() - 100.dp), min(900.dp, Toolkit.getDefaultToolkit().screenSize.height.toDp() - 100.dp)) }
            windowState.placement = WindowPlacement.Floating
        }
    }

    IntUiTheme(
        theme = titleColors,
        styling = styling
    ) {
        DecoratedWindow(
            onCloseRequest = { app().exit() },
            title = strings().launcher.name(),
            state = windowState,
            icon = BitmapPainter(useResource("icon_default.png", ::loadImageBitmap)),
        ) {
            window.minimumSize = with(LocalDensity.current) {
                Dimension(200.dp.toPx().roundToInt(), 100.dp.toPx().roundToInt())
            }

            LaunchedEffect(windowState) {
                snapshotFlow { windowState.placement }
                    .map { it == WindowPlacement.Maximized }
                    .onEach(::onWindowMaximized)
                    .launchIn(this)

                snapshotFlow { windowState.size }
                    .filter { it.isSpecified && windowState.placement == WindowPlacement.Floating }
                    .onEach(::onWindowResize)
                    .launchIn(this)

                snapshotFlow { windowState.position }
                    .filter { it.isSpecified && windowState.placement == WindowPlacement.Floating }
                    .onEach(::onWindowRelocate)
                    .launchIn(this)

            }

            TitleBar(Modifier.newFullscreenControls()) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.material.onBackground
                ) {
                    Box(
                        modifier = Modifier.offset(x = (-9).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            strings().launcher.name(),
                            style = typography().titleSmall,
                            modifier = Modifier.offset(y = 2.dp)
                        )

                        IconButton(
                            onClick = {
                                AppContext.openNews()
                            },
                            icon = icons().news,
                            tooltip = strings().news.tooltip(),
                            modifier = Modifier
                                .offset(x = 82.dp, y = 1.dp)
                        )
                    }
                }
            }

            var restartRequired by remember { mutableStateOf(false) }
            var downloading by remember { mutableStateOf(0F) }
            var initialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    KCEF.init(
                        builder = {
                            installDir(File("kcef", "bundle"))
                            progress {
                                onDownloading {
                                    downloading = max(it, 0F)
                                }
                                onInitialized {
                                    initialized = true
                                }
                            }
                        }, onError = {
                            it?.printStackTrace()
                        }, onRestartRequired = {
                            restartRequired = true
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.material.background)
            ) {
                if (restartRequired) {
                    Text(
                        text = strings().launcher.status.restartRequired(),
                        color = colors.material.onBackground
                    )
                } else if(downloading > 0 && !initialized) {
                    Text(
                        text = strings().launcher.status.preparing(downloading.roundToInt()),
                        color = colors.material.onBackground
                    )
                } else {
                    App(app)
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    KCEF.disposeBlocking()
                }
            }
        }
    }
}

actual fun getUpdaterProcess(updaterArgs: String): ProcessBuilder {
    val updaterFile = File("resources/windows/updater.exe").let {
        if(it.isFile) it else File("app/resources/updater")
    }

    val commandBuilder = StringBuilder()
    commandBuilder.append(updaterFile.absolutePath)
    return if(OsUtil.isOsName("windows")) {
        ProcessBuilder("cmd.exe", "/c", "start", "cmd", if(appSettings().isDebug) "/k" else "/c",
            "$commandBuilder $updaterArgs"
        )
    } else {
        //TODO
        ProcessBuilder("UNIMPLEMENTED")
    }
}

var resetSize: () -> Unit = {}
actual fun resetWindow() = resetSize()


private fun isValidSize(size: DpSize): Boolean {
    return size.width > 200.dp && size.height > 100.dp
}

private fun inAnyScreen(position: WindowPosition.Absolute, density: Density): Boolean {
    val screens = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
    with(density) {
        return screens.any {
            val bounds = it.defaultConfiguration.bounds
            val x = bounds.x.toDp()
            val y = bounds.y.toDp()
            val width = bounds.width.toDp()
            val height = bounds.height.toDp()

            position.x >= x && position.y >= y && position.x <= x + width && position.y <= y + height
        }
    }
}


private fun isValidPosition(position: WindowPosition, density: Density): Boolean {
    return when(position) {
        is WindowPosition.Absolute -> {
            return inAnyScreen(position, density)
        }
        else -> true
    }
}

private fun onWindowMaximized(maximized: Boolean) {
    appSettings().window = Window(
        appSettings().window?.x ?: (-1).dp,
        appSettings().window?.y ?: (-1).dp,
        appSettings().window?.width ?: (-1).dp,
        appSettings().window?.height ?: (-1).dp,
        maximized
    )
}

private fun onWindowResize(size: DpSize) {
    appSettings().window = Window(
        appSettings().window?.x ?: (-1).dp,
        appSettings().window?.y ?: (-1).dp,
        size.width,
        size.height,
        appSettings().window?.isMaximized ?: false
    )
}

private fun onWindowRelocate(position: WindowPosition) {
    appSettings().window = Window(
        position.x,
        position.y,
        appSettings().window?.width ?: (-1).dp,
        appSettings().window?.height ?: (-1).dp,
        appSettings().window?.isMaximized ?: false
    )
}