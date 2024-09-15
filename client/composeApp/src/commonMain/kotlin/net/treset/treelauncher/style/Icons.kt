package net.treset.treelauncher.style

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import net.treset.treelauncher.backend.util.ModProviderStatus

fun icons() = net.treset.treelauncher.style.Icons

object Icons {
    val instances = Icons.Rounded.Dashboard
    val saves = Icons.Rounded.Save
    val resourcePacks = Icons.Rounded.Inventory2
    val options = Icons.Rounded.Tune
    val mods = Icons.Rounded.Code
    val settings = Icons.Rounded.Settings
    val add = Icons.Rounded.AddCircle
    val time = Icons.Rounded.Timer
    val version = Icons.Rounded.SwapHoriz
    val comboBox = Icons.Rounded.ArrowDropDown
    val updateHint = Icons.Rounded.DownloadForOffline
    val start = Icons.Rounded.PlayArrow
    val logout = Icons.AutoMirrored.Rounded.Logout
    val update = Icons.Rounded.Download
    val gitHub: Painter @Composable get() = painterResource("icons/github.svg")
    val sort = Icons.Rounded.SwapVert
    val play: Painter @Composable get() = painterResource("icons/play_arrow_scaled.svg")
    val edit = Icons.Rounded.Edit
    val folder = Icons.Rounded.Folder
    val file = Icons.AutoMirrored.Rounded.InsertDriveFile
    val delete = Icons.Rounded.Delete
    val change = Icons.Rounded.Sync
    val back = Icons.AutoMirrored.Rounded.ArrowBack
    val language = Icons.Rounded.Language
    val download = Icons.Rounded.CloudDownload
    val enable = Icons.Rounded.PowerOff
    val disable = Icons.Rounded.Power
    val enabled = { enabled: Boolean -> if (enabled) disable else enable }
    val browser = Icons.Rounded.OpenInBrowser
    val search = Icons.Rounded.Search
    val modrinth: Painter @Composable get() = painterResource("icons/modrinth.svg")
    val curseforge: Painter @Composable get() = painterResource("icons/curseforge.svg")
    val selectFile = Icons.Rounded.FileOpen
    val news = Icons.Rounded.Newspaper
    val expand = Icons.Rounded.ExpandMore
    val zip = Icons.Rounded.FolderZip
    val check = Icons.Rounded.Check
    val darkMode = Icons.Rounded.DarkMode
    val lightMode = Icons.Rounded.LightMode
    val systemMode = Icons.Rounded.BrightnessMedium
    val list = Icons.Rounded.ViewStream
    val plus = Icons.Rounded.AddCircle
    val minus = Icons.Rounded.RemoveCircle
    val warning = Icons.Rounded.Warning
    val down = Icons.Rounded.KeyboardDoubleArrowDown
    val help = Icons.AutoMirrored.Rounded.Help
    val close = Icons.Rounded.Close
    val copy = Icons.Rounded.ContentCopy

    val modrinthColor = @Composable { modrinthStatus: ModProviderStatus ->
        when (modrinthStatus) {
            ModProviderStatus.CURRENT -> Color(0xFF1BD96A)
            ModProviderStatus.AVAILABLE -> LocalContentColor.current
            ModProviderStatus.UNAVAILABLE -> LocalContentColor.current.copy(alpha = 0.5f)
        }
    }
    val curseforgeColor = @Composable { curseforgeStatus: ModProviderStatus ->
        when (curseforgeStatus) {
            ModProviderStatus.CURRENT -> Color(0xFFF16436)
            ModProviderStatus.AVAILABLE -> LocalContentColor.current
            ModProviderStatus.UNAVAILABLE -> LocalContentColor.current.copy(alpha = 0.5f)
        }
    }
}