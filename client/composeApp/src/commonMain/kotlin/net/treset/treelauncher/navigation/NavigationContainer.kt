package net.treset.treelauncher.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.generic.IconButton
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.login.LoginContext
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
    loginContext: LoginContext,
    content: @Composable (NavigationContext) -> Unit
) {
    val navigationState = remember { mutableStateOf(NavigationState.INSTANCES) }
    var profileImage: BufferedImage? by remember { mutableStateOf(null) }
    var updateAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Thread {
            profileImage = loginContext.userAuth.getUserIcon()
        }.start()

        Thread {
            updateAvailable = !(updater().getUpdate().latest?: true)
        }.start()
    }

    var height by remember { mutableStateOf(0) }
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxSize().onSizeChanged { height = it.height }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalDensity.current.run { height.toDp() } - 51.dp)
        ) {
            content(NavigationContext(
                navigationState.value
            ) { navigationState.value = it })
        }

        Divider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Row(
                modifier = Modifier.padding(5.dp),
            ) {
                NavigationButton(
                    NavigationState.INSTANCES,
                    navigationState,
                    icons().instances,
                    strings().nav.home(),
                )
                NavigationButton(
                    NavigationState.ADD,
                    navigationState,
                    icons().add,
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
                    icons().saves,
                    strings().nav.saves()
                )
                NavigationButton(
                    NavigationState.RESSOURCE_PACKS,
                    navigationState,
                    icons().resourcePacks,
                    strings().nav.resourcepacks()
                )
                NavigationButton(
                    NavigationState.OPTIONS,
                    navigationState,
                    icons().options,
                    strings().nav.options()
                )
                NavigationButton(
                    NavigationState.MODS,
                    navigationState,
                    icons().mods,
                    strings().nav.mods()
                )
            }

            Row(
                modifier = Modifier.padding(5.dp),
            ) {

                NavigationButton(
                    NavigationState.SETTINGS,
                    navigationState
                ) {
                    Icon(
                        icons().settings,
                        contentDescription = strings().nav.settings(),
                        modifier = Modifier.size(36.dp)
                    )
                    profileImage?.let {
                        Image(
                            it.toComposeImageBitmap(),
                            contentDescription = strings().nav.settings(),
                            contentScale = FixedScale(LocalDensity.current.density * 3.5f),
                            filterQuality = FilterQuality.None,
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }

                    if (updateAvailable) {
                        Icon(
                            icons().updateHint,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Update Available",
                            modifier = Modifier
                                .offset(12.dp, 12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
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
    tooltip: String? = null,
    content: @Composable () -> Unit
) {
    IconButton (
        onClick = { currentState.value = targetState },
        selected = targetState == currentState.value,
        interactionTint = MaterialTheme.colorScheme.primary,
        modifier = modifier.aspectRatio(1f),
        tooltip = tooltip
    ) {
        content()
    }
}

@Composable
private fun NavigationButton(
    targetState: NavigationState,
    currentState: MutableState<NavigationState>,
    icon: ImageVector,
    tooltip: String = "",
    modifier: Modifier = Modifier
) {
    NavigationButton(
        targetState,
        currentState,
        modifier,
        tooltip
    ) {
        Icon (
            icon,
            contentDescription = tooltip,
            modifier = Modifier.size(36.dp)
        )
    }
}

data class NavigationContext(
    val navigationState: NavigationState,
    val navigateTo: (NavigationState) -> Unit
)
