package dev.treset.treelauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import dev.treset.mcdl.util.OsUtil
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.config.Window
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.*
import dev.treset.treelauncher.util.ConfigLoader
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
import kotlin.math.roundToInt


fun main() = application {
    ConfigLoader {
        val app = remember {
            LauncherApp(
                ::exitApplication
            )
        }

        val titleColors = if(AppSettings.theme.value.isDark()) JewelTheme.darkThemeDefinition() else JewelTheme.lightThemeDefinition()
        val styling = ComponentStyling.decoratedWindow(
            titleBarStyle = if(AppSettings.theme.value.isDark()) darkTitleBar() else lightTitleBar()
        )
        val colors = if(AppSettings.theme.value.isDark()) darkColors() else lightColors()

        val density = LocalDensity.current
        val position: WindowPosition = remember {
            AppSettings.window.value?.let {
                WindowPosition.Absolute(it.x, it.y)
                    .let { if(isValidPosition(it, density)) it else null }
            } ?: WindowPosition(Alignment.Center)
        }

        val size: DpSize = remember {
            AppSettings.window.value?.let {
                DpSize(it.width, it.height)
                    .let { if(isValidSize(it)) it else null }
            } ?: with(density) { DpSize(min(1600.dp, Toolkit.getDefaultToolkit().screenSize.width.toDp() - 100.dp), min(900.dp, Toolkit.getDefaultToolkit().screenSize.height.toDp() - 100.dp)) }
        }

        val placement = remember {
            AppSettings.window.value?.let {
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
                title = Strings.launcher.name(),
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
                                Strings.launcher.name(),
                                style = typography().titleSmall,
                                modifier = Modifier.offset(y = 2.dp)
                            )

                            IconButton(
                                onClick = {
                                    AppContext.openNews()
                                },
                                icon = icons().news,
                                tooltip = Strings.news.tooltip(),
                                modifier = Modifier
                                    .offset(x = 82.dp, y = 1.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.material.background)
                ) {
                    App(app)
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
        ProcessBuilder("cmd.exe", "/c", "start", "cmd", if(AppSettings.isDebug.value) "/k" else "/c",
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
    AppSettings.window.value = Window(
        AppSettings.window.value?.x ?: (-1).dp,
        AppSettings.window.value?.y ?: (-1).dp,
        AppSettings.window.value?.width ?: (-1).dp,
        AppSettings.window.value?.height ?: (-1).dp,
        maximized
    )
}

private fun onWindowResize(size: DpSize) {
    AppSettings.window.value = Window(
        AppSettings.window.value?.x ?: (-1).dp,
        AppSettings.window.value?.y ?: (-1).dp,
        size.width,
        size.height,
        AppSettings.window.value?.isMaximized ?: false
    )
}

private fun onWindowRelocate(position: WindowPosition) {
    AppSettings.window.value = Window(
        position.x,
        position.y,
        AppSettings.window.value?.width ?: (-1).dp,
        AppSettings.window.value?.height ?: (-1).dp,
        AppSettings.window.value?.isMaximized ?: false
    )
}