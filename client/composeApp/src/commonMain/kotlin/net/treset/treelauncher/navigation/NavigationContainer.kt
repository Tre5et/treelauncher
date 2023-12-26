package net.treset.treelauncher.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.colors
import net.treset.treelauncher.style.icons
import java.awt.image.BufferedImage

enum class NavigationState {
    INSTANCES,
    ADD,
    SAVES,
    RESSOURCE_PACKS,
    OPTIONS,
    MODS,
    SETTINGS
}

@Composable
fun NavigationContainer(
    content: @Composable (NavigationState) -> Unit
) {
    val navigationState = remember { mutableStateOf(NavigationState.INSTANCES) }
    var profileImage: BufferedImage? by remember { mutableStateOf(null) }
    var updateAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Thread {
            profileImage = userAuth().getUserIcon()
        }.start()

        Thread {
            updateAvailable = !(updater().getUpdate().latest?: true)
        }.start()
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        content(navigationState.value)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(5.dp),
            ) {
                NavigationButton(
                    NavigationState.INSTANCES,
                    navigationState,
                    icons().Dashboard,
                    strings().nav.home()
                )
                NavigationButton(
                    NavigationState.ADD,
                    navigationState,
                    icons().AddCircle,
                    strings().nav.add()
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(5.dp),
            ) {
                NavigationButton(
                    NavigationState.SAVES,
                    navigationState,
                    icons().Save,
                    strings().nav.saves()
                )
                NavigationButton(
                    NavigationState.RESSOURCE_PACKS,
                    navigationState,
                    icons().Inventory2,
                    strings().nav.resourcepacks()
                )
                NavigationButton(
                    NavigationState.OPTIONS,
                    navigationState,
                    icons().Tune,
                    strings().nav.options()
                )
                NavigationButton(
                    NavigationState.MODS,
                    navigationState,
                    icons().Code,
                    strings().nav.mods()
                )
            }

            Row(
                modifier = Modifier.padding(5.dp),
            ) {

                NavigationButton(
                    NavigationState.SETTINGS,
                    navigationState,
                    //TODO: Fix overflow
                ) { tint: Color ->
                    Icon(
                        icons().AccountBox,
                        contentDescription = strings().nav.settings(),
                        tint = tint,
                        modifier = Modifier.size(36.dp)
                    )
                    profileImage?.let {
                        Image(
                            it.toComposeImageBitmap(),
                            contentDescription = strings().nav.settings(),
                            contentScale = FixedScale(4f),
                            filterQuality = FilterQuality.None,
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .border(2.dp, tint)
                        )
                    }

                    if(updateAvailable) {
                        Icon(
                            icons().DownloadForOffline,
                            tint = colors().primary,
                            contentDescription = "Update Available",
                            modifier = Modifier
                                .offset(12.dp, 12.dp)
                                .clip(CircleShape)
                                .background(colors().background)
                                .border(2.dp, colors().background, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationButton(
    targetState: NavigationState,
    currentState: MutableState<NavigationState>,
    modifier: Modifier = Modifier,
    content: @Composable (Color) -> Unit
) {
    IconButton (
        onClick = { currentState.value = targetState },
        selected = targetState == currentState.value,
        interactionTint = colors().primary,
        modifier = modifier
    ) {
        content(it)
    }
}

@Composable
private fun NavigationButton(
    targetState: NavigationState,
    currentState: MutableState<NavigationState>,
    icon: ImageVector,
    contentDescription: String = "",
    modifier: Modifier = Modifier
) {
    NavigationButton(
        targetState,
        currentState,
        modifier
    ) {
        Icon (
            icon,
            contentDescription = contentDescription,
            tint = it,
            modifier = Modifier.size(36.dp)
        )
    }
}
