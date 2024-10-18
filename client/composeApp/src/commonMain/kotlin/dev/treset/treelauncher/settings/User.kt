package dev.treset.treelauncher.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.auth.userAuth
import dev.treset.treelauncher.generic.IconButton
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.login.LoginContext
import dev.treset.treelauncher.style.icons
import java.awt.image.BufferedImage

@Composable
fun User() {
    var userImage: BufferedImage? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        userImage = userAuth().getUserIcon()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(12.dp)
    ) {
        Text(
            Strings.settings.user()
        )
        Text(
            userAuth().minecraftUser?.username ?: "UNKNOWN",
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
        IconButton(
            onClick = LoginContext.logout,
            icon = icons().logout,
            size = 32.dp,
            interactionTint = MaterialTheme.colorScheme.error,
            highlighted = true,
            tooltip = Strings.settings.logout(),
            enabled = AppContext.runningInstance == null
        )
    }
}