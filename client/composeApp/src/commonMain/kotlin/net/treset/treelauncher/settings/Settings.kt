package net.treset.treelauncher.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.backend.config.appConfig
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.update.updater
import net.treset.treelauncher.backend.util.string.UrlString
import net.treset.treelauncher.generic.TitledCheckBox
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.generic.TitledComboBox
import net.treset.treelauncher.localization.Language
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.Theme
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.style.setTheme
import net.treset.treelauncher.style.theme
import java.awt.image.BufferedImage


@Composable
fun Settings() {
    var userImage: BufferedImage? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        userImage = userAuth().getUserIcon()
    }

    TitledColumn(
        title = strings().settings.title(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().settings.appearance(),
                style = MaterialTheme.typography.titleSmall
            )

            TitledComboBox(
                strings().settings.language(),
                items = Language.entries,
                onSelected = {
                    language().appLanguage = it
                },
                defaultSelected = language().appLanguage
            )

            var restart by remember { mutableStateOf(false) }
            TitledComboBox(
                strings().settings.theme(),
                items = Theme.entries,
                onSelected = {
                    setTheme(it)
                    restart = true
                },
                defaultSelected = theme()
            )

            if(restart) {
                Text(
                    strings().settings.restratRequired(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().settings.path.title(),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            var tfValue by remember { mutableStateOf(appConfig().BASE_DIR.absolutePath) }
            TextField(
                tfValue,
                onValueChange = {
                    tfValue = it
                }
            )

            var sbState by remember { mutableStateOf(true) }
            TitledCheckBox(
                sbState,
                onCheckedChange = {
                    sbState = it
                },
                text = strings().settings.path.remove()
            )

            Button(
                onClick = {
                    //TODO: change
                }
            ) {
                Text(
                    strings().settings.path.apply()
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                strings().settings.sync.title(),
                style = MaterialTheme.typography.titleSmall,
            )

            var tfUrl by remember { mutableStateOf(appSettings().syncUrl ?: "") }
            var tfPort by remember { mutableStateOf(appSettings().syncPort ?: "") }
            var tfKey by remember { mutableStateOf(appSettings().syncKey ?: "") }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "http://"
                )
                TextField(
                    tfUrl,
                    {
                        tfUrl = it
                    },
                    placeholder = { Text(strings().settings.sync.url()) }
                )
                Text(":")
                TextField(
                    tfPort,
                    {
                        tfPort = it
                    },
                    placeholder = { Text(strings().settings.sync.port()) }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings().settings.sync.key()
                )
                TextField(
                    tfKey,
                    {
                        tfKey = it
                    },
                    placeholder = { Text(strings().settings.sync.keyPlaceholder()) }
                )
            }

            Button(
                onClick = {
                    //TODO: Test
                    appSettings().syncUrl = tfUrl
                    appSettings().syncPort = tfPort
                    appSettings().syncKey = tfKey
                }
            ) {
                Text(
                    strings().settings.sync.test()
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().settings.user()
            )
            Text(
                userAuth().minecraftUser?.name ?: "UNKNOWN",
                style = MaterialTheme.typography.titleMedium
            )
            userImage?.let {
                Image(
                    it.toComposeImageBitmap(),
                    contentDescription = "Profile Image",
                    contentScale = FixedScale(LocalDensity.current.density * 8f),
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                )
            }
            Text(
                userAuth().minecraftUser?.uuid ?: "UNKNOWN UUID",
                style = MaterialTheme.typography.labelSmall
            )
            net.treset.treelauncher.generic.IconButton(
                onClick = {
                    //TODO: logout
                },
                interactionTint = MaterialTheme.colorScheme.error,
                highlighted = true
            ) {
                Icon(
                    icons().Logout,
                    "Logout",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings().launcher.name(),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                strings().settings.version()
            )

            if(updater().getUpdate().latest == false) {
                net.treset.treelauncher.generic.IconButton(
                    onClick = {
                        //TODO: Update
                    },
                    highlighted = true
                ) {
                    Icon(
                        icons().Download,
                        "Download Update"
                    )
                }
                Text(
                    strings().settings.update.available()
                )
            }

            net.treset.treelauncher.generic.IconButton(
                onClick = {
                    UrlString.of("https://github.com/Tre5et/treelauncher").openInBrowser()
                }
            ) {
                Icon(
                    painter = painterResource("icons/github.svg"),
                    contentDescription = "Link to Github Project"
                )
            }
        }
    }
}