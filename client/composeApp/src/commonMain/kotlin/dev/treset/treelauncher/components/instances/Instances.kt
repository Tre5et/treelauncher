package dev.treset.treelauncher.components.instances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.launching.GameLauncher
import dev.treset.treelauncher.backend.util.sort.InstanceSortProviders
import dev.treset.treelauncher.components.Components
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.login.LoginContext
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.util.launchGame
import java.io.IOException

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Instances() {
    var loading by remember { mutableStateOf(true) }

    Components(
        Strings.selector.instance.title(),
        components = AppContext.files.instanceComponents,
        componentManifest = AppContext.files.instanceManifest,
        checkHasComponent = {_,_ -> false },
        reload = {
            try {
                AppContext.files.reload()
            } catch (e: IOException) {
                AppContext.severeError(e)
            }
            loading = false
        },
        constructSharedData = SharedInstanceData::of,
        detailsContent = {
            this.InstanceDetails()
        },
        detailsScrollable = { false },
        isEnabled = {
            AppContext.runningInstance != this
        },
        actionBarSpecial = {
            IconButton(
                onClick = {
                    val launcher = GameLauncher(
                        component,
                        AppContext.files,
                        LoginContext.isOffline(),
                        LoginContext.userAuth.minecraftUser
                    )
                    launchGame(
                        launcher
                    ) { }
                },
                painter = icons().play,
                size = 32.dp,
                highlighted = true,
                enabled = AppContext.runningInstance == null,
                tooltip = Strings.selector.instance.play()
            )
        },
        actionBarBoxContent = { scope ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = with(scope) {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(1 / 2f)
                }
            ) {
                (headerContent.value)()
            }
        },
        actionBarFraction = 1/2f,
        selectorButton = { component, selected, enabled, onSelected ->
            InstanceButton(
                component,
                selected,
                enabled,
                onSelected
            )
        },
        sorts = InstanceSortProviders,
        selectorFraction = 1/3f,
        allowSettings = false
    )
}